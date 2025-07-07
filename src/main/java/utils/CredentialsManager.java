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
     * Load credentials for a specific user from file
     * @param username the username to load credentials for
     * @return Properties object containing only the credentials of the given user
     */
    public static Properties loadCredentials(String username) {
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

            String prefix = "user." + username + ".";
            if (!props.containsKey(prefix + "public.n")) {
                System.out.println("[CLIENT] No credentials found for user: " + username);
                return null;
            }

            Properties userProps = new Properties();
            userProps.setProperty("username", username);
            userProps.setProperty("public.n", props.getProperty(prefix + "public.n"));
            userProps.setProperty("public.e", props.getProperty(prefix + "public.e"));
            userProps.setProperty("private.d", props.getProperty(prefix + "private.d"));
            userProps.setProperty("registrationTime", props.getProperty(prefix + "registrationTime"));

            System.out.println("[CLIENT] Credentials loaded for user: " + username);
            return userProps;
        } catch (IOException e) {
            System.err.println("[CLIENT] Error loading credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load all credentials from file without filtering by user
     * @return Properties of the whole credentials file or null if none exist
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

            return props;
        } catch (IOException e) {
            System.err.println("[CLIENT] Error loading credentials: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a list of usernames for which credentials exist
     */
    public static java.util.List<String> getRegisteredUsernames() {
        java.util.List<String> users = new java.util.ArrayList<>();
        try {
            File credentialsFile = new File(CREDENTIALS_DIR, CREDENTIALS_FILE);
            if (!credentialsFile.exists()) {
                return users;
            }
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(credentialsFile)) {
                props.load(fis);
            }

            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("user.") && key.endsWith(".public.n")) {
                    String name = key.substring("user.".length(), key.length() - ".public.n".length());
                    if (!users.contains(name)) {
                        users.add(name);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Error reading credentials: " + e.getMessage());
        }
        return users;
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
