import com.google.gson.*;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class RequestHandler {

    public abstract Object handle(Connection conn, Gson gson, JsonObject jsonRequest) throws SQLException;

    public boolean needsAuthorization() {
        // True by default, override when authorization is not required
        // When true, turns the 'token' property into an 'userId' property
        return true;
    }

    public Object handle(String jsonString) {

        JsonElement jsonRequestElem;
        try {
            jsonRequestElem = JsonParser.parseString(jsonString);
        } catch (JsonSyntaxException ex) {
            return MessageResponse.error("JSON syntax error in request body");
        }

        if (!jsonRequestElem.isJsonObject()) {
            return MessageResponse.error("Request body has to be a JSON object");
        }

        var jsonRequest = jsonRequestElem.getAsJsonObject();

        // TODO temporary, just to get something going
        if (needsAuthorization()) {
            if (!jsonRequest.has("token")) {
                return MessageResponse.error("Missing token");
            }

            var tokenElem = jsonRequest.get("token");
            if (!tokenElem.isJsonPrimitive() || !tokenElem.getAsJsonPrimitive().isString()) {
                return MessageResponse.error("Invalid token");
            }

            var token = tokenElem.getAsJsonPrimitive().getAsString();

            if (!SessionManager.INSTANCE.validateTokenSyntax(token)) {
                return MessageResponse.error("Invalid token");
            }

            var userId = SessionManager.INSTANCE.getUserId(token);

            if (userId == 0) {
                return MessageResponse.error("Token expired");
            }

            jsonRequest.remove("token");
            jsonRequest.addProperty("userId", userId);
        }

        var gsonb = new GsonBuilder();
        gsonb.registerTypeAdapterFactory(new RecordTypeAdapterFactory());
        var gson = gsonb.create();

        Connection conn = null;
        try {
            conn = Database.getConnection();

            return handle(conn, gson, jsonRequest);

        } catch (SQLException e) {
            e.printStackTrace();
            return MessageResponse.error("(Internal) Failed to fulfill request");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
