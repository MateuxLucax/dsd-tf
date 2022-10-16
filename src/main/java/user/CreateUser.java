package user;

import infra.*;

import java.sql.SQLException;

public class CreateUser extends RequestHandler {

    public CreateUser(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record RequestBody(String username, String password) {}

    private record ResponseBody(long id) {}

    public boolean tokenRequired() {
        return false;
    }

    public Response run() throws SQLException {

        var conn = Database.getConnection();
        conn.setAutoCommit(false);

        try {
            var body = readJson(RequestBody.class);

            var existsStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            existsStmt.setString(1, body.username());
            var existsRes = existsStmt.executeQuery();
            if (existsRes.next()) {
                throw new ErrorResponse("badRequest", ErrCode.USERNAME_IN_USE);
            }

            var createStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            createStmt.setString(1, body.username());
            createStmt.setString(2, body.password());
            var rowCount = createStmt.executeUpdate();
            if (rowCount == 0) {
                throw new ErrorResponse("internal", ErrCode.FAILED_TO_CREATE_USER);
            }

            var idStmt = conn.prepareStatement("SELECT MAX(id) AS id FROM users");
            var idRes = idStmt.executeQuery();
            if (!idRes.next()) {
                throw new ErrorResponse("internal", ErrCode.FAILED_TO_CREATE_USER);
            }
            var id = idRes.getLong(1);

            conn.commit();
            return responseFactory.json(new ResponseBody(id));

        } catch (ErrorResponse e) {
            conn.rollback();
            return responseFactory.err(e);
        } finally {
            conn.close();
        }
    }
}
