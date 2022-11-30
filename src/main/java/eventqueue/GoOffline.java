package eventqueue;

import eventqueue.events.ConnectionRemovedEvent;
import infra.SharedContext;
import infra.request.ErrorResponse;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;

import java.sql.SQLException;

// The request to turn off a live socket comes not from the live socket,
// but from a separate request (this one) identifying the live socket to turn off

public class GoOffline extends RequestHandler {

    public GoOffline(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException {
        try {
            var userID = getUserId();
            var token = getToken();

            var event = new ConnectionRemovedEvent(userID, token);
            ctx.eventQueue().enqueue(event);

            return responseFactory.justOk();
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
