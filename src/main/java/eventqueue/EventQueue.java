package eventqueue;

import com.google.gson.Gson;
import eventqueue.events.ChatMessageSentEvent;
import eventqueue.events.ConnectionAddedEvent;
import eventqueue.events.ConnectionRemovedEvent;
import eventqueue.events.Event;
import eventqueue.messages.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class EventQueue {

    private final Gson gson;

    private final Semaphore queueSemaphore;
    private final Queue<Event> newEvents;

    private final Semaphore socketsSemaphore;
    private final Map<Long, Map<String, LiveSocket>> onlineSockets;

    // variables hoisted from processEvents() scope to instance scope for reusing the memory
    // (constructed once, then reused along many processEvents() calls, instead of constructed again at each call, reducing memory allocations)

    private final Queue<Event> eventsToProcess;
    private final Queue<Event> eventsToRetry;

    private final Queue<LiveMessage> globalMessages;
    private final Map<Long, Queue<LiveMessage>> messagesPerUser;
    private final Queue<MessageToRetry> messagesToRetry;

    public EventQueue(Gson gson) {
        this.gson = gson;

        // semaphore to deal with contention from multiple RequestHandlers enqueueing events
        queueSemaphore = new Semaphore(1, true);
        newEvents = new ArrayDeque<>();

        // semaphore to deal with contention among
        // - processEvents() adding/removing connections from Connection{Added|Removed}Event
        // - PingThread asking the EventQueue to transfer a copy of the current onlineSockets
        socketsSemaphore = new Semaphore(1, true);
        onlineSockets = new HashMap<>();

        eventsToProcess = new ArrayDeque<>();
        eventsToRetry = new ArrayDeque<>();

        globalMessages = new ArrayDeque<>();
        messagesPerUser = new HashMap<>();
        messagesToRetry = new ArrayDeque<>();
    }

    public void enqueue(Event e) {
        System.out.println("EventQueue: Enqueueing " + e);
        queueSemaphore.acquireUninterruptibly();
        newEvents.add(e);
        queueSemaphore.release();
    }

    private byte[] prepareMessage(Object message) {
        return LiveSocket.prepareMessage(gson.toJson(message));
    }

    // TODO deal with the IOException when doing socket.outputStream().write(bytes) instead of propagating it up
    //  in all event process methods

    private void processConnectionAdded(ConnectionAddedEvent event) {
        if (!socketsSemaphore.tryAcquire()) {
            eventsToRetry.add(event);
            return;
        }

        var socket = event.socket();
        var userID = socket.userID();
        var token = socket.userToken();

        var userWasAlreadyOnline = true;

        var userSockets = onlineSockets.get(userID);

        if (userSockets == null) {
            userWasAlreadyOnline = false;
            userSockets = new HashMap<>();
            onlineSockets.put(userID, userSockets);
        }

        userSockets.put(token, socket);

        if (!userWasAlreadyOnline) {
            var gMessage = new UserOnlineMessage(userID);
            globalMessages.add(gMessage);
        }

        socketsSemaphore.release();
    }

    private void processConnectionRemoved(ConnectionRemovedEvent event) throws IOException {
        if (!socketsSemaphore.tryAcquire()) {
            eventsToRetry.add(event);
            return;
        }
        // Semaphore acquired, don't forget to release
        // really wish I could just defer socketsSemaphore.release(); like in Go

        var userID = event.userID();
        var token = event.token();

        var userNotOnlineAnymore = false;

        var userSockets = onlineSockets.get(userID);
        if (userSockets != null) {
            System.out.println("  user has sockets: " + userSockets);
            var socketDisconnected = userSockets.remove(token);
            if (socketDisconnected != null) {
                System.out.println("  token match, closing socket");
                // Just close the socket, no success message or anything
                // Client immediatly stops receiving messages
                // TODO is this the right to do?
                socketDisconnected.close();
            } else System.out.println("  NO token match");
            // If that was the only connection the user had,
            // the user is not online anymore
            if (userSockets.isEmpty()) {
                System.out.println("  NO remaining connections");
                onlineSockets.remove(userID);
                userNotOnlineAnymore = true;
            } else System.out.println("  has remaining connections");
        } else System.out.println("  user has NO sockets");

        if (userNotOnlineAnymore) {
            System.out.println("  NOT online anymore");
            var gMessage = new UserOfflineMessage(userID);
            globalMessages.add(gMessage);
        } else System.out.println("  still online");

        socketsSemaphore.release();
    }

    private void processChatMessageSent(ChatMessageSentEvent chat) {
        var uMessage = ChatMessageReceivedMessage.from(chat);
        messagesPerUser
            .computeIfAbsent(chat.receiverID(), id -> new ArrayDeque<>())
            .add(uMessage);
    }

    public Map<Long, Map<String, LiveSocket>> copySockets() {
        socketsSemaphore.acquireUninterruptibly();
        var copy = new HashMap<Long, Map<String, LiveSocket>>();
        for (var userEntry : onlineSockets.entrySet()) {
            var id      = userEntry.getKey();
            var sockets = userEntry.getValue();
            var socketsCopy = new HashMap<String, LiveSocket>();
            for (var sessionEntry : sockets.entrySet()) {
                var token  = sessionEntry.getKey();
                var socket = sessionEntry.getValue();
                socketsCopy.put(token, socket);
            }
            copy.put(id, socketsCopy);
        }
        socketsSemaphore.release();
        return copy;
    }

    public Collection<Long> onlineUsers() {
        var list = new ArrayList<Long>(onlineSockets.size());
        socketsSemaphore.acquireUninterruptibly();
        list.addAll(onlineSockets.keySet());
        socketsSemaphore.release();
        return list;
    }

    public void processEvents() throws IOException {

        while (!eventsToRetry.isEmpty()) {
            eventsToProcess.add(eventsToRetry.remove());
        }

        // All we do with the queue is transfer it to another local queue for processing
        // This way, other threads can keep enqueueing events to it, at the same time we do the processing
        // I.e. we can process the events without blocking the event queue
        queueSemaphore.acquireUninterruptibly();
        while (!newEvents.isEmpty()) {
            eventsToProcess.add(newEvents.remove());
        }
        queueSemaphore.release();

        while (!eventsToProcess.isEmpty()) {
            var event = eventsToProcess.remove();

            System.out.println("Processing event " + event);

            if (event instanceof ConnectionAddedEvent conn) {
                processConnectionAdded(conn);
            } else if (event instanceof ConnectionRemovedEvent conn) {
                processConnectionRemoved(conn);
            } else if (event instanceof ChatMessageSentEvent chat) {
                processChatMessageSent(chat);
            }
        }

        // TODO what should the EventQueue do if the client closes one of the live sockets?
        // Currently we probably get an IOException
        // Maybe we could test for .isClosed() and then treat it as a connection removed event
        // (remove the socket, broadcast that the user got offline if that was their last connection etc.)

        while (!messagesToRetry.isEmpty()) {
            var message = messagesToRetry.remove();

            if (!message.attempt()) {
                continue;
            }
            System.out.println("Retrying message " + message);

            var bytes = prepareMessage(message.message());

            var userSockets = onlineSockets.get(message.userID());
            if (userSockets != null) {
                var socket = userSockets.get(message.userToken());
                if (socket != null) {
                    if (!socket.tryWrite(bytes)) {
                        messagesToRetry.add(message);
                    }
                }
            }

        }

        while (!globalMessages.isEmpty()) {
            var message = globalMessages.remove();
            System.out.println("Broadcasting message " + message);
            var bytes = prepareMessage(message);
            for (var socketMap : onlineSockets.values()) {
                for (var socket : socketMap.values()) {
                    if (!socket.tryWrite(bytes)) {
                        System.out.println("Couldn't acquire the socket, will retry later");
                        messagesToRetry.add(new MessageToRetry(socket.userID(), socket.userToken(), message));
                    }
                }
            }
        }

        for (var entry : messagesPerUser.entrySet()) {
            var userID = entry.getKey();
            var messages = entry.getValue();
            if (messages == null) continue;
            while (!messages.isEmpty()) {
                var message = messages.remove();
                var bytes = prepareMessage(message);
                var sockets = onlineSockets.get(userID);
                for (var socket : sockets.values()) {
                    if (!socket.tryWrite(bytes)) {
                        System.err.println("Couldn't acquire the socket, will retry later");
                        messagesToRetry.add(new MessageToRetry(socket.userID(), socket.userToken(), message));
                    }
                }
            }
        }
        messagesPerUser.clear();
    }

}
