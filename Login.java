import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {

    private JTextField txtName;
    private JPasswordField txtPassword;

    public Login() {
        setTitle("DSA Tracker - Login");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //force this JFrame to be at the center of the screen
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        //keep a 20pxl gap between UI compoenets and border.
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Student Name:"));
        txtName = new JTextField();
        panel.add(txtName);

        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        JButton btnLogin = new JButton("Login");
        JButton btnAddStudent = new JButton("Add Student");
        
        panel.add(btnLogin);
        panel.add(btnAddStudent);

        add(panel);

        btnLogin.addActionListener(e -> {
            String name = txtName.getText();
            String pass = new String(txtPassword.getPassword());

            String sql = "SELECT id, name FROM STUDENT WHERE name = ? AND password = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                pstmt.setString(1, name);
                pstmt.setString(2, pass);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    UserSession.currentStudentId = rs.getInt("id");
                    UserSession.currentStudentName = rs.getString("name");
                    
                    JOptionPane.showMessageDialog(this, "Welcome, " + UserSession.currentStudentName + "!");
                    
                    //handover the flow to Dashboard. 
                    new Dashboard().setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        });

        btnAddStudent.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "Enter New Student Name:");
            if (newName == null || newName.trim().isEmpty()) return;
            
            String newEmail = JOptionPane.showInputDialog(this, "Enter New Student Email:");
            if (newEmail == null || newEmail.trim().isEmpty()) return;

            String newPass = JOptionPane.showInputDialog(this, "Enter New Password:");
            if (newPass == null || newPass.trim().isEmpty()) return;

            String sql = "INSERT INTO STUDENT (name, email, password) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, newEmail);
                pstmt.setString(3, newPass);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Student Added Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding student. Make sure the email is unique.");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}