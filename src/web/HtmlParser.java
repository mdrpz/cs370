package web;

import model.DataRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parses HTML content to extract DataRecord objects.
 * Uses basic string operations (indexOf, substring) - no third-party HTML parsers.
 * 
 * This parser is designed for OpenLibrary search results page structure.
 * Adapt the patterns if using a different website.
 */
public class HtmlParser {
    private static final String BASE_URL = "https://openlibrary.org";
    
    /**
     * Parses HTML content and extracts DataRecord objects.
     * 
     * @param html the HTML content to parse
     * @param currentUser the username of the current user (for fetchedByUser field)
     * @return list of parsed DataRecord objects
     */
    public List<DataRecord> parseSearchResults(String html, String currentUser) {
        List<DataRecord> records = new ArrayList<>();
        
        if (html == null || html.isEmpty()) {
            return records;
        }
        
        if (currentUser == null || currentUser.trim().isEmpty()) {
            currentUser = "guest";
        }
        
        long fetchedAt = System.currentTimeMillis();
        
        // OpenLibrary search results are typically in <li class="searchResultItem"> or similar
        // We'll look for book entries in the HTML
        
        // Pattern 1: Look for book entries with links to /works/ or /books/
        // Example: <a href="/works/OL12345M">Title</a>
        Pattern workLinkPattern = Pattern.compile(
            "<a[^>]+href=[\"'](/works/[^\"']+)[\"'][^>]*>([^<]+)</a>",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = workLinkPattern.matcher(html);
        
        while (matcher.find()) {
            try {
                String workPath = matcher.group(1);
                String title = cleanText(matcher.group(2));
                
                if (title.isEmpty() || workPath.isEmpty()) {
                    continue;
                }
                
                // Extract work ID from path (e.g., /works/OL12345M -> OL12345M)
                String id = extractIdFromPath(workPath);
                if (id.isEmpty()) {
                    continue;
                }
                
                // Try to find author information near this link
                String author = extractAuthorNearLink(html, matcher.start());
                
                // Build full URL
                String sourceUrl = BASE_URL + workPath;
                
                // Create record
                DataRecord record = new DataRecord(
                    id,
                    title,
                    author,
                    "", // extraInfo - can be enhanced
                    sourceUrl,
                    fetchedAt,
                    currentUser
                );
                
                // Avoid duplicates
                if (!containsRecord(records, id)) {
                    records.add(record);
                }
                
            } catch (Exception e) {
                // Skip this entry if parsing fails
                System.err.println("Error parsing record: " + e.getMessage());
            }
        }
        
        // If no records found with pattern 1, try alternative pattern
        if (records.isEmpty()) {
            records = parseAlternativeFormat(html, currentUser, fetchedAt);
        }
        
        return records;
    }
    
    /**
     * Alternative parsing method for different HTML structures.
     * 
     * @param html the HTML content
     * @param currentUser the current user
     * @param fetchedAt the fetch timestamp
     * @return list of parsed records
     */
    private List<DataRecord> parseAlternativeFormat(String html, String currentUser, long fetchedAt) {
        List<DataRecord> records = new ArrayList<>();
        
        // Look for divs or spans containing book information
        // This is a fallback method - adjust based on actual HTML structure
        
        int index = 0;
        while (index < html.length()) {
            // Look for common book title patterns
            int titleStart = html.indexOf("<span class=\"title\">", index);
            if (titleStart == -1) {
                titleStart = html.indexOf("<h3", index);
            }
            if (titleStart == -1) {
                break;
            }
            
            // Extract title
            int titleEnd = html.indexOf("</", titleStart);
            if (titleEnd == -1) {
                break;
            }
            
            String titleHtml = html.substring(titleStart, titleEnd);
            String title = extractTextFromHtml(titleHtml);
            title = cleanText(title);
            
            if (title.isEmpty()) {
                index = titleStart + 1;
                continue;
            }
            
            // Look for author nearby
            String author = "";
            int searchEnd = Math.min(titleStart + 500, html.length());
            String nearbyHtml = html.substring(Math.max(0, titleStart - 200), searchEnd);
            
            int authorStart = nearbyHtml.indexOf("author");
            if (authorStart != -1) {
                int authorTagEnd = nearbyHtml.indexOf(">", authorStart);
                if (authorTagEnd != -1) {
                    int authorTextEnd = nearbyHtml.indexOf("<", authorTagEnd + 1);
                    if (authorTextEnd != -1) {
                        author = cleanText(nearbyHtml.substring(authorTagEnd + 1, authorTextEnd));
                    }
                }
            }
            
            // Generate ID from title (simple hash-based)
            String id = generateIdFromTitle(title);
            
            DataRecord record = new DataRecord(
                id,
                title,
                author,
                "",
                BASE_URL + "/search?q=" + title.replace(" ", "+"),
                fetchedAt,
                currentUser
            );
            
            if (!containsRecord(records, id)) {
                records.add(record);
            }
            
            index = titleEnd + 1;
        }
        
        return records;
    }
    
    /**
     * Extracts author information near a link position.
     * 
     * @param html the HTML content
     * @param linkPosition the position of the link
     * @return the author name, or empty string if not found
     */
    private String extractAuthorNearLink(String html, int linkPosition) {
        // Search in a window around the link
        int start = Math.max(0, linkPosition - 300);
        int end = Math.min(html.length(), linkPosition + 500);
        String window = html.substring(start, end);
        
        // Look for author patterns
        Pattern authorPattern = Pattern.compile(
            "(?:author|by)[^>]*>([^<]+)</",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher authorMatcher = authorPattern.matcher(window);
        if (authorMatcher.find()) {
            return cleanText(authorMatcher.group(1));
        }
        
        return "";
    }
    
    /**
     * Extracts ID from a path like /works/OL12345M.
     * 
     * @param path the path
     * @return the ID
     */
    private String extractIdFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        
        // Extract last part of path
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        
        return path;
    }
    
    /**
     * Generates a simple ID from a title (fallback method).
     * 
     * @param title the title
     * @return a generated ID
     */
    private String generateIdFromTitle(String title) {
        if (title == null || title.isEmpty()) {
            return "UNKNOWN_" + System.currentTimeMillis();
        }
        
        // Simple hash-based ID
        String clean = title.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (clean.length() > 10) {
            clean = clean.substring(0, 10);
        }
        return "GEN_" + clean + "_" + Math.abs(title.hashCode());
    }
    
    /**
     * Extracts text content from HTML.
     * 
     * @param html the HTML snippet
     * @return the text content
     */
    private String extractTextFromHtml(String html) {
        if (html == null) {
            return "";
        }
        
        // Remove HTML tags
        String text = html.replaceAll("<[^>]+>", " ");
        // Clean up whitespace
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }
    
    /**
     * Cleans text by removing extra whitespace and HTML entities.
     * 
     * @param text the text to clean
     * @return cleaned text
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        // Decode common HTML entities
        text = text.replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'")
                   .replace("&nbsp;", " ");
        
        // Remove extra whitespace
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }
    
    /**
     * Checks if a record with the given ID already exists in the list.
     * 
     * @param records the list of records
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    private boolean containsRecord(List<DataRecord> records, String id) {
        for (DataRecord record : records) {
            if (record.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}

