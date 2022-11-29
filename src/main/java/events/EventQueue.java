package events;

import com.google.gson.Gson;
import events.messages.EventMessage;
import events.messages.OnlineUserListMessage;
import events.messages.UserLoggedMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class EventQueue {

    private final Gson gson;

    private final Semaphore sema;
    private final Queue<Event> newEvents;

    private final Map<Long, Map<String, LiveSocket>> onlineSockets;

    // hoisted from processEvents() scope to instance scope for reusing the memory
    // (constructed once, then reused along many processEvents() calls, instead of constructed again at each call)
    private final Queue<Event> eventsToProcess;
    private final Queue<EventMessage> globalMessages;
    private final Map<Long, Queue<EventMessage>> messagesPerUser;

    public EventQueue(Gson gson) {
        this.gson = gson;

        sema = new Semaphore(1, true);
        newEvents = new ArrayDeque<>();

        onlineSockets = new HashMap<>();

        eventsToProcess = new ArrayDeque<>();
        globalMessages = new ArrayDeque<>();
        messagesPerUser = new HashMap<>();
    }

    public void enqueue(Event e) {
        System.out.printf("EventQueue: Acquring sema for %s\n", e);
        sema.acquireUninterruptibly();
        System.out.printf("EventQueue: Enqueueing %s\n", e);
        newEvents.add(e);
        sema.release();
    }

    public byte[] prepareMessage(Object message) {
        return prepareMessage(gson.toJson(message));
    }

    public static byte[] prepareMessage(String message) {
        var messageBytes = message.getBytes();
        var sizeBytes = Integer.toString(messageBytes.length).getBytes();

        var buf = new byte[sizeBytes.length + 1 + messageBytes.length];
        var off = 0;

        for (var b : sizeBytes) buf[off++] = b;
        buf[off++] = ' ';
        for (var b : messageBytes) buf[off++] = b;

        return buf;
    }

    public void processEvents() throws IOException {
        // All we do with the queue is transfer it to another local queue for processing
        // This way, other threads can keep enqueueing events to it, at the same time we do the processing
        // I.e. we can process the events without blocking the event queue
        sema.acquireUninterruptibly();
        while (!newEvents.isEmpty()) {
            eventsToProcess.add(newEvents.remove());
        }
        sema.release();

        while (!eventsToProcess.isEmpty()) {
            var event = eventsToProcess.remove();

            System.out.println("Processing event " + event);

            if (event instanceof ConnectionAddedEvent conn) {
                var socket = conn.socket();
                var userID = socket.userID();
                var token = socket.userToken();
                onlineSockets
                    .computeIfAbsent(userID, id -> new HashMap<>())
                    .put(token, socket);

                var gMessage = new UserLoggedMessage(userID);
                globalMessages.add(gMessage);

                var onlineUsers = new ArrayList<>(onlineSockets.keySet());
                var uMessage = new OnlineUserListMessage(onlineUsers);
                messagesPerUser
                    .computeIfAbsent(userID, id -> new ArrayDeque<>())
                    .add(uMessage);
            }
        }

        while (!globalMessages.isEmpty()) {
            var message = globalMessages.remove();
            System.out.println("Broadcasting message " + message);
            var bytes = prepareMessage(message);
            for (var socketMap : onlineSockets.values()) {
                for (var socket : socketMap.values()) {
                    if (socket.ioTryAcquire()) {
                        socket.outputStream().write(bytes);
                        socket.ioRelease();
                    }
                    // TODO deal with not being able to acquire the streams
                }
            }
        }

        for (var entry : messagesPerUser.entrySet()) {
            var userID = entry.getKey();
            var messages = entry.getValue();
            while (!messages.isEmpty()) {
                var message = messages.remove();
                System.out.println("Sending message " + message + " to " + userID);
                var bytes = prepareMessage(message);
                for (var socket : onlineSockets.get(userID).values()) {
                    if (socket.ioTryAcquire()) {
                        socket.outputStream().write(bytes);
                        socket.ioRelease();
                    }
                    // TODO deal with not being able to acquire the streams
                }
            }
        }

        messagesPerUser.clear();
    }

}
