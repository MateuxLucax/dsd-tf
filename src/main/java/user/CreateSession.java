package user;

// TODO could be session = socket on which user listens for server updates

import infra.*;

import java.sql.SQLException;

public class CreateSession extends RequestHandler {

    public CreateSession(Request request, ResponseWriter response, SharedContext ctx) {
        super(request, response, ctx);
    }

    public record RequestBody(String username, String password) {}

    public record ResponseBody(String token) {}

    public boolean tokenRequired() {
        return false;
    }

    public void run() throws ResponseWriteException, SQLException {

        try (var conn = Database.getConnection()) {

            var body = readRequestJson(RequestBody.class);

            var stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, body.username);
            stmt.setString(2, body.password);
            var result = stmt.executeQuery();
            if (!result.next()) {
                throw new ErrorResponse("badRequest", "Incorrect username or password");
            }
            var id = result.getLong("id");

            var token = ctx.sessionManager().createSession(id);

            var responseBody = new ResponseBody(token);
            response.writeToBody(ctx.gson().toJson(responseBody));

        } catch (ErrorResponse e) {
            response.writeError(e.getKind(), ctx.gson().toJson(e.toBody()));
        }

    }
}
