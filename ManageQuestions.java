import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ManageQuestions extends JFrame {

    private JTextField txtTitle, txtLink;
    private JComboBox<String> comboLevel, comboSource, comboTopic;

    public ManageQuestions() {
        setTitle("Add a New Problem");
        setSize(450, 400); // Made slightly taller for the new row
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 15, 15)); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25)); 

        mainPanel.add(new JLabel("Question Title:"));
        txtTitle = new JTextField();
        mainPanel.add(txtTitle);

        mainPanel.add(new JLabel("Difficulty:"));
        String[] levels = {"Easy", "Medium", "Hard"};
        comboLevel = new JComboBox<>(levels);
        mainPanel.add(comboLevel);

        mainPanel.add(new JLabel("Source:"));
        String[] sources = {"LeetCode", "GeeksForGeeks", "Codeforces", "CodeChef", "HackerRank", "Other"};
        comboSource = new JComboBox<>(sources);
        mainPanel.add(comboSource);

        mainPanel.add(new JLabel("Topic:"));
        comboTopic = new JComboBox<>();
        loadTopicsFromDatabase();
        mainPanel.add(comboTopic);

        mainPanel.add(new JLabel("Link (Optional):"));
        txtLink = new JTextField();
        mainPanel.add(txtLink);

        mainPanel.add(new JLabel("")); // Empty space
        JButton btnSave = new JButton("Save to Database");
        mainPanel.add(btnSave);

        btnSave.addActionListener(e -> saveQuestion());

        add(mainPanel);
    }

    // Fetches topics we previously inserted into the TOPIC table
    private void loadTopicsFromDatabase() {
        String sql = "SELECT name FROM TOPIC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comboTopic.addItem(rs.getString("name"));
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading topics: " + ex.getMessage());
        }
    }

    private void saveQuestion() {
        String title = txtTitle.getText();
        String level = (String) comboLevel.getSelectedItem();
        String source = (String) comboSource.getSelectedItem();
        String topic = (String) comboTopic.getSelectedItem();
        String link = txtLink.getText();

        if (title.isEmpty() || topic == null) {
            JOptionPane.showMessageDialog(this, "Title and Topic are required!");
            return;
        }

        String insertQuestionSQL = "INSERT INTO QUESTION (title, level, source, link, student_id) VALUES (?, ?, ?, ?, ?)";
        String insertMapSQL = "INSERT INTO CATEGORY_MAP (topic_name, question_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtQ = conn.prepareStatement(insertQuestionSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmtQ.setString(1, title);
            pstmtQ.setString(2, level);
            pstmtQ.setString(3, source);
            pstmtQ.setString(4, link);
            pstmtQ.setInt(5, UserSession.currentStudentId);
            pstmtQ.executeUpdate();

            ResultSet generatedKeys = pstmtQ.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newQuestionId = generatedKeys.getInt(1);

                try (PreparedStatement pstmtMap = conn.prepareStatement(insertMapSQL)) {
                    pstmtMap.setString(1, topic);
                    pstmtMap.setInt(2, newQuestionId);
                    pstmtMap.executeUpdate();
                }
            }
            
            JOptionPane.showMessageDialog(this, "✅ Problem added to your personal bank!");
            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}