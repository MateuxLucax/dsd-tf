package infra.request;

import com.google.gson.JsonSyntaxException;
import infra.SharedContext;

import java.net.Socket;
import java.sql.SQLException;

public abstract class RequestHandler {

    private Socket socket;
    protected final Request request;
    protected final SharedContext ctx;

    protected final ResponseFactory responseFactory;
    // shortcut for ctx.responseFactory(), will be used in most request handlers

    public RequestHandler(Request request, SharedContext ctx) {
        this.request = request;
        this.ctx = ctx;
        this.responseFactory = ctx.responseFactory();
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getToken() {
        return request.headers().get("token");
    }

    public long getUserId() throws ErrorResponse, InterruptedException {
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

    public abstract Response run() throws SQLException, InterruptedException;

    // true by default, handlers that do not require it need to override
    public boolean tokenRequired() {
        return true;
    }

    // false by default, handlers that require it need to override
    public boolean keepSocketOpen() {
        return false;
    }
}
