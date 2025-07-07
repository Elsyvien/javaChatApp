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
 * @author Max Staneker, Mia Schienagel
 * @version 0.1
 */
public class CredentialsManager {
    private static final String CREDENTIALS_DIR = "UserData";
    private static final String CREDENTIALS_FILE = "credentials.properties";
    
    /**
     * Save user credentials including RSA keys to file
     */
    public static void saveCredentials(String username, BigInteger n, BigInteger e, BigInteger d) {
        try {
            // Create directory if it doesn't exist
            Path credentialsDir = Paths.get(CREDENTIALS_DIR);
            if (!Files.exists(credentialsDir)) {
                Files.createDirectories(credentialsDir);
            }
            
            Properties props = new Properties();
            props.setProperty("username", username);
            props.setProperty("public.n", n.toString(16));
            props.setProperty("public.e", e.toString(16));
            props.setProperty("private.d", d.toString(16));
            
            File credentialsFile = new File(credentialsDir.toFile(), CREDENTIALS_FILE);
            try (FileOutputStream fos = new FileOutputStream(credentialsFile)) {
                props.store(fos, "User Credentials - Generated on " + new java.util.Date());
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
            try (FileInputStream fis = new FileInputStream(credentialsFile)) {
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
