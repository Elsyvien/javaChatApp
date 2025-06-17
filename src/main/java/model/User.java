package model;

/*
 * This class represents a user in the chat application.
 * It is equivalent to the User class in the Server project.
 * @author Max Staneker, Mia Schienagel
 * It contains the user's name, a unique key, and the last login time.
 * @version 0.1 
 */
import Crypto.RSAKey;

public class User {
    private String name;
    private final RSAKey key; // Unique identifier for the user
    private long lastLoginTime;

    public User(String name) {
        this.name = name;
        this.key = new RSAKey(); // Generate a new RSA key for the user 
    }

    public String getName() {
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
