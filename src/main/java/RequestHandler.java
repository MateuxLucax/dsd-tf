import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.sql.SQLException;
import java.sql.Connection;

public abstract class RequestHandler {

    public Object handle(JsonElement jsonRequest) {
        var gsonb = new GsonBuilder();
        gsonb.registerTypeAdapterFactory(new RecordTypeAdapterFactory());
        var gson = gsonb.create();

        Connection conn = null;
        try {
            conn = Database.getConnection();
            var res = handle(conn, gson, jsonRequest);
            return gson.toJson(res);
        } catch (SQLException e) {
            e.printStackTrace();
            return MessageResponse.error("Failed to fulfill request");
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

    public abstract Object handle(Connection conn, Gson gson, JsonElement jsonRequest) throws SQLException;
}
