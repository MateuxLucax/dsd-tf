import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import helpers.RecordTypeAdapterFactory;
import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class TestUtils {

    private static final String TEST_HOST = "localhost";
    private static final int    TEST_PORT = 8080;

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .create();

    public static TestResponse jsonRequest(String operation, Object body) throws IOException {
        return jsonRequest(operation, body, new String[]{});
    }

    public static TestResponse jsonRequest(String operation, Object body, String[] headers) throws IOException {
        var json = TestUtils.GSON.toJson(body);
        var bytes = json.getBytes();
        return makeRequest(operation, bytes, headers);
    }

    public static TestResponse makeRequest(String operation, byte[] body) throws IOException {
        return jsonRequest(operation, body, new String[]{});
    }

    public static TestResponse makeRequest(String operation, byte[] body, String[] headers) throws IOException {
        var bef = "";  // stuff before the body
        bef += "operation " + operation + "\n";
        bef += "body-size " + body.length + "\n";
        for (var header : headers) {
            bef += header + "\n";
        }
        bef += "\n";

        var sock = new Socket(TEST_HOST, TEST_PORT);
        var out = sock.getOutputStream();
        out.write(bef.getBytes());
        out.write(body);

        var responseHeaders = new HashMap<String, String>();
        var in = sock.getInputStream();
        var buf = new byte[1024];
        var len = 0;
        while (true) {
            var c = in.read();

            if (c != -1 && c != '\n' && len < buf.length) {
                buf[len++] = (byte) c;
            }
            else {
                var line = new String(buf, 0, len);

                // Blank like marking the end of the header section
                if (line.isBlank()) break;

                var split = line.indexOf(" ");

                var key = line.substring(0, split).trim().toLowerCase();
                var val = line.substring(split+1).trim();

                responseHeaders.put(key, val);

                // End of request or prepare for next line
                if (c == -1) break;
                len = 0;
            }
        }

        var responseBodySize = Integer.parseInt(responseHeaders.get("body-size"));
        var responseBody = new byte[responseBodySize];
        in.readNBytes(responseBody, 0, responseBodySize);

        return new TestResponse(responseHeaders, responseBody);
    }

    public static Connection testConnection() throws SQLException {
        var config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        return DriverManager.getConnection("jdbc:sqlite:tests.db", config.toProperties());
    }

    public static void clearTestsDatabase() throws SQLException {

        // far from being the most elegant solution, but:
        // it's the easiest to implement
        // it's hard to maintain but we're not gonna do that anyways
        // and we couldn't just delete because it doesn't reset the autoincrements

        var conn = testConnection();
        var sqls = new String[]{
            "drop table if exists users;",
            "create table users ( id          integer not null primary key autoincrement, username    text    unique not null, fullname    text    not null, password    text    not null, avatar_path text    null, created_at  timestamp default current_timestamp not null, updated_at  timestamp null, check (updated_at is null or updated_at > created_at) );",
            "drop table if exists friends;" +
            "create table friends ( your_id    integer not null, their_id   integer not null, created_at timestamp default current_timestamp not null, primary key (your_id, their_id), foreign key (your_id) references users(id), foreign key (their_id) references users(id) );",
            "drop table if exists friend_requests;",
            "create table friend_requests ( sender_id   integer not null, receiver_id integer not null, created_at  timestamp default current_timestamp not null, primary key (sender_id, receiver_id), foreign key (sender_id) references users(id), foreign key (receiver_id) references users(id) );",
            "create index idx_friend_requests_receiver on friend_requests(receiver_id, sender_id);",
            "drop table if exists messages;",
            "create table messages ( sender_id integer not null, receiver_id integer not null, id integer not null primary key autoincrement, sent_at timestamp not null default current_timestamp, text_contents text null, file_reference text null, check (text_contents is not null or file_reference is not null), check (text_contents is null or file_reference is null), foreign key (sender_id) references users (id), foreign key (receiver_id) references users (id) );",
        };

        for (var sql : sqls) {
            conn.prepareStatement(sql).executeUpdate();
        }
    }
}




