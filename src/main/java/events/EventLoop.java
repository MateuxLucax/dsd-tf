package events;

public class EventLoop extends Thread {

    private static final int RUN_EVERY_MS = 1000; // Run roughly every second

    private final EventQueue eventQueue;

    public EventLoop(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                var start = System.currentTimeMillis();
                System.out.println("Processing events...");
                this.eventQueue.processEvents();
                var end = System.currentTimeMillis();
                var processingTime = end - start;
                System.out.printf("Event loop: processing took %dms\n", processingTime);
                Thread.sleep(RUN_EVERY_MS - processingTime);
            }
        } catch (InterruptedException e) {
            System.err.println("Event loop got interrupted");
            e.printStackTrace();
        }
    }
}
