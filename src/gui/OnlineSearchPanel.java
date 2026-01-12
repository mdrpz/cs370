package gui;

import auth.SessionManager;
import log.TransactionLogger;
import model.DataRecord;
import model.User;
import service.UserService;
import web.HtmlFetcher;
import web.HtmlParser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Panel for online search functionality.
 * Allows users to search websites and optionally store results.
 */
public class OnlineSearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton storeButton;
    private JLabel statusLabel;
    
    private UserService userService;
    private SessionManager sessionManager;
    private HtmlFetcher htmlFetcher;
    private HtmlParser htmlParser;
    private TransactionLogger logger;
    private List<DataRecord> currentResults;
    
    /**
     * Constructs a new OnlineSearchPanel.
     * 
     * @param userService the user service
     * @param sessionManager the session manager
     */
    public OnlineSearchPanel(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
        this.htmlFetcher = new HtmlFetcher();
        this.htmlParser = new HtmlParser();
        this.logger = TransactionLogger.getInstance();
        this.currentResults = new java.util.ArrayList<>();
        
        initializeComponents();
        layoutComponents();
        attachEventHandlers();
    }
    
    private void initializeComponents() {
        searchField = new JTextField(30);
        searchButton = new JButton("Search Online");
        storeButton = new JButton("Store Results");
        statusLabel = new JLabel("Enter a search query and click Search Online");
        
        // Results table
        String[] columnNames = {"ID", "Title", "Author", "Source URL"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Disable store button for guests
        if (!sessionManager.canStoreData()) {
            storeButton.setEnabled(false);
        }
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Query:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(storeButton);
        add(searchPanel, BorderLayout.NORTH);
        
        // Results panel
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Status label
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void attachEventHandlers() {
        searchButton.addActionListener(e -> performSearch());
        storeButton.addActionListener(e -> storeResults());
        searchField.addActionListener(e -> performSearch());
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search query.", 
                                        "Search Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Log search
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            logger.logSearchOnline(currentUser, query);
        }
        
        // Disable button during search
        searchButton.setEnabled(false);
        statusLabel.setText("Searching...");
        
        // Perform search in background thread
        new Thread(() -> {
            try {
                // Build URL
                String url = htmlFetcher.buildSearchUrl(query);
                
                // Fetch HTML
                String htmlFile = htmlFetcher.fetch(url);
                String html = htmlFetcher.readFromFile(htmlFile);
                
                // Parse HTML
                String username = sessionManager.getCurrentUsername();
                currentResults = htmlParser.parseSearchResults(html, username);
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    updateResultsTable(currentResults);
                    
                    if (currentResults.isEmpty()) {
                        statusLabel.setText("No results found. Try a different query.");
                    } else {
                        statusLabel.setText("Found " + currentResults.size() + " results.");
                    }
                    searchButton.setEnabled(true);
                });
                
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Error performing search: " + e.getMessage(), 
                        "Search Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Search failed. Please try again.");
                    searchButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void updateResultsTable(List<DataRecord> results) {
        tableModel.setRowCount(0);
        currentResults = results;
        
        for (DataRecord record : results) {
            Object[] row = {
                record.getId(),
                record.getTitle(),
                record.getAuthor(),
                record.getSourceUrl()
            };
            tableModel.addRow(row);
        }
    }
    
    private void storeResults() {
        if (!sessionManager.canStoreData()) {
            JOptionPane.showMessageDialog(this, 
                "Guest users cannot store results. Please login as a registered user.", 
                "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (currentResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No results to store. Please perform a search first.", 
                "No Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int stored = userService.storeSearchResults(currentResults);
        JOptionPane.showMessageDialog(this, 
            "Stored " + stored + " record(s) successfully.", 
            "Storage Success", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("Stored " + stored + " record(s).");
    }
}

