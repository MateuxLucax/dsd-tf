import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.sql.Connection;
import java.sql.SQLException;

public class CreateUserHandler extends RequestHandler {

    public record RequestData(String username, String password) {}

    public record ResponseData(long id) {}

    public Object handle(Connection conn, Gson gson, JsonElement jsonRequest) throws SQLException {
        var req = gson.fromJson(jsonRequest, RequestData.class);
        try {
            conn.setAutoCommit(false);

            var existsStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            existsStmt.setString(1, req.username());
            var alreadyExists = existsStmt.executeQuery().next();
            if (alreadyExists) {
                throw new Exception("User already exists");
            }

            var createStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            createStmt.setString(1, req.username());
            createStmt.setString(2, req.password());
            var created = createStmt.executeUpdate() > 0;
            if (!created) {
                throw new Exception("Failed to create the user");
            }

            // sqlite has RETURNING but jdbc doesn't support it
            // the sqlite driver for jdbc also doesn't support PreparedStatement.RETURN_GENERATED_KEYS

            // I think we can do MAX(id) without worrying about some interleaving concurrent insertion because we're inside a transaction, but I'm not sure
            // if that's not the case just change it to SELECT id FROM users WHERE username = {req.username()}
            var idStmt = conn.prepareStatement("SELECT MAX(id) AS id FROM users");
            var idResult = idStmt.executeQuery();
            var gotId = idResult.next();
            if (!gotId) {
                throw new Exception("Failed to retrieve created user ID");
            }

            conn.commit();

            return new ResponseData(idResult.getLong("id"));
        } catch (Exception e) {
            if (conn.getAutoCommit()) {
                conn.rollback(); // SQLException thrown
            }
            return MessageResponse.error(e.getMessage());
        }
    }
}
