import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class Dashboard extends JFrame {
    
    private JTable activityTable;
    private DefaultTableModel tableModel;

    public Dashboard() {
        setTitle("DSA Practice Companion - " + (UserSession.currentStudentName != null ? UserSession.currentStudentName : "Dashboard"));
        setSize(1000, 600); // Made slightly wider to fit all the new buttons!
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();

        headerPanel.setBackground(new Color(41, 128, 185));

        JLabel titleLabel = new JLabel("Welcome, " + (UserSession.currentStudentName != null ? UserSession.currentStudentName : "Student") + " | Activity Dashboard");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel (The Live Activity Table)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Define table columns
        String[] columns = {"Attempt ID", "Date", "Question", "Duration (mins)", "Result", "Link"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevents accidental typing in the table cells
            }
        };
        activityTable = new JTable(tableModel);
        activityTable.setRowHeight(25); 
        activityTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        // Enable Horizontal Scrolling and Set Widths
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        activityTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        activityTable.getColumnModel().getColumn(1).setPreferredWidth(140); // Date
        activityTable.getColumnModel().getColumn(2).setPreferredWidth(230); // Question
        activityTable.getColumnModel().getColumn(3).setPreferredWidth(110); // Duration
        activityTable.getColumnModel().getColumn(4).setPreferredWidth(140); // Result
        activityTable.getColumnModel().getColumn(5).setPreferredWidth(350); // Link

        // The JScrollPane will automatically provide a horizontal scrollbar
        JScrollPane scrollPane = new JScrollPane(activityTable, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Fetch data from database to fill the table immediately
        refreshTable();

        // 3. Bottom Navigation Panel
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Buttons
        JButton btnLogAttempt = new JButton("Record Attempt");
        JButton btnManageQuestions = new JButton("Add New Problem");
        JButton btnPendingRevisions = new JButton("Pending Revisions");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnLogout = new JButton("Logout");

        Dimension btnSize = new Dimension(150, 40);
        btnLogAttempt.setPreferredSize(btnSize);
        btnManageQuestions.setPreferredSize(btnSize);
        btnPendingRevisions.setPreferredSize(btnSize);
        
        btnDelete.setPreferredSize(btnSize);
        btnDelete.setBackground(new Color(231, 76, 60)); // Red UI for Delete
        btnDelete.setForeground(Color.WHITE);
        
        btnLogout.setPreferredSize(btnSize);
        btnLogout.setBackground(new Color(52, 73, 94));  // Dark UI for Logout
        btnLogout.setForeground(Color.WHITE);

        // 4. Button Actions
        btnManageQuestions.addActionListener(e -> {
            ManageQuestions mqWindow = new ManageQuestions();
            mqWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
            mqWindow.setVisible(true);
            mqWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    refreshTable();
                }
            });
        });

        btnLogAttempt.addActionListener(e -> {
            LogAttempt laWindow = new LogAttempt();
            laWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            laWindow.setVisible(true);
            laWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    refreshTable();
                }
            });
        });

        btnPendingRevisions.addActionListener(e -> {
            PendingRevisions prWindow = new PendingRevisions();
            prWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            prWindow.setVisible(true);
            // Refresh table in case a revision gets completed and logged
            prWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    refreshTable();
                }
            });
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = activityTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an attempt from the table to delete.");
                return;
            }
            
            int attemptId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this attempt?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM ATTEMPT WHERE id = ?")) {
                    pstmt.setInt(1, attemptId);
                    pstmt.executeUpdate();
                    refreshTable(); 
                    JOptionPane.showMessageDialog(this, "✅ Attempt deleted successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage());
                }
            }
        });

        btnLogout.addActionListener(e -> {
            UserSession.currentStudentId = 0;
            UserSession.currentStudentName = null;
            new Login().setVisible(true);
            this.dispose();
        });

        actionPanel.add(btnLogAttempt);
        actionPanel.add(btnManageQuestions);
        actionPanel.add(btnPendingRevisions);
        actionPanel.add(btnDelete);
        actionPanel.add(btnLogout);
        
        add(actionPanel, BorderLayout.SOUTH);
    }

    // Method to query the database and update the JTable
    private void refreshTable() {
        tableModel.setRowCount(0);

        // Updated SQL to fetch a.id so Delete knows what to target
        String sql = "SELECT a.id, DATE_FORMAT(a.attempted_on, '%Y-%m-%d %H:%i') as date, " +
                     "q.title, a.duration, a.result, q.link " +
                     "FROM ATTEMPT a " +
                     "JOIN QUESTION q ON a.question_id = q.id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY a.attempted_on DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, UserSession.currentStudentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String title = rs.getString("title");
                String duration = rs.getString("duration");
                String result = rs.getString("result");
                String link = rs.getString("link");

                // Add the ID and the link to the row
                tableModel.addRow(new Object[]{id, date, title, duration, result, link});
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading activity: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}