package model;

/**
 * Represents a user in the system with authentication credentials and role-based access.
 */
public class User {
    private String username;
    private String passwordHash;
    private Role role;
    
    /**
     * Constructs a new User with the specified credentials and role.
     * 
     * @param username the username (must not be null or empty)
     * @param passwordHash the hashed password (SHA-256 hash)
     * @param role the user's role (ADMIN, USER, or GUEST)
     */
    public User(String username, String passwordHash, Role role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (passwordHash == null) {
            throw new IllegalArgumentException("Password hash cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        this.username = username.trim();
        this.passwordHash = passwordHash;
        this.role = role;
    }
    
    /**
     * Gets the username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the password hash.
     * 
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * Gets the user's role.
     * 
     * @return the role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Sets the password hash (useful for password updates).
     * 
     * @param passwordHash the new password hash
     */
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null) {
            throw new IllegalArgumentException("Password hash cannot be null");
        }
        this.passwordHash = passwordHash;
    }
    
    /**
     * Checks if this user has admin privileges.
     * 
     * @return true if role is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    /**
     * Checks if this user can store data (USER or ADMIN roles).
     * 
     * @return true if user can store data, false otherwise
     */
    public boolean canStoreData() {
        return role == Role.USER || role == Role.ADMIN;
    }
    
    @Override
    public String toString() {
        return "User{username='" + username + "', role=" + role + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}

