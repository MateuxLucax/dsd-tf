package user;

import infra.*;

import java.sql.SQLException;

public class CreateUser extends RequestHandler {

    public CreateUser(Request request, ResponseWriter response, SharedContext ctx) {
        super(request, response, ctx);
    }

    private record RequestBody(String username, String password) {}

    private record ResponseBody(long id) {}

    public boolean tokenRequired() {
        return false;
    }

    public void run() throws ResponseWriteException, SQLException {

        var gson = ctx.gson();
        var conn = Database.getConnection();
        conn.setAutoCommit(false);

        try {

            // Already throws ErrorResponse, so we don't need to worry about it failing
            var body = readJson(RequestBody.class);

            var existsStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            existsStmt.setString(1, body.username());
            var existsRes = existsStmt.executeQuery();
            if (existsRes.next()) {
                throw new ErrorResponse("badRequest", "User already exists");
            }

            var createStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            createStmt.setString(1, body.username());
            createStmt.setString(2, body.password());
            var rowCount = createStmt.executeUpdate();
            if (rowCount == 0) {
                throw new ErrorResponse("internal", "Failed to create the user");
            }

            var idStmt = conn.prepareStatement("SELECT MAX(id) AS id FROM users");
            var idRes = idStmt.executeQuery();
            if (!idRes.next()) {
                throw new ErrorResponse("internal", "Failed to retrieve created user id");
            }
            var id = idRes.getLong(1);

            conn.commit();

            var respBody = new ResponseBody(id);
            response.writeToBody(gson.toJson(respBody));

        } catch (ErrorResponse e) {
            response.writeError(e.getKind(), gson.toJson(e.toBody()));
            conn.rollback();
        } finally {
            conn.close();
        }
    }
}
