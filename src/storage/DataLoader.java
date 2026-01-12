package storage;

import model.DataRecord;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for loading initial data from files.
 * Supports loading from transaction log format files.
 */
public class DataLoader {
    /**
     * Loads initial data from a file into storage.
     * Only processes INSERT transactions.
     * 
     * @param storage the storage to load into
     * @param filePath the path to the data file
     * @return number of records loaded
     * @throws IOException if file cannot be read
     */
    public int loadInitialData(Storage storage, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Initial data file not found: " + filePath);
        }
        
        int loaded = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse line: TIMESTAMP | USERNAME | ACTION | ID | TITLE | ADDITIONAL_FIELDS_JSON
                String[] parts = line.split("\\|", -1);
                
                if (parts.length != 6) {
                    System.err.println("Warning: Invalid format in line " + lineNumber);
                    continue;
                }
                
                String action = parts[2].trim();
                
                // Only process INSERT actions
                if (!"INSERT".equals(action)) {
                    continue;
                }
                
                try {
                    DataRecord record = parseInsertLine(parts);
                    if (record != null) {
                        storage.insert(record);
                        loaded++;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        
        return loaded;
    }
    
    /**
     * Parses an INSERT line into a DataRecord.
     * 
     * @param parts the split line parts
     * @return the DataRecord, or null if parsing fails
     */
    private DataRecord parseInsertLine(String[] parts) {
        try {
            String id = parts[3].trim();
            String title = parts[4].trim();
            String jsonStr = parts[5].trim();
            
            if (id.isEmpty() || title.isEmpty()) {
                return null;
            }
            
            // Parse JSON fields
            String author = extractJsonField(jsonStr, "author");
            String extraInfo = extractJsonField(jsonStr, "extraInfo");
            String url = extractJsonField(jsonStr, "url");
            String fetchedByUser = extractJsonField(jsonStr, "fetchedByUser");
            
            long fetchedAt = 0;
            try {
                String fetchedAtStr = extractJsonField(jsonStr, "fetchedAt");
                if (!fetchedAtStr.isEmpty()) {
                    fetchedAt = Long.parseLong(fetchedAtStr);
                } else {
                    fetchedAt = System.currentTimeMillis();
                }
            } catch (NumberFormatException e) {
                fetchedAt = System.currentTimeMillis();
            }
            
            if (fetchedByUser.isEmpty()) {
                fetchedByUser = "system";
            }
            
            return new DataRecord(id, title, author, extraInfo, url, fetchedAt, fetchedByUser);
            
        } catch (Exception e) {
            System.err.println("Error parsing INSERT line: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts a field value from a JSON string.
     * 
     * @param jsonStr the JSON string
     * @param fieldName the field name
     * @return the field value, or empty string if not found
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
        
        while (endIdx < jsonStr.length()) {
            if (jsonStr.charAt(endIdx) == '"' && 
                (endIdx == startIdx || jsonStr.charAt(endIdx - 1) != '\\')) {
                break;
            }
            endIdx++;
        }
        
        if (endIdx > startIdx) {
            String value = jsonStr.substring(startIdx, endIdx);
            return value.replace("\\\"", "\"")
                       .replace("\\\\", "\\")
                       .replace("\\n", "\n")
                       .replace("\\r", "\r")
                       .replace("\\t", "\t");
        }
        
        return "";
    }
}

