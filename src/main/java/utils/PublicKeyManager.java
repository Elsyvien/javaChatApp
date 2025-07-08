package utils;

import WebSocketHandling.ChatClientEndpoint;
import model.Message;
import model.User;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages public keys for other users in the chat application.
 * Handles automatic key retrieval, caching, and validation.
 * @author Max Staneker, Mia Schienagel
 * @version 1.5
 */
public class PublicKeyManager {
    private static final Map<String, BigInteger[]> publicKeyCache = new ConcurrentHashMap<>(); // username -> {n, e}
    private static final Map<String, CompletableFuture<BigInteger[]>> pendingRequests = new ConcurrentHashMap<>();
    
    private static ChatClientEndpoint chatClient;
    private static User currentUser;
    
    /**
     * Initialize the PublicKeyManager with the chat client and current user
     */
    public static void initialize(ChatClientEndpoint client, User user) {
        chatClient = client;
        currentUser = user;
    }
    
    /**
     * Get public key for a user. Returns cached key immediately or requests it from server.
     * @param username The username to get the public key for
     * @return CompletableFuture that resolves to the public key {n, e} or null if not found
     */
    public static CompletableFuture<BigInteger[]> getPublicKey(String username) {
        // Check cache first
        BigInteger[] cachedKey = publicKeyCache.get(username);
        if (cachedKey != null) {
            System.out.println("[KEYMANAGER] Using cached public key for: " + username);
            return CompletableFuture.completedFuture(cachedKey);
        }
        
        // Check if request is already pending
        CompletableFuture<BigInteger[]> pendingRequest = pendingRequests.get(username);
        if (pendingRequest != null) {
            System.out.println("[KEYMANAGER] Public key request already pending for: " + username);
            return pendingRequest;
        }
        
        // Create new request
        CompletableFuture<BigInteger[]> future = new CompletableFuture<>();
        pendingRequests.put(username, future);
        
        try {
            Message keyRequest = new Message(currentUser.getUsername(), "get-public-key:" + username);
            chatClient.sendMessage(keyRequest);
            System.out.println("[KEYMANAGER] Requesting public key for: " + username);
            
            // Set timeout for request
            future.orTimeout(10, TimeUnit.SECONDS);
            
        } catch (Exception ex) {
            System.err.println("[KEYMANAGER] Failed to request public key: " + ex.getMessage());
            future.completeExceptionally(ex);
            pendingRequests.remove(username);
        }
        
        return future;
    }
    
    /**
     * Handle public key response from server
     * @param response The response string in format "public-key:username:n:e"
     */
    public static void handlePublicKeyResponse(String response) {
        // Format: "public-key:username:n:e"
        String[] parts = response.split(":");
        if (parts.length == 4) {
            String username = parts[1];
            BigInteger n = new BigInteger(parts[2], 16);
            BigInteger e = new BigInteger(parts[3], 16);
            
            // Store in cache
            BigInteger[] publicKey = new BigInteger[]{n, e};
            publicKeyCache.put(username, publicKey);
            System.out.println("[KEYMANAGER] Public key received and cached for: " + username);
            
            // Complete pending future if exists
            CompletableFuture<BigInteger[]> pendingRequest = pendingRequests.remove(username);
            if (pendingRequest != null) {
                pendingRequest.complete(publicKey);
                System.out.println("[KEYMANAGER] Completed pending request for: " + username);
            }
        } else if (response.startsWith("public-key-not-found:")) {
            String username = response.substring("public-key-not-found:".length());
            System.err.println("[KEYMANAGER] Public key not found for: " + username);
            
            // Complete pending future with null
            CompletableFuture<BigInteger[]> pendingRequest = pendingRequests.remove(username);
            if (pendingRequest != null) {
                pendingRequest.complete(null);
            }
        } else {
            System.err.println("[KEYMANAGER] Invalid public key response format: " + response);
        }
    }
    
    /**
     * Get cached public key (synchronous, only returns if already cached)
     * @param username The username
     * @return The cached public key or null if not cached
     */
    public static BigInteger[] getCachedPublicKey(String username) {
        return publicKeyCache.get(username);
    }
    
    /**
     * Check if we have a public key for a user
     * @param username The username
     * @return true if we have the public key cached
     */
    public static boolean hasPublicKey(String username) {
        return publicKeyCache.containsKey(username);
    }
    
    /**
     * Preload public key for a user (fire-and-forget)
     * @param username The username to preload key for
     */
    public static void preloadPublicKey(String username) {
        if (!hasPublicKey(username)) {
            getPublicKey(username).thenAccept(key -> {
                if (key != null) {
                    System.out.println("[KEYMANAGER] Preloaded public key for: " + username);
                } else {
                    System.out.println("[KEYMANAGER] Failed to preload public key for: " + username);
                }
            });
        }
    }
    
    /**
     * Clear all cached keys (for logout/cleanup)
     */
    public static void clearCache() {
        publicKeyCache.clear();
        pendingRequests.clear();
        System.out.println("[KEYMANAGER] Cache cleared");
    }
    
    /**
     * Get cache statistics
     */
    public static void printCacheStats() {
        System.out.println("[KEYMANAGER] Cached keys: " + publicKeyCache.size() + 
                          ", Pending requests: " + pendingRequests.size());
    }
}
