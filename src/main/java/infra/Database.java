package infra;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static String file;

    public static void setFile(String file) {
        Database.file = file;
    }

    public static Connection getConnection() throws SQLException {

        var config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);

        return DriverManager.getConnection("jdbc:sqlite:" + file, config.toProperties());
    }
}
