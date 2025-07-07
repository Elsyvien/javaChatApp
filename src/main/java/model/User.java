package model;

/*
 * This class represents a user in the chat application.
 * It is equivalent to the User class in the Server project.
 * @author Max Staneker, Mia Schienagel
 * It contains the user's name, a unique key, and the last login time.
 * @version 0.1 
 */
import Crypto.RSAKey;
import utils.CredentialsManager;
import java.util.Properties;

public class User {
    private String name;
    private final RSAKey key; // Unique identifier for the user
    private long lastLoginTime;

    public User(String name) {
        this(name, false, null);
    }
    
    public User(String name, boolean loadFromCredentials, Properties credentials) {
        this.name = name;
        this.key = new RSAKey(loadFromCredentials, credentials);
        
        // If new keys were generated, save them
        if (!loadFromCredentials || credentials == null) {
            CredentialsManager.saveCredentials(name, key.getN(), key.getE(), key.getD());
        }
    }

    public String getUsername() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public RSAKey getKey() {
        return key;
    }
}
