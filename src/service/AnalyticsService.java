package service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing transaction log data (Innovation #2).
 * Computes statistics: top search queries, records per day, active users.
 */
public class AnalyticsService {
    private static final String LOG_FILE = "transactions.log";
    
    /**
     * Gets the top N search queries by frequency.
     * 
     * @param topN the number of top queries to return
     * @return list of SearchQueryStats sorted by frequency (descending)
     */
    public List<SearchQueryStats> getTopSearchQueries(int topN) {
        Map<String, Integer> queryCounts = new HashMap<>();
        
        try {
            parseLogFile((parts) -> {
                String action = parts[2].trim();
                if ("SEARCH_ONLINE".equals(action)) {
                    String json = parts[5].trim();
                    String query = extractJsonField(json, "query");
                    if (!query.isEmpty()) {
                        queryCounts.put(query, queryCounts.getOrDefault(query, 0) + 1);
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading log for analytics: " + e.getMessage());
        }
        
        return queryCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(topN)
            .map(e -> new SearchQueryStats(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets the count of records stored per day.
     * 
     * @return map of date (YYYY-MM-DD) to count
     */
    public Map<String, Integer> getRecordsPerDay() {
        Map<String, Integer> dailyCounts = new HashMap<>();
        
        try {
            parseLogFile((parts) -> {
                String action = parts[2].trim();
                if ("INSERT".equals(action)) {
                    try {
                        long timestamp = Long.parseLong(parts[0].trim());
                        String date = formatDate(timestamp);
                        dailyCounts.put(date, dailyCounts.getOrDefault(date, 0) + 1);
                    } catch (NumberFormatException e) {
                        // Skip invalid timestamp
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading log for analytics: " + e.getMessage());
        }
        
        return dailyCounts;
    }
    
    /**
     * Gets the count of distinct active users in the last N days.
     * 
     * @param days the number of days to look back
     * @return the count of distinct users
     */
    public int getActiveUsersCount(int days) {
        Set<String> activeUsers = new HashSet<>();
        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);
        
        try {
            parseLogFile((parts) -> {
                try {
                    long timestamp = Long.parseLong(parts[0].trim());
                    if (timestamp >= cutoffTime) {
                        String username = parts[1].trim();
                        if (!username.isEmpty() && !"guest".equalsIgnoreCase(username)) {
                            activeUsers.add(username);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid timestamp
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading log for analytics: " + e.getMessage());
        }
        
        return activeUsers.size();
    }
    
    /**
     * Parses the log file and calls the processor for each valid line.
     * 
     * @param processor the function to process each log line
     * @throws IOException if file cannot be read
     */
    private void parseLogFile(LogLineProcessor processor) throws IOException {
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(logFile, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split("\\|", -1);
                if (parts.length == 6) {
                    processor.process(parts);
                }
            }
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
            return jsonStr.substring(startIdx, endIdx);
        }
        
        return "";
    }
    
    /**
     * Formats a timestamp as YYYY-MM-DD.
     * 
     * @param timestamp the timestamp in milliseconds
     * @return the formatted date string
     */
    private String formatDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * Functional interface for processing log lines.
     */
    @FunctionalInterface
    private interface LogLineProcessor {
        void process(String[] parts);
    }
    
    /**
     * Statistics class for search queries.
     */
    public static class SearchQueryStats {
        private final String query;
        private final int count;
        
        public SearchQueryStats(String query, int count) {
            this.query = query;
            this.count = count;
        }
        
        public String getQuery() {
            return query;
        }
        
        public int getCount() {
            return count;
        }
        
        @Override
        public String toString() {
            return query + " (" + count + " times)";
        }
    }
}

