package model;

/**
 * Represents user-specific metadata for a data record.
 * Used for favorites and personal notes functionality (Innovation #1).
 */
public class UserRecordMeta {
    private String recordId;
    private String username;
    private boolean isFavorite;
    private String note;        // Free-form text note
    private long lastUpdated;   // Timestamp when last updated
    
    /**
     * Constructs a new UserRecordMeta object.
     * 
     * @param recordId the ID of the associated DataRecord
     * @param username the username who owns this metadata
     * @param isFavorite whether this record is marked as favorite
     * @param note personal note text (can be null or empty)
     * @param lastUpdated timestamp when last updated (milliseconds)
     */
    public UserRecordMeta(String recordId, String username, boolean isFavorite, 
                         String note, long lastUpdated) {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new IllegalArgumentException("RecordId cannot be null or empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        this.recordId = recordId.trim();
        this.username = username.trim();
        this.isFavorite = isFavorite;
        this.note = (note != null) ? note : "";
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Gets the record ID.
     * 
     * @return the record ID
     */
    public String getRecordId() {
        return recordId;
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
     * Checks if this record is marked as favorite.
     * 
     * @return true if favorite, false otherwise
     */
    public boolean isFavorite() {
        return isFavorite;
    }
    
    /**
     * Sets the favorite status.
     * 
     * @param isFavorite the new favorite status
     */
    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
    
    /**
     * Gets the personal note.
     * 
     * @return the note text
     */
    public String getNote() {
        return note;
    }
    
    /**
     * Sets the personal note.
     * 
     * @param note the new note text
     */
    public void setNote(String note) {
        this.note = (note != null) ? note : "";
    }
    
    /**
     * Gets the last updated timestamp.
     * 
     * @return timestamp in milliseconds
     */
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Sets the last updated timestamp.
     * 
     * @param lastUpdated timestamp in milliseconds
     */
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Generates a unique key for hash-based storage.
     * Format: username + "#" + recordId
     * 
     * @return the storage key
     */
    public String getStorageKey() {
        return username + "#" + recordId;
    }
    
    @Override
    public String toString() {
        return "UserRecordMeta{recordId='" + recordId + "', username='" + username + 
               "', isFavorite=" + isFavorite + ", lastUpdated=" + lastUpdated + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserRecordMeta that = (UserRecordMeta) obj;
        return recordId.equals(that.recordId) && username.equals(that.username);
    }
    
    @Override
    public int hashCode() {
        return (username + "#" + recordId).hashCode();
    }
}

