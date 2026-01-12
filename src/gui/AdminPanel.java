package gui;

import log.LogRebuilder;
import service.AdminService;
import service.AnalyticsService;
import service.UserService;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Admin panel for viewing transaction log, rebuilding storage, and viewing analytics.
 */
public class AdminPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JTextArea logTextArea;
    private JButton refreshLogButton;
    private JButton rebuildButton;
    private JTextArea rebuildStatusArea;
    private JTextArea analyticsTextArea;
    private JButton refreshAnalyticsButton;
    
    private AdminService adminService;
    private UserService userService;
    private AnalyticsService analyticsService;
    
    /**
     * Constructs a new AdminPanel.
     * 
     * @param adminService the admin service
     * @param userService the user service
     */
    public AdminPanel(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
        this.analyticsService = new AnalyticsService();
        
        if (!adminService.isAdmin()) {
            JOptionPane.showMessageDialog(this, 
                "Access denied. Admin privileges required.", 
                "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        initializeComponents();
        layoutComponents();
        attachEventHandlers();
    }
    
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        
        // Transaction log tab
        logTextArea = new JTextArea(20, 80);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        refreshLogButton = new JButton("Refresh Log");
        
        // Rebuild tab
        rebuildButton = new JButton("Rebuild Storage from Log");
        rebuildStatusArea = new JTextArea(10, 80);
        rebuildStatusArea.setEditable(false);
        rebuildStatusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Analytics tab
        analyticsTextArea = new JTextArea(20, 80);
        analyticsTextArea.setEditable(false);
        analyticsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        refreshAnalyticsButton = new JButton("Refresh Analytics");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Transaction Log tab
        JPanel logPanel = new JPanel(new BorderLayout());
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logButtonPanel.add(refreshLogButton);
        logPanel.add(logButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Transaction Log", logPanel);
        
        // Rebuild tab
        JPanel rebuildPanel = new JPanel(new BorderLayout());
        JScrollPane rebuildScrollPane = new JScrollPane(rebuildStatusArea);
        rebuildPanel.add(rebuildScrollPane, BorderLayout.CENTER);
        JPanel rebuildButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rebuildButtonPanel.add(rebuildButton);
        rebuildPanel.add(rebuildButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Rebuild Storage", rebuildPanel);
        
        // Analytics tab
        JPanel analyticsPanel = new JPanel(new BorderLayout());
        JScrollPane analyticsScrollPane = new JScrollPane(analyticsTextArea);
        analyticsPanel.add(analyticsScrollPane, BorderLayout.CENTER);
        JPanel analyticsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        analyticsButtonPanel.add(refreshAnalyticsButton);
        analyticsPanel.add(analyticsButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Analytics", analyticsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void attachEventHandlers() {
        refreshLogButton.addActionListener(e -> refreshLog());
        rebuildButton.addActionListener(e -> rebuildStorage());
        refreshAnalyticsButton.addActionListener(e -> refreshAnalytics());
    }
    
    private void refreshLog() {
        try {
            String logContent = adminService.readTransactionLog();
            logTextArea.setText(logContent);
            logTextArea.setCaretPosition(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error reading transaction log: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void rebuildStorage() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "This will clear all current storage and rebuild from transaction log.\n" +
            "Are you sure you want to continue?", 
            "Rebuild Storage", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                LogRebuilder.RebuildResult result = adminService.rebuildStorage();
                rebuildStatusArea.setText(result.toString() + "\n\nRebuild completed successfully.");
                JOptionPane.showMessageDialog(this, 
                    "Storage rebuilt successfully!\n" + result.toString(), 
                    "Rebuild Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                String errorMsg = "Error rebuilding storage: " + e.getMessage();
                rebuildStatusArea.setText(errorMsg);
                JOptionPane.showMessageDialog(this, errorMsg, 
                    "Rebuild Error", JOptionPane.ERROR_MESSAGE);
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void refreshAnalytics() {
        StringBuilder analytics = new StringBuilder();
        analytics.append("=== Search History Analytics ===\n\n");
        
        // Top search queries
        analytics.append("Top 10 Search Queries:\n");
        analytics.append("------------------------\n");
        List<AnalyticsService.SearchQueryStats> topQueries = 
            analyticsService.getTopSearchQueries(10);
        if (topQueries.isEmpty()) {
            analytics.append("No search queries found.\n");
        } else {
            int rank = 1;
            for (AnalyticsService.SearchQueryStats stats : topQueries) {
                analytics.append(String.format("%d. %s (%d times)\n", 
                    rank++, stats.getQuery(), stats.getCount()));
            }
        }
        
        analytics.append("\n");
        
        // Records per day
        analytics.append("Records Stored Per Day:\n");
        analytics.append("------------------------\n");
        Map<String, Integer> recordsPerDay = analyticsService.getRecordsPerDay();
        if (recordsPerDay.isEmpty()) {
            analytics.append("No records stored.\n");
        } else {
            recordsPerDay.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByKey().reversed())
                .forEach(entry -> {
                    analytics.append(String.format("%s: %d record(s)\n", 
                        entry.getKey(), entry.getValue()));
                });
        }
        
        analytics.append("\n");
        
        // Active users
        analytics.append("Active Users (Last 7 Days):\n");
        analytics.append("------------------------\n");
        int activeUsers = analyticsService.getActiveUsersCount(7);
        analytics.append(String.format("Distinct active users: %d\n", activeUsers));
        
        analyticsTextArea.setText(analytics.toString());
        analyticsTextArea.setCaretPosition(0);
    }
}

