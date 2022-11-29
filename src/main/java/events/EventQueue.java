package events;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;

public class EventQueue {

    private final Gson gson;

    private final Semaphore sema;
    private final Queue<Event> newEvents;

    private final Map<Long, Map<String, Socket>> onlineSockets;

    // hoisted from processEvents() scope to instance scope for reusing the memory
    // (constructed once, then reused along many processEvents() calls, instead of constructed again at each call)
    private final Queue<Event> eventsToProcess;
    private final Queue<Object> globalMessages;
    private final Map<Long, Queue<Object>> messagesPerUser;

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

    // TODO all these messages need an extra `type` field

    private record UserLoggedMessage(
        long userID
    ) {}

    private class OtherOnlineUsersMessage extends ArrayList<Long> {}

    public void processEvents() throws IOException {
        // At the start of each processEvents() call the hoisted maps/lists/queues should all be empty

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
            // Process each event...

            System.out.println("Processing event " + event);

            // TODO when processing ConnectionAddedEvent, also send a message to the user containing the list of all currently online users

            if (event instanceof ConnectionAddedEvent conn) {
                onlineSockets
                    .computeIfAbsent(conn.userID(), id -> new HashMap<>())
                    .put(conn.token(), conn.socket());

                var gMessage = new UserLoggedMessage(conn.userID());
                globalMessages.add(gMessage);

                var uMessage = new OtherOnlineUsersMessage();
                uMessage.addAll(onlineSockets.keySet());
                messagesPerUser
                    .computeIfAbsent(conn.userID(), id -> new ArrayDeque<>())
                    .add(uMessage);
            }
        }

        while (!globalMessages.isEmpty()) {
            var message = globalMessages.remove();
            System.out.println("Broadcasting message " + message);
            var bytes = prepareMessage(message);
            for (var socketMap : onlineSockets.values()) {
                for (var socket : socketMap.values()) {
                    socket.getOutputStream().write(bytes);
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
                    socket.getOutputStream().write(bytes);
                }
            }
        }

        messagesPerUser.clear();

        // send all global messages
        // send all per user messages
    }

}
