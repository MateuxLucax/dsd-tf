package infra;

import infra.request.SharedContext;
import infra.session.SessionCleaner;
import infra.session.SessionManager;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) {

        int port = args.length >= 1 ? Integer.parseInt(args[0]) : 1235;
        System.out.println("Running on port " + port);

        // TODO? set up a way to gracefully shut down the server, closing existing connections sending a "server closing" message or something

        try (var server = new ServerSocket(port)) {
            System.out.println("ServerSocket initialized, waiting connections...");

            var sharedContext = new SharedContext();

            var sessionCleaner = new SessionCleaner(
                sharedContext.sessionManager(), SessionManager.THREAD_SLEEP.toMillis()
            );
            sessionCleaner.start();

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
