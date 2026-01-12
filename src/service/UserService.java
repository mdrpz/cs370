package service;

import auth.SessionManager;
import auth.UserManager;
import log.TransactionLogger;
import model.User;
import model.DataRecord;
import storage.Storage;
import storage.UserMetaStorage;
import java.util.List;

/**
 * Service layer for user operations: searching, storing, and querying data.
 */
public class UserService {
    private Storage storage;
    private UserMetaStorage metaStorage;
    private SessionManager sessionManager;
    private TransactionLogger logger;
    
    /**
     * Constructs a new UserService.
     * 
     * @param storage the data storage
     * @param metaStorage the user metadata storage
     * @param sessionManager the session manager
     * @param userManager the user manager (unused, kept for API compatibility)
     */
    public UserService(Storage storage, UserMetaStorage metaStorage, 
                      SessionManager sessionManager, UserManager userManager) {
        this.storage = storage;
        this.metaStorage = metaStorage;
        this.sessionManager = sessionManager;
        this.logger = TransactionLogger.getInstance();
    }
    
    /**
     * Stores search results into storage (if user has permission).
     * 
     * @param records the records to store
     * @return number of records successfully stored
     */
    public int storeSearchResults(List<DataRecord> records) {
        if (!sessionManager.canStoreData()) {
            return 0; // Guest cannot store
        }
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        
        int stored = 0;
        for (DataRecord record : records) {
            try {
                // Check if record already exists
                DataRecord existing = storage.get(record.getId());
                if (existing != null) {
                    // Update existing record
                    storage.update(record);
                    logger.logModify(currentUser, existing, record);
                } else {
                    // Insert new record
                    storage.insert(record);
                    logger.logInsert(currentUser, record);
                }
                stored++;
            } catch (Exception e) {
                System.err.println("Error storing record " + record.getId() + ": " + e.getMessage());
            }
        }
        
        return stored;
    }
    
    /**
     * Performs offline query by time range.
     * 
     * @param start start timestamp (milliseconds)
     * @param end end timestamp (milliseconds)
     * @return list of matching records
     */
    public List<DataRecord> queryByTimeRange(long start, long end) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            logger.logSearchOffline(currentUser, "TIME_RANGE", 
                "from " + start + " to " + end);
        }
        
        return storage.queryByTimeRange(start, end);
    }
    
    /**
     * Performs offline query by title keyword.
     * 
     * @param keyword the search keyword
     * @return list of matching records
     */
    public List<DataRecord> queryByTitle(String keyword) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            logger.logSearchOffline(currentUser, "TITLE", keyword);
        }
        
        return storage.queryByTitleContains(keyword);
    }
    
    /**
     * Gets all favorite records for the current user.
     * 
     * @return list of favorite DataRecords
     */
    public List<DataRecord> getFavorites() {
        if (!sessionManager.isLoggedIn()) {
            return List.of();
        }
        
        String username = sessionManager.getCurrentUsername();
        List<String> favoriteIds = metaStorage.getFavorites(username);
        
        List<DataRecord> favorites = new java.util.ArrayList<>();
        for (String id : favoriteIds) {
            DataRecord record = storage.get(id);
            if (record != null) {
                favorites.add(record);
            }
        }
        
        return favorites;
    }
    
    /**
     * Toggles favorite status for a record.
     * 
     * @param recordId the record ID
     * @return true if now favorite, false if removed from favorites
     */
    public boolean toggleFavorite(String recordId) {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        User currentUser = sessionManager.getCurrentUser();
        String username = currentUser.getUsername();
        
        model.UserRecordMeta meta = metaStorage.get(username, recordId);
        boolean isFavorite;
        
        if (meta == null) {
            // Create new metadata
            meta = new model.UserRecordMeta(recordId, username, true, "", 
                                          System.currentTimeMillis());
            isFavorite = true;
            logger.logFavoriteAdd(currentUser, recordId);
        } else {
            // Toggle favorite status
            isFavorite = !meta.isFavorite();
            meta.setFavorite(isFavorite);
            meta.setLastUpdated(System.currentTimeMillis());
            
            if (isFavorite) {
                logger.logFavoriteAdd(currentUser, recordId);
            } else {
                logger.logFavoriteRemove(currentUser, recordId);
            }
        }
        
        metaStorage.put(meta);
        return isFavorite;
    }
    
    /**
     * Updates the note for a record.
     * 
     * @param recordId the record ID
     * @param note the note text
     */
    public void updateNote(String recordId, String note) {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        User currentUser = sessionManager.getCurrentUser();
        String username = currentUser.getUsername();
        
        model.UserRecordMeta meta = metaStorage.get(username, recordId);
        
        if (meta == null) {
            meta = new model.UserRecordMeta(recordId, username, false, note, 
                                          System.currentTimeMillis());
        } else {
            meta.setNote(note);
            meta.setLastUpdated(System.currentTimeMillis());
        }
        
        metaStorage.put(meta);
        logger.logNoteUpdate(currentUser, recordId);
    }
    
    /**
     * Gets the note for a record.
     * 
     * @param recordId the record ID
     * @return the note text, or empty string if not found
     */
    public String getNote(String recordId) {
        if (!sessionManager.isLoggedIn()) {
            return "";
        }
        
        String username = sessionManager.getCurrentUsername();
        return metaStorage.getNote(username, recordId);
    }
}

