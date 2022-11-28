package events;

import infra.SharedContext;
import infra.request.ErrorResponse;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;

import java.sql.SQLException;

public class GoOnline extends RequestHandler {

    public GoOnline(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public boolean keepSocketOpen() {
        return true;
    }

    public Response run() throws SQLException, InterruptedException {
        try {
            var userId = getUserId();
            var token = getToken();
            var socket = getSocket();
            var event = new ConnectionAddedEvent(userId, token, socket);

            ctx.eventQueue().enqueue(event);
            return responseFactory.justOk();

        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
