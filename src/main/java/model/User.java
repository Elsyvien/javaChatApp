package model;

public class User {
    private String name;
    private String key; // Unique identifier for the user
    private long lastLoginTime;
    
    
    public User(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public User(String name) {
        this.name = name;
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

    public String getKey() {
        return key;
    }
}
