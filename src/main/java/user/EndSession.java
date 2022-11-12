package user;

import infra.*;

import java.sql.SQLException;

public class EndSession extends RequestHandler {
    public EndSession(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException {
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