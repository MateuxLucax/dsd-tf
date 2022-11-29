package events;

import infra.SharedContext;
import infra.request.*;

public class GoOnline extends RequestHandler {

    public GoOnline(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public boolean keepSocketOpen() {
        return true;
    }

    public Response run() {
        System.out.println("GoOnline request handler running");

        try {
            var userID = getUserId();
            var token = getToken();
            var socket = getSocket();

            var event = new ConnectionAddedEvent(userID, token, socket);
            ctx.eventQueue().enqueue(event);
            return responseFactory.justOk();

        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
