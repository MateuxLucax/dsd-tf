package eventqueue;

import eventqueue.events.ConnectionAddedEvent;
import infra.SharedContext;
import infra.request.*;

import java.io.IOException;

public class GoOnline extends RequestHandler {

    public GoOnline(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public boolean keepSocketOpen() {
        return createdLiveSocket != null;
    }

    public void afterResponseWritten() {
        if (createdLiveSocket != null) {
            createdLiveSocket.ioRelease();
        }
    }

    private LiveSocket createdLiveSocket = null;

    public Response run() {
        System.out.println("GoOnline request handler running");

        try {
            var userID = getUserId();
            var token = getToken();
            var socket = getSocket();

            try {
                createdLiveSocket = new LiveSocket(userID, token, socket);
                // Because the connection handler still needs to write a response to it:
                createdLiveSocket.ioAcquire();
                var event = new ConnectionAddedEvent(createdLiveSocket);
                ctx.eventQueue().enqueue(event);
                return responseFactory.justOk();
            } catch (IOException e) {
                throw new ErrorResponse("internal", MsgCode.IO_ERROR);
            }

        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
