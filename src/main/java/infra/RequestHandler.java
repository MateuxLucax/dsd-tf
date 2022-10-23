package infra;

import com.google.gson.JsonSyntaxException;

import java.sql.SQLException;

public abstract class RequestHandler {

    protected final Request request;
    protected final SharedContext ctx;

    protected final ResponseFactory responseFactory;
    // shortcut for ctx.responseFactory(), will always be used in every request handler

    public RequestHandler(Request request, SharedContext ctx) {
        this.request = request;
        this.ctx = ctx;
        this.responseFactory = ctx.responseFactory();
    }

    public <T> T readJson(Class<T> C) throws ErrorResponse {
        try {
            var body = new String(request.body());
            return ctx.gson().fromJson(body, C);
        } catch (JsonSyntaxException e) {
            throw new ErrorResponse("internal", MsgCode.FAILED_TO_PARSE_JSON);
        }
    }

    public abstract Response run() throws SQLException;

    // true by default, handlers that do not require it need to override
    public boolean tokenRequired() {
        return true;
    }
}
