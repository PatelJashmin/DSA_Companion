import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LogAttempt extends JFrame {

    private JTextField txtSearch;
    private JComboBox<String> comboQuestions;
    private JTextField txtDuration;
    private JComboBox<String> comboResult;
    private JTextField txtLogic;

    public LogAttempt() {
        setTitle("Record Practice Attempt");
        setSize(450, 400); // Taller to fit the search bar
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        mainPanel.add(new JLabel("Search Question:"));
        txtSearch = new JTextField();
        mainPanel.add(txtSearch);

        //Dropdown (Now updates dynamically)
        mainPanel.add(new JLabel("Select Question:"));
        comboQuestions = new JComboBox<>();
        loadQuestionsFromDatabase(""); // Load all initially
        mainPanel.add(comboQuestions);

        // Listen for typing in the search bar
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadQuestionsFromDatabase(txtSearch.getText());
            }
        });

        // Row 3: Duration
        mainPanel.add(new JLabel("Duration (minutes):"));
        txtDuration = new JTextField();
        mainPanel.add(txtDuration);

        // Row 4: Result
        mainPanel.add(new JLabel("Result:"));
        String[] results = {"Accepted", "Wrong Answer", "TLE", "Runtime Error", "Compilation Error"};
        comboResult = new JComboBox<>(results);
        mainPanel.add(comboResult);

        // Row 5: Logic/Notes
        mainPanel.add(new JLabel("Logic / Notes:"));
        txtLogic = new JTextField();
        mainPanel.add(txtLogic);

        // Row 6: Save Button
        mainPanel.add(new JLabel("")); // Spacer
        JButton btnSave = new JButton("Save Attempt");
        mainPanel.add(btnSave);

        btnSave.addActionListener(e -> saveAttempt());

        add(mainPanel);
    }

    // Now accepts a search filter parameter
    // Updated to filter by the current logged-in student
    private void loadQuestionsFromDatabase(String filter) {
        comboQuestions.removeAllItems(); 
        
        // NEW: Added "AND student_id = ?" to the query
        String sql = "SELECT id, title FROM QUESTION WHERE title LIKE ? AND student_id = ? ORDER BY title ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + filter + "%");
            pstmt.setInt(2, UserSession.currentStudentId); // Filters the list!
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String displayStr = rs.getInt("id") + " - " + rs.getString("title");
                comboQuestions.addItem(displayStr);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + ex.getMessage());
        }
    }

    private void saveAttempt() {
        String selectedQuestion = (String) comboQuestions.getSelectedItem();
        String durationStr = txtDuration.getText();
        String result = (String) comboResult.getSelectedItem();
        String logic = txtLogic.getText();

        if (selectedQuestion == null || durationStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields.");
            return;
        }

        int questionId = Integer.parseInt(selectedQuestion.split(" ")[0]);
        int duration = Integer.parseInt(durationStr);
        int studentId = UserSession.currentStudentId; 

        String insertAttemptSql = "INSERT INTO ATTEMPT (student_id, question_id, duration, result, logic) VALUES (?, ?, ?, ?, ?)";
        // NEW: Schedules Revision #1 for tomorrow. INSERT IGNORE prevents errors if you attempt the same question twice in one day.
        String insertRevisionSql = "INSERT IGNORE INTO REVISION (student_id, question_id, rev_no, due_on) VALUES (?, ?, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtAttempt = conn.prepareStatement(insertAttemptSql);
             PreparedStatement pstmtRev = conn.prepareStatement(insertRevisionSql)) {

            // 1. Save the Attempt
            pstmtAttempt.setInt(1, studentId);
            pstmtAttempt.setInt(2, questionId);
            pstmtAttempt.setInt(3, duration);
            pstmtAttempt.setString(4, result);
            pstmtAttempt.setString(5, logic);
            pstmtAttempt.executeUpdate();
            
            // 2. Schedule the Revision
            pstmtRev.setInt(1, studentId);
            pstmtRev.setInt(2, questionId);
            pstmtRev.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "✅ Attempt logged & Revision scheduled!");
            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving attempt: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}