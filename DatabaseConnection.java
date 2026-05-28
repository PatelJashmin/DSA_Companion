import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/dsa_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password"; // Update this

    //built-in getConnection method is in DriverManager class.
    //and my getConnection method is in DatabaseConnection calss. Hence no conflict.  
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}