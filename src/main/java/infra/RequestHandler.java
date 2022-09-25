package infra;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;
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

    public <T> T readRequestJson(Class<T> C) throws ErrorResponse {
        try {
            var body = request.readBody();
            return ctx.gson().fromJson(body, C);
        } catch (MalformedRequestException e) {
            throw new ErrorResponse("badRequest", e.getMessage());
        } catch (IOException e) {
            throw new ErrorResponse("internal", "Failed to read request body");
        } catch (JsonSyntaxException e) {
            throw new ErrorResponse("internal", "Failed to parse JSON request body");
        }
    }

    public abstract void run() throws ResponseWriteException, SQLException;
}
