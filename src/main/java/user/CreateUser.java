package user;

import infra.*;

import java.sql.SQLException;

public class CreateUser extends RequestHandler {

    public CreateUser(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record RequestBody(String username, String password, String fullname) {}

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
                throw new ErrorResponse("badRequest", MsgCode.USERNAME_IN_USE);
            }

            var createStmt = conn.prepareStatement("INSERT INTO users (username, password, fullname) VALUES (?, ?, ?)");
            createStmt.setString(1, body.username());
            createStmt.setString(2, body.password());
            createStmt.setString(3, body.fullname());
            var rowCount = createStmt.executeUpdate();
            if (rowCount == 0) {
                throw new ErrorResponse("internal", MsgCode.FAILED_TO_CREATE_USER);
            }

            conn.commit();
            return responseFactory.json(MessageCodeBody.from(MsgCode.USER_CREATED_SUCCESSFULLY));

        } catch (ErrorResponse e) {
            conn.rollback();
            return responseFactory.err(e);
        } finally {
            conn.close();
        }
    }
}
