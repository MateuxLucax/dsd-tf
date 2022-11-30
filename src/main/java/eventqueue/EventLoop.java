package eventqueue;

import java.io.IOException;

public class EventLoop extends Thread {

    private static final int RUN_EVERY_MS = 1000; // Run roughly every second

    private final EventQueue eventQueue;

    public EventLoop(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void run() {
        try {
            var processingTime = -1l;

            while (!isInterrupted()) {

                var start = System.currentTimeMillis();
                System.out.println("Processing events... (last processing took "+processingTime+"ms)");

                try {
                    this.eventQueue.processEvents();
                } catch (IOException e) {
                    System.err.println("! IOException when processing events");
                    System.err.println(e);
                }

                var end = System.currentTimeMillis();
                processingTime = end - start;

                Thread.sleep(Math.max(0, RUN_EVERY_MS - processingTime));
            }
        } catch (InterruptedException e) {
            System.err.println("Event loop got interrupted");
            e.printStackTrace();
        }
    }
}
