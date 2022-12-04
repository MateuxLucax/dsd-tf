package eventqueue;

import infra.SharedContext;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;

import java.sql.SQLException;

public class GetOnlineUsers extends RequestHandler {

    public GetOnlineUsers(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException {
        var ids = ctx.eventQueue().onlineUsers();
        return responseFactory.json(ids);
    }
}
