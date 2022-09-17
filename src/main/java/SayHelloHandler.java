// just to test session manager

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;

public class SayHelloHandler extends RequestHandler {

    public Object handle(Connection conn, Gson gson, JsonObject jsonRequest) throws SQLException {
        return MessageResponse.ok("Hello! :^) Here is the request I got: " + jsonRequest);
    }
}
