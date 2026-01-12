package gui;

import auth.SessionManager;
import model.DataRecord;
import service.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for offline search functionality.
 * Allows users to query stored data by time range or title keyword.
 */
public class OfflineSearchPanel extends JPanel {
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton timeRangeButton;
    private JTextField titleSearchField;
    private JButton titleSearchButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    
    private UserService userService;
    private SessionManager sessionManager;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Constructs a new OfflineSearchPanel.
     * 
     * @param userService the user service
     * @param sessionManager the session manager
     */
    public OfflineSearchPanel(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
        
        initializeComponents();
        layoutComponents();
        attachEventHandlers();
    }
    
    private void initializeComponents() {
        fromDateField = new JTextField(12);
        toDateField = new JTextField(12);
        timeRangeButton = new JButton("Search by Time Range");
        titleSearchField = new JTextField(20);
        titleSearchButton = new JButton("Search by Title");
        statusLabel = new JLabel("Enter search criteria and click a search button");
        
        // Set placeholder text
        fromDateField.setToolTipText("Format: YYYY-MM-DD");
        toDateField.setToolTipText("Format: YYYY-MM-DD");
        
        // Results table
        String[] columnNames = {"ID", "Title", "Author", "Fetched At", "Fetched By"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search criteria panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
        
        // Time range search
        JPanel timeRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeRangePanel.add(new JLabel("From (YYYY-MM-DD):"));
        timeRangePanel.add(fromDateField);
        timeRangePanel.add(new JLabel("To (YYYY-MM-DD):"));
        timeRangePanel.add(toDateField);
        timeRangePanel.add(timeRangeButton);
        searchPanel.add(timeRangePanel);
        
        // Title search
        JPanel titleSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleSearchPanel.add(new JLabel("Title contains:"));
        titleSearchPanel.add(titleSearchField);
        titleSearchPanel.add(titleSearchButton);
        searchPanel.add(titleSearchPanel);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Results panel
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Status label
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void attachEventHandlers() {
        timeRangeButton.addActionListener(e -> performTimeRangeSearch());
        titleSearchButton.addActionListener(e -> performTitleSearch());
    }
    
    private void performTimeRangeSearch() {
        String fromStr = fromDateField.getText().trim();
        String toStr = toDateField.getText().trim();
        
        if (fromStr.isEmpty() || toStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both from and to dates.", 
                "Search Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Date fromDate = DATE_FORMAT.parse(fromStr);
            Date toDate = DATE_FORMAT.parse(toStr);
            
            // Convert to start and end of day timestamps
            long start = fromDate.getTime();
            long end = toDate.getTime() + (24 * 60 * 60 * 1000) - 1; // End of day
            
            List<DataRecord> results = userService.queryByTimeRange(start, end);
            updateResultsTable(results);
            
            if (results.isEmpty()) {
                statusLabel.setText("No records found in the specified time range.");
            } else {
                statusLabel.setText("Found " + results.size() + " record(s).");
            }
            
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid date format. Please use YYYY-MM-DD.", 
                "Date Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performTitleSearch() {
        String keyword = titleSearchField.getText().trim();
        
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a search keyword.", 
                "Search Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<DataRecord> results = userService.queryByTitle(keyword);
        updateResultsTable(results);
        
        if (results.isEmpty()) {
            statusLabel.setText("No records found matching '" + keyword + "'.");
        } else {
            statusLabel.setText("Found " + results.size() + " record(s) matching '" + keyword + "'.");
        }
    }
    
    private void updateResultsTable(List<DataRecord> results) {
        tableModel.setRowCount(0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (DataRecord record : results) {
            String fetchedAtStr = dateFormat.format(new Date(record.getFetchedAt()));
            Object[] row = {
                record.getId(),
                record.getTitle(),
                record.getAuthor(),
                fetchedAtStr,
                record.getFetchedByUser()
            };
            tableModel.addRow(row);
        }
    }
}

