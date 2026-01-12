package cli;

import java.util.*;

/**
 * Parses command line arguments for the application.
 * Supports: --startEmpty, --loadInitialData <file>
 */
public class CommandLineParser {
    private boolean startEmpty = false;
    private String initialDataFile = null;
    private Map<String, String> options = new HashMap<>();
    
    /**
     * Parses command line arguments.
     * 
     * @param args the command line arguments
     */
    public void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if ("--startEmpty".equals(arg)) {
                startEmpty = true;
            } else if ("--loadInitialData".equals(arg)) {
                if (i + 1 < args.length) {
                    initialDataFile = args[++i];
                } else {
                    System.err.println("Warning: --loadInitialData requires a file path");
                }
            } else if (arg.startsWith("--")) {
                // Store other options
                String key = arg.substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    options.put(key, args[++i]);
                } else {
                    options.put(key, "true");
                }
            }
        }
    }
    
    /**
     * Checks if --startEmpty flag was set.
     * 
     * @return true if startEmpty flag is set
     */
    public boolean isStartEmpty() {
        return startEmpty;
    }
    
    /**
     * Gets the initial data file path.
     * 
     * @return the file path, or null if not specified
     */
    public String getInitialDataFile() {
        return initialDataFile;
    }
    
    /**
     * Gets an option value by key.
     * 
     * @param key the option key (without -- prefix)
     * @return the option value, or null if not found
     */
    public String getOption(String key) {
        return options.get(key);
    }
    
    /**
     * Checks if an option is set.
     * 
     * @param key the option key
     * @return true if option exists
     */
    public boolean hasOption(String key) {
        return options.containsKey(key);
    }
}

