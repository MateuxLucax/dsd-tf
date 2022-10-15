package infra;

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

                var res = new ResponseWriter(out, this.ctx);

                try {
                    var req = Request.from(in);

                    var headers = req.headers();
                    var operation = headers.get("operation");

                    System.out.println("Read request successfully");

                    var maybeHandler = OperationLookup.get(operation);

                    if (maybeHandler.isEmpty()) {
                        res.writeError("badRequest", "I haven't implemented that operation yet.");
                    } else {
                        var handler = maybeHandler.get().constructor(req, res, ctx);
                        if (handler.tokenRequired()) {
                            if (!headers.containsKey("token")) {
                                throw MalformedRequestException.missingHeader("token");
                            }
                            var token = headers.get("token");

                            var mgr = ctx.sessionManager();
                            if (!SessionManager.tokenSyntaxValid(token)) {
                                throw MalformedRequestException.invalidHeaderValue("token");
                            }

                            if (!mgr.hasSession(token)) {
                                res.writeError("badRequest", "Token expired");
                            }
                        }
                        handler.run();
                    }

                    // TODO what if the request handler run() impl doesn't write anything to the body?

                    // The session is both for authorization and for listening to updates
                    // Because it's for listening to updates we don't close the socket, but keep it open and manage when to close it

                    // The client is supposed to know when the server has ended a message by using the zero byte as delimiter '\0'

                    // TODO close session handler for when users closes/minimizes the app or the phone goes into sleep mode (whenever it's not active)

                    // We won't implement listening for updates yet (when we do just remove the true||)
                    if (true|| !operation.equals("create-session")) {
                        socket.close();
                    }

                } catch (IOException | SQLException e) {
                    res.writeError("internal", "");
                    e.printStackTrace();
                    socket.close();
                } catch (MalformedRequestException e) {
                    res.writeError("badRequest", e.getMessage());
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
