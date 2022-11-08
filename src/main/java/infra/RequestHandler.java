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

    public String getToken() {
        return request.headers().get("token");
    }

    public long getUserId() throws ErrorResponse {
        var token = getToken();
        var sessionData = ctx.sessionManager().getSessionData(token);
        if (sessionData == null) {
            throw new ErrorResponse("badRequest", MsgCode.TOKEN_EXPIRED);
        }
        return sessionData.getUserId();
    }

    public <T> T readJson(Class<T> C) throws ErrorResponse {
        try {
            var body = new String(request.body());
            var json = ctx.gson().fromJson(body, C);
            if (json == null) {
                throw new NullPointerException();
            }
            return json;
        } catch (JsonSyntaxException | NullPointerException e) {
            throw new ErrorResponse("internal", MsgCode.FAILED_TO_PARSE_JSON);
        }
    }

    public abstract Response run() throws SQLException;

    // true by default, handlers that do not require it need to override
    public boolean tokenRequired() {
        return true;
    }
}
