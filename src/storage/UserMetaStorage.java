package storage;

import model.UserRecordMeta;
import java.util.*;

/**
 * Hash-based storage for user-specific metadata (favorites and notes).
 * Key format: username + "#" + recordId
 */
public class UserMetaStorage {
    private Map<String, UserRecordMeta> storage;
    
    /**
     * Constructs a new empty UserMetaStorage.
     */
    public UserMetaStorage() {
        this.storage = new HashMap<>();
    }
    
    /**
     * Stores or updates user metadata for a record.
     * 
     * @param meta the metadata to store
     */
    public void put(UserRecordMeta meta) {
        if (meta == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        storage.put(meta.getStorageKey(), meta);
    }
    
    /**
     * Gets user metadata for a specific user and record.
     * 
     * @param username the username
     * @param recordId the record ID
     * @return the UserRecordMeta if found, null otherwise
     */
    public UserRecordMeta get(String username, String recordId) {
        if (username == null || recordId == null) {
            return null;
        }
        String key = username.trim() + "#" + recordId.trim();
        return storage.get(key);
    }
    
    /**
     * Removes user metadata.
     * 
     * @param username the username
     * @param recordId the record ID
     * @return true if removed, false if not found
     */
    public boolean remove(String username, String recordId) {
        if (username == null || recordId == null) {
            return false;
        }
        String key = username.trim() + "#" + recordId.trim();
        return storage.remove(key) != null;
    }
    
    /**
     * Gets all favorite records for a user.
     * 
     * @param username the username
     * @return list of record IDs that are favorites
     */
    public List<String> getFavorites(String username) {
        if (username == null) {
            return new ArrayList<>();
        }
        
        List<String> favorites = new ArrayList<>();
        String prefix = username.trim() + "#";
        
        for (Map.Entry<String, UserRecordMeta> entry : storage.entrySet()) {
            if (entry.getKey().startsWith(prefix) && entry.getValue().isFavorite()) {
                favorites.add(entry.getValue().getRecordId());
            }
        }
        
        return favorites;
    }
    
    /**
     * Gets all metadata records for a specific user.
     * 
     * @param username the username
     * @return list of UserRecordMeta objects
     */
    public List<UserRecordMeta> getUserMetadata(String username) {
        if (username == null) {
            return new ArrayList<>();
        }
        
        List<UserRecordMeta> result = new ArrayList<>();
        String prefix = username.trim() + "#";
        
        for (Map.Entry<String, UserRecordMeta> entry : storage.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a record is marked as favorite by a user.
     * 
     * @param username the username
     * @param recordId the record ID
     * @return true if favorite, false otherwise
     */
    public boolean isFavorite(String username, String recordId) {
        UserRecordMeta meta = get(username, recordId);
        return meta != null && meta.isFavorite();
    }
    
    /**
     * Gets the note for a record by a user.
     * 
     * @param username the username
     * @param recordId the record ID
     * @return the note, or empty string if not found
     */
    public String getNote(String username, String recordId) {
        UserRecordMeta meta = get(username, recordId);
        return (meta != null) ? meta.getNote() : "";
    }
    
    /**
     * Clears all metadata (for rebuild operations).
     */
    public void clear() {
        storage.clear();
    }
    
    /**
     * Gets the total number of metadata entries.
     * 
     * @return the count
     */
    public int size() {
        return storage.size();
    }
}

