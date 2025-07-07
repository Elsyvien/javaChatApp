package utils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manages user credentials and RSA keys.
 * Stores and loads user data including private/public keys.
 * Using Properties for easy serialization. (Basically a Hashmap just as a file)
 * @author Max Staneker, Mia Schienagel
 * @version 0.1
 */
public class CredentialsManager {
    private static final String CREDENTIALS_DIR = "UserData";
    private static final String CREDENTIALS_FILE = "credentials.properties";
    
    /**
     * Save user credentials including RSA keys to file
     * Now supports multiple users by using username-prefixed properties
     */
    public static void saveCredentials(String username, BigInteger n, BigInteger e, BigInteger d) {
        try {
            // Create directory if it doesn't exist
            Path credentialsDir = Paths.get(CREDENTIALS_DIR);
            if (!Files.exists(credentialsDir)) {
                Files.createDirectories(credentialsDir);
            }
            
            File credentialsFile = new File(credentialsDir.toFile(), CREDENTIALS_FILE);
            Properties props = new Properties();
            
            // Load existing credentials if file exists
            if (credentialsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(credentialsFile)) {
                    props.load(fis);
                }
            }
            
            // Add new user credentials with username prefix
            String userPrefix = "user." + username + ".";
            props.setProperty(userPrefix + "public.n", n.toString(16));
            props.setProperty(userPrefix + "public.e", e.toString(16));
            props.setProperty(userPrefix + "private.d", d.toString(16));
            props.setProperty(userPrefix + "registrationTime", String.valueOf(System.currentTimeMillis()));
            
            // Save updated properties
            try (FileOutputStream fos = new FileOutputStream(credentialsFile)) {
                props.store(fos, "Multi-User Credentials - Updated on " + new java.util.Date());
            }
            
            System.out.println("[CLIENT] Credentials saved for user: " + username);
        } catch (IOException ex) {
            System.err.println("[CLIENT] Error saving credentials: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Load user credentials from file
     * @return Properties object with user data or null if not found
     */
    public static Properties loadCredentials() {
        try {
            File credentialsFile = new File(CREDENTIALS_DIR, CREDENTIALS_FILE);
            if (!credentialsFile.exists()) {
                System.out.println("[CLIENT] No existing credentials found.");
                return null;
            }
            
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(credentialsFile)) { // Using try-with-resources for automatic closing and a File Stream
                props.load(fis);
            }
            
            String username = props.getProperty("username");
            System.out.println("[CLIENT] Credentials loaded for user: " + username);
            return props;
        } catch (IOException e) {
            System.err.println("[CLIENT] Error loading credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if credentials exist for any user
     */
    public static boolean credentialsExist() {
        File credentialsFile = new File(CREDENTIALS_DIR, CREDENTIALS_FILE);
        return credentialsFile.exists();
    }
    
    /**
     * Delete existing credentials (for testing/reset purposes)
     */
    public static void deleteCredentials() {
        File credentialsFile = new File(CREDENTIALS_DIR, CREDENTIALS_FILE);
        if (credentialsFile.exists()) {
            credentialsFile.delete();
            System.out.println("[CLIENT] Existing credentials deleted.");
        }
    }
}
