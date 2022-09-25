package infra;

import user.CreateUserHandler;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ConnectionHandler extends Thread {

    private final Socket socket;
    private final SharedContext ctx;

    public ConnectionHandler(Socket socket, SharedContext ctx) {
        this.socket = socket;
        this.ctx = ctx;
    }

    public void run() {
        super.run();

        try {
            try {
                var in = socket.getInputStream();
                var out = socket.getOutputStream();

                var res = new ResponseWriter(out);

                try {
                    var req = Request.from(in);

                    System.out.println("Read request successfully");

                    if (req.operation().equals("create-user")) {
                        new CreateUserHandler(req, res, ctx).run();
                    } else {
                        res.writeError("badRequest", "I haven't implemented that operation yet");
                    }

                    // if handler.closeAfterResponse(), by default true, except when the request is for listening to updates
                    System.out.println("Closing socket");
                    socket.close();

                } catch (IOException | SQLException e) {
                    res.writeError("internal", "");
                    e.printStackTrace();
                    socket.close();
                } catch (MalformedRequestException e) {
                    res.writeError("badRequest", e.getMessage());
                    e.printStackTrace();
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("FATAL ERROR: Failed to get socket input and/or output streams, or to close the socket");
                e.printStackTrace();
                socket.close();
            } catch (ResponseWriteException e) {
                System.err.println("FATAL ERROR: Failed to write a response");
                System.err.println("Underlying IO exception stack trace:");
                e.getIOException().printStackTrace();
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("FATAL ERROR: Failed to close the socket (possibly a second time)");
            e.printStackTrace();
        }
    }
}
