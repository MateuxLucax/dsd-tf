package infra;

import com.google.gson.JsonSyntaxException;

import java.sql.SQLException;

public abstract class RequestHandler {

    protected final Request request;
    protected final ResponseWriter response;
    protected final SharedContext ctx;

    public RequestHandler(Request request, ResponseWriter response, SharedContext ctx) {
        this.request = request;
        this.response = response;
        this.ctx = ctx;
    }

    public <T> T readJson(Class<T> C) throws ErrorResponse {
        try {
            var body = new String(request.body());
            return ctx.gson().fromJson(body, C);
        } catch (JsonSyntaxException e) {
            throw new ErrorResponse("internal", "Failed to parse JSON request body");
        }
    }

    public abstract void run() throws ResponseWriteException, SQLException;

    // true by default, handlers that do not require it need to override
    public boolean tokenRequired() {
        return true;
    }
}
