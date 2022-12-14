package infra;

import infra.request.*;
import infra.session.SessionManager;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Optional;

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

                var factory = ctx.responseFactory();

                Optional<RequestHandler> maybeHandler = Optional.empty();
                Optional<Response> responseToWrite = Optional.empty();

                try {
                    var req = Request.from(in);

                    var headers = req.headers();
                    var operation = headers.get("operation");

                    var maybeHandlerConstructor = OperationLookup.get(operation);

                    if (maybeHandlerConstructor.isEmpty()) {
                        responseToWrite = Optional.of(factory.err("badRequest", MsgCode.UNKNOWN_OPERATION));
                    } else {
                        var handler = maybeHandlerConstructor.get().construct(req, ctx);
                        maybeHandler = Optional.of(handler);

                        handler.setSocket(socket);

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
                                responseToWrite = Optional.of(factory.err("badRequest", MsgCode.TOKEN_EXPIRED));
                            }
                        }

                        if (responseToWrite.isEmpty()) {
                            responseToWrite = Optional.of(handler.run());
                        }
                    }

                } catch (IOException | SQLException e) {
                    responseToWrite = Optional.of(factory.err("internal", MsgCode.INTERNAL));
                    e.printStackTrace();
                } catch (MalformedRequestException e) {
                    responseToWrite = Optional.of(factory.err("badRequest", MsgCode.MALFORMED_REQUEST));
                } finally {

                    responseToWrite
                        .orElse(factory.err("internal", MsgCode.NO_RESPONSE))
                        .writeTo(out);

                    var shouldCloseSocket = true;

                    if (maybeHandler.isPresent()) {
                        var handler = maybeHandler.get();
                        handler.afterResponseWritten();
                        if (handler.keepSocketOpen()) {
                            shouldCloseSocket = false;
                        }
                    }

                    if (shouldCloseSocket) {
                        socket.close();
                    }

                }
            } catch (IOException e) {
                System.err.println("FATAL ERROR: Failed to get socket input or output streams, or to close the socket");
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
