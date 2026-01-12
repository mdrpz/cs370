package log;

import storage.Storage;
import model.DataRecord;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Rebuilds storage from transaction log file.
 * Reads transactions.log and replays all transactions to reconstruct storage state.
 */
public class LogRebuilder {
    private static final String LOG_FILE = "transactions.log";
    
    /**
     * Rebuilds storage by replaying all transactions from the log file.
     * 
     * @param storage the storage to rebuild (will be cleared first)
     * @return RebuildResult containing statistics about the rebuild
     * @throws IOException if log file cannot be read
     */
    public RebuildResult rebuild(Storage storage) throws IOException {
        File logFile = new File(LOG_FILE);
        
        if (!logFile.exists()) {
            throw new FileNotFoundException("Transaction log file not found: " + LOG_FILE);
        }
        
        // Clear existing storage
        storage.clear();
        
        int insertCount = 0;
        int modifyCount = 0;
        int deleteCount = 0;
        int errorCount = 0;
        int totalLines = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(logFile, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    // Parse line: TIMESTAMP | USERNAME | ACTION | ID | TITLE | ADDITIONAL_FIELDS_JSON
                    String[] parts = line.split("\\|", -1);
                    
                    if (parts.length != 6) {
                        System.err.println("Warning: Invalid log line format (line " + totalLines + "): " + line);
                        errorCount++;
                        continue;
                    }
                    
                    String action = parts[2].trim();
                    String id = parts[3].trim();
                    
                    // Process based on action type
                    switch (action) {
                        case "INSERT":
                            if (!id.isEmpty()) {
                                DataRecord record = parseInsertRecord(parts);
                                if (record != null) {
                                    storage.insert(record);
                                    insertCount++;
                                } else {
                                    errorCount++;
                                }
                            }
                            break;
                            
                        case "MODIFY":
                            if (!id.isEmpty()) {
                                DataRecord record = parseModifyRecord(parts);
                                if (record != null) {
                                    storage.update(record);
                                    modifyCount++;
                                } else {
                                    errorCount++;
                                }
                            }
                            break;
                            
                        case "DELETE":
                            if (!id.isEmpty()) {
                                storage.delete(id);
                                deleteCount++;
                            }
                            break;
                            
                        // Ignore SEARCH_ONLINE, SEARCH_OFFLINE, FAVORITE_*, NOTE_UPDATE
                        // These don't affect storage state
                        default:
                            break;
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error processing log line " + totalLines + ": " + e.getMessage());
                    errorCount++;
                }
            }
        }
        
