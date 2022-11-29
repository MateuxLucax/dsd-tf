package user;

import infra.Database;
import infra.SharedContext;
import infra.request.*;

import java.sql.SQLException;
import java.sql.Timestamp;

public class Whoami extends RequestHandler {

    public Whoami(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record UserData(String username, String fullname, Timestamp createdAt, Timestamp updatedAt) {}

    @Override
    public Response run() throws SQLException {

        var token = request.headers().get("token");
        var id = ctx.sessionManager().getSessionData(token).getUserId();

        try (var conn = Database.getConnection()) {
            var stmt = conn.prepareStatement("SELECT username, fullname, created_at, updated_at FROM users WHERE id = ?");
            stmt.setLong(1, id);
            var result = stmt.executeQuery();
            if (!result.next()) {

                // TODO when user is deleted, also close any associated sessions in the session manager so this doesn't happen
                //  which btw should be an atomic operation
                System.err.println("INTERNAL ERROR: ID retrieved from session manager does not exist");

                throw new ErrorResponse("internal", MsgCode.INTERNAL);
            }

            var username = result.getString("username");
            var fullname = result.getString("fullname");
            var createdAt = result.getTimestamp("created_at");
            var updatedAT = result.getTimestamp("updated_at");

            var body = new UserData(username, fullname, createdAt, updatedAT);
            return responseFactory.json(body);

        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
