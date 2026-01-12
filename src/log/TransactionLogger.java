package log;

import model.User;
import model.DataRecord;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles transaction logging to transactions.log file.
 * Thread-safe singleton that appends transaction records.
 * 
 * Format: TIMESTAMP | USERNAME | ACTION | ID | TITLE | ADDITIONAL_FIELDS_JSON
 */
public class TransactionLogger {
    private static TransactionLogger instance;
    private static final String LOG_FILE = "transactions.log";
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
     * Private constructor for singleton pattern.
     */
    private TransactionLogger() {
        // Ensure log file exists
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Warning: Could not create transactions.log: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gets the singleton instance.
     * 
     * @return the TransactionLogger instance
     */
    public static synchronized TransactionLogger getInstance() {
        if (instance == null) {
            instance = new TransactionLogger();
        }
        return instance;
    }
    
    /**
     * Logs an INSERT transaction.
     * 
     * @param user the user performing the action
     * @param record the record being inserted
     */
    public void logInsert(User user, DataRecord record) {
        if (user == null || record == null) {
            return;
        }
        
        String additionalFields = String.format(
            "{\"author\":\"%s\",\"extraInfo\":\"%s\",\"url\":\"%s\",\"fetchedAt\":%d,\"fetchedByUser\":\"%s\"}",
            escapeJson(record.getAuthor()),
            escapeJson(record.getExtraInfo()),
            escapeJson(record.getSourceUrl()),
            record.getFetchedAt(),
            escapeJson(record.getFetchedByUser())
        );
        
        logTransaction(System.currentTimeMillis(), user.getUsername(), "INSERT", 
                      record.getId(), record.getTitle(), additionalFields);
    }
    
    /**
     * Logs a DELETE transaction.
     * 
     * @param user the user performing the action
     * @param id the ID of the record being deleted
     * @param oldRecord the record being deleted (for reference)
     */
    public void logDelete(User user, String id, DataRecord oldRecord) {
        if (user == null || id == null) {
            return;
        }
        
        String title = (oldRecord != null) ? oldRecord.getTitle() : "";
        String additionalFields = "{}";
        
        logTransaction(System.currentTimeMillis(), user.getUsername(), "DELETE", 
                      id, title, additionalFields);
    }
    
    /**
     * Logs a MODIFY transaction.
     * 
     * @param user the user performing the action
     * @param oldRecord the record before modification
     * @param newRecord the record after modification
     */
    public void logModify(User user, DataRecord oldRecord, DataRecord newRecord) {
        if (user == null || oldRecord == null || newRecord == null) {
            return;
        }
        
        String additionalFields = String.format(
            "{\"oldTitle\":\"%s\",\"newTitle\":\"%s\",\"oldAuthor\":\"%s\",\"newAuthor\":\"%s\"}",
            escapeJson(oldRecord.getTitle()),
            escapeJson(newRecord.getTitle()),
            escapeJson(oldRecord.getAuthor()),
            escapeJson(newRecord.getAuthor())
        );
        
        logTransaction(System.currentTimeMillis(), user.getUsername(), "MODIFY", 
                      newRecord.getId(), newRecord.getTitle(), additionalFields);
    }
    
    /**
     * Logs a SEARCH_ONLINE transaction.
     * 
     * @param user the user performing the search
     * @param query the search query term
     */
    public void logSearchOnline(User user, String query) {
        if (user == null || query == null) {
            return;
        }
        
        String additionalFields = String.format("{\"query\":\"%s\"}", escapeJson(query));
        logTransaction(System.currentTimeMillis(), user.getUsername(), "SEARCH_ONLINE", 
                      "", "", additionalFields);
    }
    
    /**
     * Logs a SEARCH_OFFLINE transaction.
     * 
     * @param user the user performing the search
     * @param queryType the type of query ("TIME_RANGE" or "TITLE")
     * @param query the search query
     */
    public void logSearchOffline(User user, String queryType, String query) {
        if (user == null) {
            return;
        }
        
        String additionalFields = String.format("{\"queryType\":\"%s\",\"query\":\"%s\"}", 
                                               escapeJson(queryType), escapeJson(query));
        logTransaction(System.currentTimeMillis(), user.getUsername(), "SEARCH_OFFLINE", 
                      "", "", additionalFields);
    }
    
    /**
     * Logs a FAVORITE_ADD transaction.
     * 
     * @param user the user adding favorite
     * @param recordId the record ID
     */
    public void logFavoriteAdd(User user, String recordId) {
        if (user == null || recordId == null) {
            return;
        }
        
        String additionalFields = String.format("{\"recordId\":\"%s\"}", escapeJson(recordId));
        logTransaction(System.currentTimeMillis(), user.getUsername(), "FAVORITE_ADD", 
                      recordId, "", additionalFields);
    }
    
    /**
     * Logs a FAVORITE_REMOVE transaction.
     * 
     * @param user the user removing favorite
     * @param recordId the record ID
     */
    public void logFavoriteRemove(User user, String recordId) {
        if (user == null || recordId == null) {
            return;
        }
        
        String additionalFields = String.format("{\"recordId\":\"%s\"}", escapeJson(recordId));
        logTransaction(System.currentTimeMillis(), user.getUsername(), "FAVORITE_REMOVE", 
                      recordId, "", additionalFields);
    }
    
    /**
     * Logs a NOTE_UPDATE transaction.
     * 
     * @param user the user updating note
     * @param recordId the record ID
     */
    public void logNoteUpdate(User user, String recordId) {
        if (user == null || recordId == null) {
            return;
        }
        
        String additionalFields = String.format("{\"recordId\":\"%s\"}", escapeJson(recordId));
        logTransaction(System.currentTimeMillis(), user.getUsername(), "NOTE_UPDATE", 
                      recordId, "", additionalFields);
    }
    
    /**
     * Writes a transaction line to the log file.
     * Thread-safe implementation.
     * 
     * @param timestamp the timestamp
     * @param username the username
     * @param action the action type
     * @param id the record ID
     * @param title the title
     * @param additionalFields JSON string with additional fields
     */
    private void logTransaction(long timestamp, String username, String action, 
                               String id, String title, String additionalFields) {
        lock.lock();
        try {
            // Replace pipes in title with underscores to avoid format issues
            String safeTitle = (title != null) ? title.replace("|", "_") : "";
            String safeId = (id != null) ? id : "";
            String safeUsername = (username != null) ? username : "";
            String safeAction = (action != null) ? action : "";
            
            String logLine = String.format("%d | %s | %s | %s | %s | %s",
                timestamp, safeUsername, safeAction, safeId, safeTitle, additionalFields);
            
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(LOG_FILE, StandardCharsets.UTF_8, true))) {
                writer.write(logLine);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Error writing to transaction log: " + e.getMessage());
                // Show error to user if possible (in GUI context)
                javax.swing.JOptionPane.showMessageDialog(null,
                    "Failed to write transaction log. Operation may not be persisted.",
                    "Log Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Escapes special characters in JSON strings.
     * 
     * @param str the string to escape
     * @return the escaped string
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}

