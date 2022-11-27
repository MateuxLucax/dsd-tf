package events;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class EventQueue {

    private final Semaphore sema;
    private final Queue<Event> newEvents;
    // Maybe we could replace the semaphore acquiring and releasing with synchronized (newEvents) {}
    // but the semaphore has the advantage that it can be fair (2nd constructor argument),
    // whereas Java monitors offer no such guarantee

    // eventsToProcess:
    // Queue of events to be processed in each call of processEvents()
    // Could be local to processEvents(), but instead we make it global to reuse its memory
    // The same is true for all other fields below
    private final Queue<Event> eventsToProcess;

    public EventQueue() {
        sema = new Semaphore(1, true);
        newEvents = new ArrayDeque<>();

        eventsToProcess = new ArrayDeque<>();
    }

    public void enqueue(Event e) {
        // This is (should be) the *only* point of contention between threads (request handlers etc.)
        // everything else happens in a single thread that periodically polls the queue and processes the events sequentially
        sema.acquireUninterruptibly();
        newEvents.add(e);
        sema.release();
    }

    public void processEvents() {
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

            // Collect messaged to send in lists
            // - globalMessages   (list<byteArray>)
            // - messagesPerUser  (map<user, byteArray>)
        }

        // send all global messages
        // send all per user messages
    }

}
