import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public static Connection getConnection() {
        var config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:xet.db", config.toProperties());
        } catch (SQLException ignored) {}
        return conn;
    }
}
