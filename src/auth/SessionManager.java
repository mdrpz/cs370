package auth;

import model.User;
import model.Role;

/**
 * Manages the current user session.
 * Singleton pattern to ensure only one active session at a time.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SessionManager() {
        this.currentUser = null;
    }
    
    /**
     * Gets the singleton instance.
     * 
     * @return the SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Sets the current user session.
     * 
     * @param user the user to set as current (can be null to clear session)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Creates a guest session.
     */
    public void createGuestSession() {
        this.currentUser = new User("guest", "", Role.GUEST);
    }
    
    /**
     * Gets the current user.
     * 
     * @return the current User, or null if no session
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if there is an active session.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Checks if the current user is an admin.
     * 
     * @return true if current user is admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Checks if the current user can store data (USER or ADMIN).
     * 
     * @return true if user can store data, false otherwise
     */
    public boolean canStoreData() {
        return currentUser != null && currentUser.canStoreData();
    }
    
    /**
     * Gets the current username.
     * 
     * @return the username, or "guest" if no session
     */
    public String getCurrentUsername() {
        if (currentUser == null) {
            return "guest";
        }
        return currentUser.getUsername();
    }
    
    /**
     * Clears the current session (logout).
     */
    public void logout() {
        this.currentUser = null;
    }
}

