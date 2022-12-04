package eventqueue;

import com.google.gson.Gson;
import eventqueue.events.ConnectionRemovedEvent;
import eventqueue.messages.PingMessage;
import infra.SharedContext;

import java.io.IOException;

public class PingThread extends Thread {

    private static final int MILLISECOND = 1;
    private static final int SECOND = 1000 * MILLISECOND;
    private static final int MINUTE = 60 * SECOND;

    //private static final int INTERVAL = 5 * MINUTE;  // In "production"
    private static final int INTERVAL = 7 * SECOND;  // For testing

    private static final int TIMEOUT = 2 * SECOND;

    private final EventQueue eventQueue;
    private final Gson gson;

    public PingThread(SharedContext ctx) {
        this.eventQueue = ctx.eventQueue();
        this.gson = ctx.gson();
    }

    public void pingSocket(LiveSocket socket) {

        var id = socket.userID();
        var token = socket.userToken();

        var repliedCorrectly = false;

        try {

            // The socket could've got closed before we copied the socket list
            // but we stil have the old socket list, so we first have to check
            if (socket.isClosed()) {
                return;
            }

            socket.ioAcquire();
            socket.setSoTimeout(TIMEOUT);

            var json = gson.toJson(new PingMessage());
            socket.writeMessage(json);

            // Client needs to reply "pong" back

            var expected = "pong".getBytes();
            var idx = 0;

            var in = socket.in();
            var c = 0;

            while (idx < expected.length && (c = in.read()) != -1) {
                var match = expected[idx] == (byte) c;
                if (!match) break;
                idx++;
            }

            if (idx == expected.length) {
                repliedCorrectly = true;
            }

            socket.setSoTimeout(0);

        } catch (IOException e) {
            // IOException covers SocketException and SocketTimeoutException
            repliedCorrectly = false;
        } finally {
            socket.ioRelease();
        }


        if (!repliedCorrectly) {
            var event = new ConnectionRemovedEvent(id, token);
            eventQueue.enqueue(event);
        }
    }

    public void run() {
        try {
            while (!isInterrupted()) {

                var startTime = System.currentTimeMillis();

                var sockets = eventQueue.copySockets();
                for (var userSockets : sockets.values()) {
                    for (var socket : userSockets.values()) {
                        pingSocket(socket);
                    }
                }

                var endTime = System.currentTimeMillis();
                var runTime = endTime - startTime;

                Thread.sleep(Math.max(0, INTERVAL - runTime));
            }
        } catch (InterruptedException e) {
            System.err.println("PingThread got interrupted");
            System.err.println(e);
        }
    }

}
