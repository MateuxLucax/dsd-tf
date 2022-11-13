package live;

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

    public Response run() throws SQLException, InterruptedException {
        try {
            var id = getUserId();
            var liveSockets = ctx.getLiveSockets();
            liveSockets.add(id, getSocket());
            return responseFactory.justOk();
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }

    public boolean keepSocketOpen() {
        return true;
    }
}
