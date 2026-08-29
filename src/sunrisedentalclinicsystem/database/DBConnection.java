package sunrisedentalclinicsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author yasit
 */
public class DBConnection {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "sunrise_dental_clinic_system_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final String URL = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF8",
            HOST, PORT, DATABASE
    );


    // Connect to the MySQL database.
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found!");
            System.err.println("Please ensure 'mysql-connector-java-9.7.0.jar' is in the project libraries.");
            throw new SQLException("Unable to load MySQL JDBC Driver: " + e.getMessage(), e);

        } catch (SQLException e) {
            System.err.println("Error: Database connection failed!");
            System.err.println("Please verify:");
            System.err.println("  1. MySQL server is running on " + HOST + ":" + PORT);
            System.err.println("  2. Database '" + DATABASE + "' exists");
            System.err.println("  3. Username and password are correct");
            System.err.println("Connection URL: " + URL);
            throw e;
        }
    }


    // Test the database connection.
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        System.out.println("URL: " + URL);

        try {
            Connection connection = getConnection();
            System.out.println("Connected Successfully!");
            connection.close();
            System.out.println("Connection closed.");
        } catch (SQLException e) {
            System.out.println("Connection failed. Check error messages above.");
        }
    }
}
