import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.SQLException;

public class Server {

    public static void main(String[] args) throws SQLException {
        var gson = new Gson();
        var reqString = "{\"operation\": \"CREATE_USER\", \"username\": \"admin\", \"password\": \"123\"}";
        var req = JsonParser.parseString(reqString).getAsJsonObject();
        var res = gson.toJson(createUser(req));
        System.out.println(res);
    }

    private record CreateUserResponse(long id) {}

    public static Object createUser(JsonObject req) throws SQLException {
        // TODO exceptions for rollback
        var username = req.getAsJsonPrimitive("username").getAsString();
        var password = req.getAsJsonPrimitive("password").getAsString();

        var conn = Database.getConnection();
        conn.setAutoCommit(false);

        var existsStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
        existsStmt.setString(1, username);
        var existsResult = existsStmt.executeQuery();
        var alreadyExists = existsResult.next();
        if (alreadyExists) {
            return MessageResponse.error("User already exists");
        }

        /*
        var createStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?) RETURNING id");
        createStmt.setString(1, username);
        createStmt.setString(2, password);
        var ok = createStmt.execute();
        if (!ok) { return MessageResponse.error("Failed to execute query"); }
        var idResult = createStmt.getResultSet();
        if (!idResult.next()) { return MessageResponse.error("Failed to create user"); }
        var id = idResult.getLong("id");
        return new CreateUserResponse(id);
         */


        var createStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
        createStmt.setString(1, username);
        createStmt.setString(2, password);
        var created = 1 == createStmt.executeUpdate();
        if (!created) {
            return MessageResponse.error("Failed to create user, try again later");
        }

        // sqlite has RETURNING, but it doesn't work with jdbc...
        // and the sqlite driver doesn't support PreparedStatement.RETURN_GENERATED_KEYS...
        // so we have to do another query to get the id
        // I think we can use max(id) without worrying about concurrent inserts because we're in a transaction
        var idStmt = conn.prepareStatement("SELECT MAX(id) AS id FROM users");
        var idResult = idStmt.executeQuery();
        var gotId = idResult.next();
        if (!gotId) {
            conn.rollback();
            return MessageResponse.error("Failed to retrieve ID of created user, try again later");
        }
        var id = idResult.getLong("id");
        conn.commit();
        return new CreateUserResponse(id);
    }
}
