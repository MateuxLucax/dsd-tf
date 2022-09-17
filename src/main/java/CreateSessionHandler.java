import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;

public class CreateSessionHandler extends RequestHandler {

    public boolean needsAuthorization() {
        return false;
    }

    public record RequestData(String username, String password) {}

    public record ResponseData(String token) {}

    public Object handle(Connection conn, Gson gson, JsonObject jsonRequest) throws SQLException {
         try {
             var req = gson.fromJson(jsonRequest, RequestData.class);

             var stmt = conn.prepareStatement("SELECT id, password FROM users WHERE username = ?");
             stmt.setString(1, req.username);
             var result = stmt.executeQuery();

             if (!result.next()) {
                 return MessageResponse.error("No user named " + req.username);
             }

             var id = result.getLong("id");
             var storedPassword = result.getString("password");

             if (!storedPassword.equals(req.password)) {
                 return MessageResponse.error("Incorrect password");
             }

             var token = SessionManager.INSTANCE.createTokenFor(id);
             return new ResponseData(token);

         } catch (Exception e) {
             return MessageResponse.error(e.getMessage());
         }
    }
}
