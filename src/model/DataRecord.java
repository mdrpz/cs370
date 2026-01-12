package model;

/**
 * Represents a single data record scraped from a website.
 * This is the core data unit stored in the hash-based storage system.
 */
public class DataRecord {
    private String id;              // Unique identifier (e.g., "OL12345M" or URL unique part)
    private String title;
    private String author;
    private String extraInfo;       // Additional information (year, rating, etc.)
    private String sourceUrl;       // URL of the page where this record came from
    private long fetchedAt;         // Timestamp when record was fetched (milliseconds)
    private String fetchedByUser;   // Username who fetched this record
    
    /**
     * Constructs a new DataRecord with all required fields.
     * 
     * @param id unique identifier for the record
     * @param title the title of the record
     * @param author the author or creator
     * @param extraInfo additional information (can be null or empty)
     * @param sourceUrl the source URL where this was scraped from
     * @param fetchedAt timestamp when fetched (milliseconds since epoch)
     * @param fetchedByUser username who fetched this record
     */
    public DataRecord(String id, String title, String author, String extraInfo, 
                     String sourceUrl, long fetchedAt, String fetchedByUser) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        if (fetchedByUser == null) {
            throw new IllegalArgumentException("FetchedByUser cannot be null");
        }
        
        this.id = id.trim();
        this.title = title;
        this.author = (author != null) ? author : "";
        this.extraInfo = (extraInfo != null) ? extraInfo : "";
        this.sourceUrl = (sourceUrl != null) ? sourceUrl : "";
        this.fetchedAt = fetchedAt;
        this.fetchedByUser = fetchedByUser;
    }
    
    /**
     * Gets the unique identifier.
     * 
     * @return the ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title.
     * 
     * @param title the new title
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        this.title = title;
    }
    
    /**
     * Gets the author.
     * 
     * @return the author
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * Sets the author.
     * 
     * @param author the new author
     */
    public void setAuthor(String author) {
        this.author = (author != null) ? author : "";
    }
    
    /**
     * Gets the extra information.
     * 
     * @return the extra info
     */
    public String getExtraInfo() {
        return extraInfo;
    }
    
    /**
     * Sets the extra information.
     * 
     * @param extraInfo the new extra info
     */
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = (extraInfo != null) ? extraInfo : "";
    }
    
    /**
     * Gets the source URL.
     * 
     * @return the source URL
     */
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    /**
     * Sets the source URL.
     * 
     * @param sourceUrl the new source URL
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = (sourceUrl != null) ? sourceUrl : "";
    }
    
    /**
     * Gets the fetch timestamp.
     * 
     * @return timestamp in milliseconds
     */
    public long getFetchedAt() {
        return fetchedAt;
    }
    
    /**
     * Sets the fetch timestamp.
     * 
     * @param fetchedAt timestamp in milliseconds
     */
    public void setFetchedAt(long fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
    
    /**
     * Gets the username who fetched this record.
     * 
     * @return the username
     */
    public String getFetchedByUser() {
        return fetchedByUser;
    }
    
    /**
     * Sets the username who fetched this record.
     * 
     * @param fetchedByUser the username
     */
    public void setFetchedByUser(String fetchedByUser) {
        if (fetchedByUser == null) {
            throw new IllegalArgumentException("FetchedByUser cannot be null");
        }
        this.fetchedByUser = fetchedByUser;
    }
    
    /**
     * Creates a copy of this DataRecord.
     * 
     * @return a new DataRecord with the same values
     */
    public DataRecord copy() {
        return new DataRecord(id, title, author, extraInfo, sourceUrl, fetchedAt, fetchedByUser);
    }
    
    @Override
    public String toString() {
        return "DataRecord{id='" + id + "', title='" + title + "', author='" + author + 
               "', fetchedAt=" + fetchedAt + ", fetchedByUser='" + fetchedByUser + "'}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DataRecord that = (DataRecord) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