        return new RebuildResult(insertCount, modifyCount, deleteCount, errorCount, totalLines);
    }
    
    /**
     * Parses an INSERT transaction line into a DataRecord.
     * 
     * @param parts the split log line parts
     * @return the DataRecord, or null if parsing fails
     */
    private DataRecord parseInsertRecord(String[] parts) {
        try {
            String id = parts[3].trim();
            String title = parts[4].trim();
            String jsonStr = parts[5].trim();
            
            // Parse JSON (simplified - assumes valid JSON format)
            String author = extractJsonField(jsonStr, "author");
            String extraInfo = extractJsonField(jsonStr, "extraInfo");
            String url = extractJsonField(jsonStr, "url");
            String fetchedByUser = extractJsonField(jsonStr, "fetchedByUser");
            
            long fetchedAt = 0;
            try {
                String fetchedAtStr = extractJsonField(jsonStr, "fetchedAt");
                if (!fetchedAtStr.isEmpty()) {
                    fetchedAt = Long.parseLong(fetchedAtStr);
                }
            } catch (NumberFormatException e) {
                // Use current time as fallback
                fetchedAt = System.currentTimeMillis();
            }
            
            if (fetchedByUser.isEmpty()) {
                fetchedByUser = "system";
            }
            
            return new DataRecord(id, title, author, extraInfo, url, fetchedAt, fetchedByUser);
            
        } catch (Exception e) {
            System.err.println("Error parsing INSERT record: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parses a MODIFY transaction line into a DataRecord.
     * Uses newTitle and newAuthor from JSON.
     * 
     * @param parts the split log line parts
     * @return the DataRecord, or null if parsing fails
     */
    private DataRecord parseModifyRecord(String[] parts) {
        try {
            String id = parts[3].trim();
            String title = parts[4].trim();
            String jsonStr = parts[5].trim();
            
            // For MODIFY, use newTitle and newAuthor from JSON
            String newTitle = extractJsonField(jsonStr, "newTitle");
            if (newTitle.isEmpty()) {
                newTitle = title;
            }
            
            String newAuthor = extractJsonField(jsonStr, "newAuthor");
            
            // Get existing record to preserve other fields
            // Note: This is a limitation - we don't have full record in MODIFY log
            // For now, create minimal record with new values
            return new DataRecord(id, newTitle, newAuthor, "", "", 
                                System.currentTimeMillis(), "system");
            
        } catch (Exception e) {
            System.err.println("Error parsing MODIFY record: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts a field value from a JSON string (simplified parser).
     * 
     * @param jsonStr the JSON string
     * @param fieldName the field name to extract
     * @return the field value (unescaped), or empty string if not found
     */
    private String extractJsonField(String jsonStr, String fieldName) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return "";
        }
        
        String searchPattern = "\"" + fieldName + "\":\"";
        int startIdx = jsonStr.indexOf(searchPattern);
        
        if (startIdx == -1) {
            // Try numeric field
            searchPattern = "\"" + fieldName + "\":";
            startIdx = jsonStr.indexOf(searchPattern);
            if (startIdx != -1) {
                startIdx += searchPattern.length();
                int endIdx = jsonStr.indexOf(",", startIdx);
                if (endIdx == -1) {
                    endIdx = jsonStr.indexOf("}", startIdx);
                }
                if (endIdx != -1) {
                    return jsonStr.substring(startIdx, endIdx).trim();
                }
            }
            return "";
        }
        
        startIdx += searchPattern.length();
        int endIdx = startIdx;
        
        // Find end of string value (accounting for escaped quotes)
        while (endIdx < jsonStr.length()) {
            if (jsonStr.charAt(endIdx) == '"' && 
                (endIdx == startIdx || jsonStr.charAt(endIdx - 1) != '\\')) {
                break;
            }
            endIdx++;
        }
        
        if (endIdx > startIdx) {
            String value = jsonStr.substring(startIdx, endIdx);
            // Unescape JSON escapes
            return value.replace("\\\"", "\"")
                       .replace("\\\\", "\\")
                       .replace("\\n", "\n")
                       .replace("\\r", "\r")
                       .replace("\\t", "\t");
        }
        
        return "";
    }
    
    /**
     * Result class containing rebuild statistics.
     */
    public static class RebuildResult {
        private final int insertCount;
        private final int modifyCount;
        private final int deleteCount;
        private final int errorCount;
        private final int totalLines;
        
        public RebuildResult(int insertCount, int modifyCount, int deleteCount, 
                           int errorCount, int totalLines) {
            this.insertCount = insertCount;
            this.modifyCount = modifyCount;
            this.deleteCount = deleteCount;
            this.errorCount = errorCount;
            this.totalLines = totalLines;
        }
        
        public int getInsertCount() { return insertCount; }
        public int getModifyCount() { return modifyCount; }
        public int getDeleteCount() { return deleteCount; }
        public int getErrorCount() { return errorCount; }
        public int getTotalLines() { return totalLines; }
        
        @Override
        public String toString() {
            return String.format("Rebuilt: %d inserts, %d modifies, %d deletes. " +
                               "Errors: %d. Total lines: %d",
                insertCount, modifyCount, deleteCount, errorCount, totalLines);
        }
    }
}

