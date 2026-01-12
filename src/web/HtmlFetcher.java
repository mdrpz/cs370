package web;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fetches HTML content from a URL using HttpURLConnection.
 * Saves the raw HTML to a temporary file as required.
 */
public class HtmlFetcher {
    private static final int TIMEOUT_MS = 10000; // 10 seconds
    private static final String TEMP_DIR = "temp";
    private static final String TEMP_FILE_PREFIX = "html_";
    private static final String TEMP_FILE_SUFFIX = ".html";
    
    /**
     * Fetches HTML content from the given URL.
     * Saves the HTML to a temporary file and returns the file path.
     * 
     * @param urlString the URL to fetch
     * @return the path to the temporary file containing the HTML, or null if fetch failed
     * @throws IOException if network error or file I/O error occurs
     */
    public String fetch(String urlString) throws IOException {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        URL url = new URL(urlString);
        HttpURLConnection connection = null;
        
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            
            // Read HTML content
            StringBuilder htmlContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line).append("\n");
                }
            }
            
            // Save to temporary file
            String html = htmlContent.toString();
            Path tempFile = saveToTempFile(html);
            
            return tempFile.toString();
            
        } catch (java.net.UnknownHostException e) {
            throw new IOException("Unable to connect to website. Please check your internet connection.", e);
        } catch (java.net.ConnectException e) {
            throw new IOException("Connection timeout. Please check your internet connection.", e);
        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("Request timeout. The website may be slow or unavailable.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Builds a search URL for OpenLibrary.
     * 
     * @param query the search query
     * @return the complete URL
     * @throws UnsupportedEncodingException if URL encoding fails
     */
    public String buildSearchUrl(String query) throws UnsupportedEncodingException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        return "https://openlibrary.org/search?q=" + encodedQuery;
    }
    
    /**
     * Saves HTML content to a temporary file.
     * 
     * @param htmlContent the HTML content to save
     * @return the Path to the temporary file
     * @throws IOException if file creation fails
     */
    private Path saveToTempFile(String htmlContent) throws IOException {
        // Create temp directory if it doesn't exist
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        // Create temporary file
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = TEMP_FILE_PREFIX + timestamp + TEMP_FILE_SUFFIX;
        File tempFile = new File(tempDir, filename);
        
        // Write HTML content
        try (BufferedWriter writer = Files.newBufferedWriter(
                tempFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
        
        return tempFile.toPath();
    }
    
    /**
     * Reads HTML content from a file.
     * 
     * @param filePath the path to the HTML file
     * @return the HTML content as a string
     * @throws IOException if file cannot be read
     */
    public String readFromFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        Path path = Path.of(filePath);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

