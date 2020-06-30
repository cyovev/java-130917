package database;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    private final static String DATABASE_FILE = "resources/db.config";

    ///////////////////////////////////////////////////////////////////////////
    public static Connection getConnection() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String DB_URL = loadDBConfig();
            
            return DriverManager.getConnection(DB_URL);
        }
        catch (Exception e) {
            throw new Exception("Error connecting to the database: " + e.getMessage());
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    private static String loadDBConfig() throws FileNotFoundException, IOException {
        Properties settings = new Properties();
        settings.load(new FileReader(DATABASE_FILE));
        
        String HOST     = settings.getProperty("host"),
               PORT     = settings.getProperty("port"),
               DATABASE = settings.getProperty("database"),
               USERNAME = settings.getProperty("username"),
               PASSWORD = settings.getProperty("password");
        
        // apart from credentials, unicode and character encoding is also passed to the connection (UTF-8)
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=UTF-8", HOST, PORT, DATABASE, USERNAME, PASSWORD);
    }
}