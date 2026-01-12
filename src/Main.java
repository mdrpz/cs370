import auth.SessionManager;
import auth.UserManager;
import cli.CommandLineParser;
import gui.LoginFrame;
import gui.MainDashboardFrame;
import service.AdminService;
import service.UserService;
import storage.DataLoader;
import storage.HashStorageImpl;
import storage.Storage;
import storage.UserMetaStorage;
import javax.swing.*;

/**
 * Main entry point for the Book Search System.
 * Handles command line arguments, initializes storage, and launches the GUI.
 */
public class Main {
    private static Storage storage;
    private static UserMetaStorage metaStorage;
    private static UserManager userManager;
    private static SessionManager sessionManager;
    private static UserService userService;
    private static AdminService adminService;
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Warning: Could not set system look and feel");
        }
        
        // Parse command line arguments
        CommandLineParser parser = new CommandLineParser();
        parser.parse(args);
        
        // Initialize storage
        storage = new HashStorageImpl();
        metaStorage = new UserMetaStorage();
        
        // If --startEmpty flag is set, ensure storage is empty
        if (parser.isStartEmpty()) {
            storage.clear();
            metaStorage.clear();
        }
        
        // Load initial data if specified
        if (parser.getInitialDataFile() != null) {
            try {
                DataLoader loader = new DataLoader();
                int loaded = loader.loadInitialData(storage, parser.getInitialDataFile());
                System.out.println("Loaded " + loaded + " records from initial data file.");
            } catch (Exception e) {
                System.err.println("Error loading initial data: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Error loading initial data: " + e.getMessage(), 
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Initialize managers and services
        userManager = new UserManager();
        sessionManager = SessionManager.getInstance();
        userService = new UserService(storage, metaStorage, sessionManager, userManager);
        adminService = new AdminService(storage, metaStorage, sessionManager);
        
        // Launch login GUI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(userManager, sessionManager, () -> {
                // On successful login, show dashboard
                MainDashboardFrame dashboard = new MainDashboardFrame(
                    sessionManager, userService, adminService, () -> {
                        // On logout, show login again
                        SwingUtilities.invokeLater(() -> {
                            LoginFrame newLoginFrame = new LoginFrame(
                                userManager, sessionManager, () -> {
                                    MainDashboardFrame newDashboard = new MainDashboardFrame(
                                        sessionManager, userService, adminService, 
                                        () -> System.exit(0));
                                    newDashboard.setVisible(true);
                                });
                            newLoginFrame.setVisible(true);
                        });
                    });
                dashboard.setVisible(true);
            });
            loginFrame.setVisible(true);
        });
    }
}

