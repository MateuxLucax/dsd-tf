package user;

import infra.*;

import java.sql.SQLException;
import java.sql.Timestamp;

public class Whoami extends RequestHandler {

    public Whoami(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record UserData(String username, Timestamp createdAt, Timestamp updatedAt) {}

    @Override
    public Response run() throws SQLException {

        var token = request.headers().get("token");
        var id = ctx.sessionManager().getSessionData(token).getUserId();

        try (var conn = Database.getConnection()) {
            var stmt = conn.prepareStatement("SELECT username, created_at, updated_at FROM users WHERE id = ?");
            stmt.setLong(1, id);
            var result = stmt.executeQuery();
            if (!result.next()) {

                // TODO when user is deleted, also close any associated sessions in the session manager so this doesn't happen
                //  which btw should be an atomic operation
                System.err.println("INTERNAL ERROR: ID retrieved from session manager does not exist");

                throw new ErrorResponse("internal", ErrCode.INTERNAL);
            }

            var username = result.getString("username");
            var createdAt = result.getTimestamp("created_at");
            var updatedAT = result.getTimestamp("updated_at");

            var body = new UserData(username, createdAt, updatedAT);
            return responseFactory.json(body);

        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
