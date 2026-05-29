import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PendingRevisions extends JFrame {

    private JTable revisionTable;
    private DefaultTableModel tableModel;
    private JTextField txtQuestionId, txtRevNo;
    private JComboBox<Integer> comboScore;

    public PendingRevisions() {
        setTitle("Full Revision Schedule");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        String[] columns = {"Question ID", "Title", "Revision #", "Scheduled Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        revisionTable = new JTable(tableModel);
        revisionTable.setRowHeight(25);
        revisionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(revisionTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        actionPanel.add(new JLabel("Selected ID:"));
        txtQuestionId = new JTextField(4);
        txtQuestionId.setEditable(false);
        actionPanel.add(txtQuestionId);

        actionPanel.add(new JLabel("Rev #:"));
        txtRevNo = new JTextField(3);
        txtRevNo.setEditable(false);
        actionPanel.add(txtRevNo);

        actionPanel.add(new JLabel("Score (1-10):"));
        Integer[] scores = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        comboScore = new JComboBox<>(scores);
        actionPanel.add(comboScore);

        JButton btnComplete = new JButton("Mark Completed");
        actionPanel.add(btnComplete);
        add(actionPanel, BorderLayout.SOUTH);
        
        revisionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && revisionTable.getSelectedRow() != -1) {
                txtQuestionId.setText(tableModel.getValueAt(revisionTable.getSelectedRow(), 0).toString());
                txtRevNo.setText(tableModel.getValueAt(revisionTable.getSelectedRow(), 2).toString());
            }
        });

        btnComplete.addActionListener(e -> completeReview());

        loadAllRevisions();
    }

    private void loadAllRevisions() {
        tableModel.setRowCount(0);
        txtQuestionId.setText("");
        txtRevNo.setText("");

        String sql = "SELECT r.question_id, q.title, r.rev_no, r.due_on, " +
                     "CASE WHEN r.due_on <= CURRENT_DATE THEN 'OVERDUE / DUE TODAY' ELSE 'UPCOMING' END as status_msg " +
                     "FROM REVISION r " +
                     "JOIN QUESTION q ON r.question_id = q.id " +
                     "WHERE r.done_on IS NULL " +
                     "AND r.student_id = ? " +
                     "ORDER BY r.due_on ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, UserSession.currentStudentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("question_id"),
                        rs.getString("title"),
                        rs.getInt("rev_no"),
                        rs.getDate("due_on"),
                        rs.getString("status_msg")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading revisions: " + ex.getMessage());
        }
    }

    private void completeReview() {
        if (txtQuestionId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a question from the table first.");
            return;
        }

        int questionId = Integer.parseInt(txtQuestionId.getText());
        int revNo = Integer.parseInt(txtRevNo.getText());
        int score = (int) comboScore.getSelectedItem();
        int studentId = UserSession.currentStudentId;

        int daysToAdd;
        if (score == 10) {
            daysToAdd = 30; // Mastered: 1 month
        } else if (score >= 8) {
            daysToAdd = 21; // Confident: 3 weeks
        } else if (score >= 6) {
            daysToAdd = 7;  // Medium-Easy: 1 week
        } else if (score >= 4) {
            daysToAdd = 3;  // Medium-Hard: 3 days
        } else {
            daysToAdd = 1;  // Struggled: 1 day
        }
        
        int nextRevNo = revNo + 1;

        String markDoneSql = "UPDATE REVISION SET done_on = CURRENT_DATE, score = ? WHERE student_id = ? AND question_id = ? AND rev_no = ?";
        String scheduleNextSql = "INSERT INTO REVISION (student_id, question_id, rev_no, due_on) VALUES (?, ?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY))";
        
        // 2. Query to automatically log this revision to the Dashboard
        String logRevisionAttemptSql = "INSERT INTO ATTEMPT (student_id, question_id, duration, result, logic) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtDone = conn.prepareStatement(markDoneSql);
             PreparedStatement pstmtNext = conn.prepareStatement(scheduleNextSql);
             PreparedStatement pstmtAttempt = conn.prepareStatement(logRevisionAttemptSql)) {

            // Step A: Mark current revision as done
            pstmtDone.setInt(1, score);
            pstmtDone.setInt(2, studentId);
            pstmtDone.setInt(3, questionId);
            pstmtDone.setInt(4, revNo);
            pstmtDone.executeUpdate();

            // Step B: Schedule the next hop out into the future
            pstmtNext.setInt(1, studentId);
            pstmtNext.setInt(2, questionId);
            pstmtNext.setInt(3, nextRevNo);
            pstmtNext.setInt(4, daysToAdd);
            pstmtNext.executeUpdate();

            // Step C: Log it on the dashboard
            pstmtAttempt.setInt(1, studentId);
            pstmtAttempt.setInt(2, questionId);
            pstmtAttempt.setInt(3, 15); // Defaulting to 15 mins for a quick revision
            pstmtAttempt.setString(4, (score >= 8) ? "Mastered" : "Reviewed");
            pstmtAttempt.setString(5, "Spaced Repetition Review (Score: " + score + "/10)");
            pstmtAttempt.executeUpdate();

            // 3. Format the output message to sound natural for the presentation
            String timeText;
            if (daysToAdd == 30) timeText = "1 month";
            else if (daysToAdd == 21) timeText = "3 weeks";
            else if (daysToAdd == 7) timeText = "1 week";
            else timeText = daysToAdd + (daysToAdd == 1 ? " day" : " days");

            JOptionPane.showMessageDialog(this, "✅ Review logged! Next revision scheduled in " + timeText + ".");
            loadAllRevisions(); 

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving review: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}