package user;

import infra.SharedContext;
import infra.request.MsgCode;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;

import java.sql.SQLException;

public class EndSession extends RequestHandler {
    public EndSession(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException, InterruptedException {
        var mgr = ctx.sessionManager();
        var token = request.headers().get("token");
        var session = mgr.getSessionData(token);
        if (session == null) {
            return responseFactory.err("internal", MsgCode.INTERNAL);
        }
        mgr.removeSession(session);
        return responseFactory.justOk();
    }
}
