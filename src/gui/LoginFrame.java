package gui;

import auth.SessionManager;
import auth.UserManager;
import model.User;
import javax.swing.*;
import java.awt.*;

/**
 * Login window for user authentication and guest access.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton guestButton;
    private JButton registerButton;
    
    private UserManager userManager;
    private SessionManager sessionManager;
    private Runnable onLoginSuccess;
    
    /**
     * Constructs a new LoginFrame.
     * 
     * @param userManager the user manager
     * @param sessionManager the session manager
     * @param onLoginSuccess callback when login succeeds
     */
    public LoginFrame(UserManager userManager, SessionManager sessionManager, 
                     Runnable onLoginSuccess) {
        this.userManager = userManager;
        this.sessionManager = sessionManager;
        this.onLoginSuccess = onLoginSuccess;
        
        initializeComponents();
        layoutComponents();
        attachEventHandlers();
        
        setTitle("Login - Book Search System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        guestButton = new JButton("Continue as Guest");
        registerButton = new JButton("Register");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel("Book Search System");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        mainPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(passwordField, gbc);
        
        // Buttons
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(guestButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void attachEventHandlers() {
        loginButton.addActionListener(e -> performLogin());
        guestButton.addActionListener(e -> performGuestLogin());
        registerButton.addActionListener(e -> openRegistration());
        
        // Enter key on password field triggers login
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.", 
                                        "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User user = userManager.authenticate(username, password);
        if (user != null) {
            sessionManager.setCurrentUser(user);
            JOptionPane.showMessageDialog(this, 
                "Welcome, " + user.getUsername() + "!", 
                "Login Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            onLoginSuccess.run();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password.", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    
    private void performGuestLogin() {
        sessionManager.createGuestSession();
        JOptionPane.showMessageDialog(this, 
            "Continuing as guest. You can search but cannot store results.", 
            "Guest Mode", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        onLoginSuccess.run();
    }
    
    private void openRegistration() {
        RegisterFrame registerFrame = new RegisterFrame(userManager, this);
        registerFrame.setVisible(true);
    }
}

