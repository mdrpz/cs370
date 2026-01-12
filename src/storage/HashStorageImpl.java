package storage;

import model.DataRecord;
import java.util.*;

/**
 * Hash-based storage implementation using Java's HashMap.
 * Provides O(1) average-case performance for insert, delete, get operations.
 * 
 * This implementation uses HashMap internally but could be replaced with
 * a custom hash table implementation to demonstrate data structure knowledge.
 */
public class HashStorageImpl implements Storage {
    // Hash-based storage: key is record ID, value is DataRecord
    private Map<String, DataRecord> storage;
    
    /**
     * Constructs a new empty HashStorageImpl.
     */
    public HashStorageImpl() {
        this.storage = new HashMap<>();
    }
    
    /**
     * Constructs a HashStorageImpl with initial capacity.
     * 
     * @param initialCapacity initial capacity for the hash map
     */
    public HashStorageImpl(int initialCapacity) {
        this.storage = new HashMap<>(initialCapacity);
    }
    
    @Override
    public void insert(DataRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }
        if (record.getId() == null || record.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Record ID cannot be null or empty");
        }
        
        storage.put(record.getId(), record);
    }
    
    @Override
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        DataRecord removed = storage.remove(id.trim());
        return removed != null;
    }
    
    @Override
    public boolean update(DataRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }
        if (record.getId() == null || record.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Record ID cannot be null or empty");
        }
        
        String id = record.getId().trim();
        if (!storage.containsKey(id)) {
            return false;
        }
        
        storage.put(id, record);
        return true;
    }
    
    @Override
    public DataRecord get(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        
        return storage.get(id.trim());
    }
    
    @Override
    public List<DataRecord> queryByTimeRange(long start, long end) {
        List<DataRecord> results = new ArrayList<>();
        
        for (DataRecord record : storage.values()) {
            long fetchedAt = record.getFetchedAt();
            if (fetchedAt >= start && fetchedAt <= end) {
                results.add(record);
            }
        }
        
        // Sort by timestamp (most recent first)
        results.sort((r1, r2) -> Long.compare(r2.getFetchedAt(), r1.getFetchedAt()));
        
        return results;
    }
    
    @Override
    public List<DataRecord> queryByTitleContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DataRecord> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase().trim();
        
        for (DataRecord record : storage.values()) {
            if (record.getTitle().toLowerCase().contains(lowerKeyword)) {
                results.add(record);
            }
        }
        
        return results;
    }
    
    @Override
    public int size() {
        return storage.size();
    }
    
    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }
    
    @Override
    public void clear() {
        storage.clear();
    }
    
    @Override
    public List<DataRecord> getAll() {
        return new ArrayList<>(storage.values());
    }
    
    /**
     * Checks if a record with the given ID exists.
     * 
     * @param id the record ID
     * @return true if exists, false otherwise
     */
    public boolean contains(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return storage.containsKey(id.trim());
    }
}

