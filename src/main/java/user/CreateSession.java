package user;

// TODO could be session = socket on which user listens for server updates

import infra.Database;
import infra.SharedContext;
import infra.request.*;

import java.sql.SQLException;

public class CreateSession extends RequestHandler {

    public CreateSession(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public record RequestBody(String username, String password) {}

    // returns user data missing in client
    public record ResponseBody(String token, long id, String fullname) {}

    public boolean tokenRequired() {
        return false;
    }

    public Response run() throws SQLException {

        try (var conn = Database.getConnection()) {

            var body = readJson(RequestBody.class);

            var sql =
                "SELECT id, fullname "+
                "FROM users "+
                "WHERE username = ? AND password = ?";
            var stmt = conn.prepareStatement(sql);
            stmt.setString(1, body.username);
            stmt.setString(2, body.password);

            var result = stmt.executeQuery();
            var hasResult = result.next();
            if (!hasResult) {
                throw new ErrorResponse("badRequest", MsgCode.INCORRECT_CREDENTIALS);
            }
            var id = result.getLong("id");
            var fullname = result.getString("fullname");
            var token = ctx.sessionManager().createSession(id);
            return responseFactory.json(new ResponseBody(token, id, fullname));
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }

    }
}
