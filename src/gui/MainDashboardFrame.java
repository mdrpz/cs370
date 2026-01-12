package gui;

import auth.SessionManager;
import service.AdminService;
import service.UserService;
import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard window after login.
 * Provides access to online search, offline search, favorites, and admin panel.
 */
public class MainDashboardFrame extends JFrame {
    private JButton onlineSearchButton;
    private JButton offlineSearchButton;
    private JButton favoritesButton;
    private JButton adminButton;
    private JButton logoutButton;
    
    private SessionManager sessionManager;
    private UserService userService;
    private AdminService adminService;
    private Runnable onLogout;
    
    /**
     * Constructs a new MainDashboardFrame.
     * 
     * @param sessionManager the session manager
     * @param userService the user service
     * @param adminService the admin service
     * @param onLogout callback when user logs out
     */
    public MainDashboardFrame(SessionManager sessionManager, UserService userService, 
                             AdminService adminService, Runnable onLogout) {
        this.sessionManager = sessionManager;
        this.userService = userService;
        this.adminService = adminService;
        this.onLogout = onLogout;
        
        initializeComponents();
        layoutComponents();
        attachEventHandlers();
        
        updateUI();
        
        setTitle("Book Search System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        onlineSearchButton = new JButton("Online Search");
        offlineSearchButton = new JButton("Offline Search");
        favoritesButton = new JButton("My Favorites");
        adminButton = new JButton("Admin Panel");
        logoutButton = new JButton("Logout");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Welcome panel
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String username = sessionManager.getCurrentUsername();
        String role = sessionManager.getCurrentUser() != null ? 
                     sessionManager.getCurrentUser().getRole().name() : "GUEST";
        JLabel welcomeLabel = new JLabel("Welcome, " + username + " (" + role + ")");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        welcomePanel.add(welcomeLabel);
        add(welcomePanel, BorderLayout.NORTH);
        
        // Main buttons panel
        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        onlineSearchButton.setPreferredSize(new Dimension(200, 50));
        offlineSearchButton.setPreferredSize(new Dimension(200, 50));
        favoritesButton.setPreferredSize(new Dimension(200, 50));
        
        mainPanel.add(onlineSearchButton);
        mainPanel.add(offlineSearchButton);
        mainPanel.add(favoritesButton);
        
        // Admin button (only for admins)
        if (sessionManager.isAdmin()) {
            adminButton.setPreferredSize(new Dimension(200, 50));
            mainPanel.add(adminButton);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Logout button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        add(logoutPanel, BorderLayout.SOUTH);
    }
    
    private void attachEventHandlers() {
        onlineSearchButton.addActionListener(e -> openOnlineSearch());
        offlineSearchButton.addActionListener(e -> openOfflineSearch());
        favoritesButton.addActionListener(e -> openFavorites());
        adminButton.addActionListener(e -> openAdminPanel());
        logoutButton.addActionListener(e -> performLogout());
    }
    
    private void updateUI() {
        // Disable favorites for guests
        if (sessionManager.getCurrentUser() != null && 
            sessionManager.getCurrentUser().getRole().name().equals("GUEST")) {
            favoritesButton.setEnabled(false);
        }
    }
    
    private void openOnlineSearch() {
        OnlineSearchPanel searchPanel = new OnlineSearchPanel(userService, sessionManager);
        JFrame searchFrame = new JFrame("Online Search");
        searchFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        searchFrame.add(searchPanel);
        searchFrame.pack();
        searchFrame.setLocationRelativeTo(this);
        searchFrame.setVisible(true);
    }
    
    private void openOfflineSearch() {
        OfflineSearchPanel searchPanel = new OfflineSearchPanel(userService, sessionManager);
        JFrame searchFrame = new JFrame("Offline Search");
        searchFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        searchFrame.add(searchPanel);
        searchFrame.pack();
        searchFrame.setSize(800, 600);
        searchFrame.setLocationRelativeTo(this);
        searchFrame.setVisible(true);
    }
    
    private void openFavorites() {
        FavoritesPanel favoritesPanel = new FavoritesPanel(userService, sessionManager);
        JFrame favoritesFrame = new JFrame("My Favorites");
        favoritesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        favoritesFrame.add(favoritesPanel);
        favoritesFrame.pack();
        favoritesFrame.setSize(800, 600);
        favoritesFrame.setLocationRelativeTo(this);
        favoritesFrame.setVisible(true);
    }
    
    private void openAdminPanel() {
        AdminPanel adminPanel = new AdminPanel(adminService, userService);
        JFrame adminFrame = new JFrame("Admin Panel");
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adminFrame.add(adminPanel);
        adminFrame.pack();
        adminFrame.setSize(900, 700);
        adminFrame.setLocationRelativeTo(this);
        adminFrame.setVisible(true);
    }
    
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Logout", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            sessionManager.logout();
            dispose();
            onLogout.run();
        }
    }
}

