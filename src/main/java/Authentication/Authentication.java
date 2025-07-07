package Authentication;

/* 
 * This class handles user authentication on the Client side.
 * It will use the RSAKey pair and a challenge to authenticate users on Server.
 * @author Max Staneker, Mia Schienagel
 * @version 0.1
 */
import model.User;
import Crypto.RSAKey;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Authentication {
    private final User user; //← From User
    private String currentChallenge;

    public Authentication(User user) {
        this.user = user;
    }

    // Set the challenge received from the server
    public void setChallenge(String challenge) {
        this.currentChallenge = challenge;
    }

    // Sign the challenge with the user's private key
    public BigInteger signChallenge() {
        if (currentChallenge == null) {
            System.err.println("[CLIENT] AUTH ERROR: No challenge set. Use setChallenge() first.");
            throw new IllegalStateException("No challenge set. Use setChallenge() first.");
        }
        RSAKey key = user.getKey();
        

        System.out.println("[CLIENT DEBUG] Challenge HEX: " + currentChallenge);
        BigInteger challengeBigInt = new BigInteger(currentChallenge, 16);
        System.out.println("[CLIENT DEBUG] Challenge BigInt: " + challengeBigInt.toString(16));
        BigInteger signature = key.sign(challengeBigInt);
        System.out.println("[CLIENT DEBUG] Signature: " + signature.toString(16));
        return signature;
    }

    // Builds the full auth-response message to send back
    public String buildAuthResponse() {
        String signatureHex = handleChallenge(currentChallenge);
        return "auth-response:" + signatureHex + ":" + user.getUsername();
    }

    // Verify the signed challenge with the user's public key
    public boolean verify(String challenge, BigInteger signature) {
        BigInteger challengeBigInt = new BigInteger(challenge, 16); // Correct HEX decoding
        RSAKey key = user.getKey();
        return key.verify(challengeBigInt, signature);
    }

    public String getCurrentChallenge() {
        return currentChallenge; // Return the current challenge
    }
    
    public User getUser() {
        return user; // Return the user object
    }

    // Handle the challenge by hashing and signing it
    public String handleChallenge(String challenge) {
        try {
            System.out.println("[CLIENT DEBUG] Original challenge: " + challenge);
            
            // The challenge is received as a hex string, convert it to bytes first
            byte[] challengeBytes = hexStringToByteArray(challenge);
            System.out.println("[CLIENT DEBUG] Challenge bytes length: " + challengeBytes.length);
            
            // Hash the challenge bytes
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedChallenge = digest.digest(challengeBytes);
            BigInteger hashedChallengeBigInt = new BigInteger(1, hashedChallenge);
            System.out.println("[CLIENT DEBUG] Hashed challenge: " + hashedChallengeBigInt.toString(16));

            // Sign the hashed challenge with the private key
            BigInteger signature = user.getKey().sign(hashedChallengeBigInt);
            System.out.println("[CLIENT DEBUG] Signature: " + signature.toString(16));

            // Return the signature as a hex string
            return signature.toString(16);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[CLIENT/Auth] SHA-256 algorithm not found: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Helper method to convert hex string to byte array
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
