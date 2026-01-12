package model;

/**
 * Enumeration representing user roles in the system.
 * Defines three access levels: ADMIN, USER, and GUEST.
 */
public enum Role {
    /**
     * Administrator role with full system access including log viewing and rebuild operations.
     */
    ADMIN,
    
    /**
     * Registered user role with ability to search, store data, and perform offline queries.
     */
    USER,
    
    /**
     * Guest role with limited access - can perform online searches but cannot store data.
     */
    GUEST
}

