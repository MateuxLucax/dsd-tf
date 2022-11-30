package infra;

import eventqueue.EventLoop;
import infra.session.SessionCleaner;
import infra.session.SessionManager;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) {

        int port = args.length >= 1 ? Integer.parseInt(args[0]) : 8080;
        System.out.println("Running on port " + port);

        // So we can use something like a test.db for the automated tests
        var db = args.length >= 2 ? args[1] : "xet.db";
        System.out.println("Database: " + db);

        Database.setFile(db);

        // TODO? set up a way to gracefully shut down the server, closing existing connections sending a "server closing" message or something

        try (var server = new ServerSocket(port)) {
            System.out.println("ServerSocket initialized, waiting connections...");

            var sharedContext = new SharedContext();

            var sessionCleaner = new SessionCleaner(
                sharedContext.sessionManager(), SessionManager.THREAD_SLEEP.toMillis()
            );
            sessionCleaner.start();

            var eventLoop = new EventLoop( sharedContext.eventQueue() );
            eventLoop.start();

            while (true) {
                // Don't auto-close the socket, some connections will be kept alive for listening to updates
                var socket = server.accept();
                new ConnectionHandler(socket, sharedContext).start();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
