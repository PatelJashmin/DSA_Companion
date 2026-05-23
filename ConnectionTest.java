import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/dsa_tracker";
        String user = "root"; 
        String password = "your_password";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connection Successful!");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM STUDENT");
            
            System.out.println("Registered Students:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("name"));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Connection Failed!");
            e.printStackTrace();
        }
    }
}