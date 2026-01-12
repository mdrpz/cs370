package auth;

import model.User;
import model.Role;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Manages user accounts: loading, saving, authentication, and registration.
 * Handles reading/writing users.txt file and password hashing.
 */
public class UserManager {
    private static final String USERS_FILE = "users.txt";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    
    private Map<String, User> users;
    
    /**
     * Constructs a new UserManager and loads users from file.
     * Creates default admin account if users.txt doesn't exist.
     */
    public UserManager() {
        this.users = new HashMap<>();
        loadUsers();
    }
    
    /**
     * Loads users from users.txt file.
     * Creates default admin account if file doesn't exist.
     */
    private void loadUsers() {
        File usersFile = new File(USERS_FILE);
        
        if (!usersFile.exists()) {
            // Create default admin account
            createDefaultAdmin();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(usersFile, StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse line: username|passwordHash|role
                String[] parts = line.split("\\|", -1);
                
                if (parts.length != 3) {
                    System.err.println("Warning: Invalid format in users.txt line " + 
                                     lineNumber + ": " + line);
                    continue;
                }
                
                String username = parts[0].trim();
                String passwordHash = parts[1].trim();
                String roleStr = parts[2].trim().toUpperCase();
                
                if (username.isEmpty()) {
                    System.err.println("Warning: Empty username in line " + lineNumber);
                    continue;
                }
                
                Role role;
                try {
                    role = Role.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Invalid role '" + roleStr + "' in line " + 
                                     lineNumber + ". Using USER as default.");
                    role = Role.USER;
                }
                
                User user = new User(username, passwordHash, role);
                users.put(username, user);
            }
            
            // Ensure at least one admin exists
            boolean hasAdmin = users.values().stream()
                    .anyMatch(u -> u.getRole() == Role.ADMIN);
            
            if (!hasAdmin) {
                System.err.println("Warning: No admin account found. Creating default admin.");
                createDefaultAdmin();
            }
            
        } catch (IOException e) {
            System.err.println("Error reading users.txt: " + e.getMessage());
            System.err.println("Creating default admin account.");
            createDefaultAdmin();
        }
    }
    
    /**
     * Creates the default admin account and saves it to file.
     */
    private void createDefaultAdmin() {
        String passwordHash = hashPassword(DEFAULT_ADMIN_PASSWORD);
        User admin = new User(DEFAULT_ADMIN_USERNAME, passwordHash, Role.ADMIN);
        users.put(DEFAULT_ADMIN_USERNAME, admin);
        saveUsers();
    }
    
    /**
     * Saves all users to users.txt file.
     */
    public void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(USERS_FILE, StandardCharsets.UTF_8))) {
            
            for (User user : users.values()) {
                writer.write(user.getUsername() + "|" + 
                           user.getPasswordHash() + "|" + 
                           user.getRole().name());
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error writing users.txt: " + e.getMessage());
            throw new RuntimeException("Failed to save users file", e);
        }
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username
     * @param password the plain text password
     * @return the User if authentication succeeds, null otherwise
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        
        User user = users.get(username.trim());
        if (user == null) {
            return null;
        }
        
        String passwordHash = hashPassword(password);
        if (user.getPasswordHash().equals(passwordHash)) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Registers a new USER account (not ADMIN).
     * 
     * @param username the username
     * @param password the plain text password
     * @return true if registration successful, false if username already exists
     * @throws IllegalArgumentException if username or password is invalid
     */
    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        username = username.trim();
        
        if (users.containsKey(username)) {
            return false; // Username already exists
        }
        
        String passwordHash = hashPassword(password);
        User newUser = new User(username, passwordHash, Role.USER);
        users.put(username, newUser);
        saveUsers();
        
        return true;
    }
    
    /**
     * Gets a user by username.
     * 
     * @param username the username
     * @return the User if found, null otherwise
     */
    public User getUser(String username) {
        if (username == null) {
            return null;
        }
        return users.get(username.trim());
    }
    
    /**
     * Checks if a username exists.
     * 
     * @param username the username to check
     * @return true if exists, false otherwise
     */
    public boolean userExists(String username) {
        if (username == null) {
            return false;
        }
        return users.containsKey(username.trim());
    }
    
    /**
     * Hashes a password using SHA-256.
     * 
     * @param password the plain text password
     * @return the hexadecimal hash string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Gets all users (for admin purposes).
     * 
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}

