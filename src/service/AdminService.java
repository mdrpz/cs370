package service;

import auth.SessionManager;
import log.LogRebuilder;
import storage.Storage;
import storage.UserMetaStorage;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Service layer for admin operations: log viewing and storage rebuild.
 */
public class AdminService {
    private static final String LOG_FILE = "transactions.log";
    
    private Storage storage;
    private UserMetaStorage metaStorage;
    private SessionManager sessionManager;
    private LogRebuilder logRebuilder;
    
    /**
     * Constructs a new AdminService.
     * 
     * @param storage the data storage
     * @param metaStorage the user metadata storage
     * @param sessionManager the session manager
     */
    public AdminService(Storage storage, UserMetaStorage metaStorage, 
                       SessionManager sessionManager) {
        this.storage = storage;
        this.metaStorage = metaStorage;
        this.sessionManager = sessionManager;
        this.logRebuilder = new LogRebuilder();
    }
    
    /**
     * Checks if the current user is an admin.
     * 
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }
    
    /**
     * Reads the transaction log file and returns its contents.
     * 
     * @return the log file contents as a string
     * @throws IOException if file cannot be read
     */
    public String readTransactionLog() throws IOException {
        if (!isAdmin()) {
            throw new SecurityException("Only admins can read transaction log");
        }
        
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            return "Transaction log file does not exist.";
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(logFile, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Rebuilds storage from the transaction log.
     * 
     * @return RebuildResult with statistics
     * @throws IOException if log file cannot be read
     * @throws SecurityException if user is not admin
     */
    public LogRebuilder.RebuildResult rebuildStorage() throws IOException {
        if (!isAdmin()) {
            throw new SecurityException("Only admins can rebuild storage");
        }
        
        // Clear metadata storage as well (it's not in the log, so we'll lose it)
        metaStorage.clear();
        
        return logRebuilder.rebuild(storage);
    }
    
    /**
     * Gets storage statistics.
     * 
     * @return a string with storage statistics
     */
    public String getStorageStats() {
        if (!isAdmin()) {
            return "Access denied";
        }
        
        return String.format("Total records: %d\nStorage empty: %s", 
                           storage.size(), storage.isEmpty());
    }
}

