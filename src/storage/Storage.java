package storage;

import model.DataRecord;
import java.util.List;

/**
 * Interface for hash-based storage of DataRecord objects.
 * Provides basic CRUD operations and query capabilities.
 */
public interface Storage {
    /**
     * Inserts a new record into storage.
     * If a record with the same ID already exists, behavior is implementation-dependent
     * (may overwrite or throw exception).
     * 
     * @param record the record to insert
     * @throws IllegalArgumentException if record is null or has invalid ID
     */
    void insert(DataRecord record);
    
    /**
     * Deletes a record from storage by ID.
     * 
     * @param id the ID of the record to delete
     * @return true if record was found and deleted, false if not found
     */
    boolean delete(String id);
    
    /**
     * Updates an existing record in storage.
     * The record must already exist (same ID).
     * 
     * @param record the updated record
     * @return true if record was found and updated, false if not found
     * @throws IllegalArgumentException if record is null or has invalid ID
     */
    boolean update(DataRecord record);
    
    /**
     * Retrieves a record by ID.
     * 
     * @param id the ID of the record to retrieve
     * @return the DataRecord if found, null otherwise
     */
    DataRecord get(String id);
    
    /**
     * Queries records within a time range.
     * 
     * @param start start timestamp (inclusive, milliseconds)
     * @param end end timestamp (inclusive, milliseconds)
     * @return list of records within the time range, empty list if none found
     */
    List<DataRecord> queryByTimeRange(long start, long end);
    
    /**
     * Queries records whose title contains the specified keyword (case-insensitive).
     * 
     * @param keyword the keyword to search for
     * @return list of matching records, empty list if none found
     */
    List<DataRecord> queryByTitleContains(String keyword);
    
    /**
     * Gets the total number of records in storage.
     * 
     * @return the count of records
     */
    int size();
    
    /**
     * Checks if storage is empty.
     * 
     * @return true if no records, false otherwise
     */
    boolean isEmpty();
    
    /**
     * Clears all records from storage.
     * Use with caution - typically only for rebuild operations.
     */
    void clear();
    
    /**
     * Gets all records in storage.
     * 
     * @return list of all records
     */
    List<DataRecord> getAll();
}

