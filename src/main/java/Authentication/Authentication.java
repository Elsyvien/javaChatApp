package Authentication;

/* 
 * This class handles user authentication on the Client side.
 * It will use the RSAKey pair and a challenge to authenticate users on Server.
 * @author Max Staneker, Mia Schienagel
 * @version 0.1
 */
import model.User;
import Crypto.RSAKey;
import java.security.SecureRandom;
import java.math.BigInteger;


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
            throw new IllegalStateException("No challenge set. Use setChallenge() first.");
        }
        RSAKey key = user.getKey();
        BigInteger challengeHash = new BigInteger(currentChallenge, 16);
        return key.sign(challengeHash);
    }

    // Builds the full auth-response message to send back
    public String buildAuthResponse() {
        BigInteger signature = signChallenge();
        return "auth-response:" + signature.toString(16) + ":" + user.getUsername();
    }

    // Verify the signed challenge with the user's public key
    public boolean verify(String challenge, BigInteger signature) {
        BigInteger hash = new BigInteger(challenge.getBytes()); // Convert the challenge to a BigInteger
        RSAKey key = user.getKey(); // Get the user's RSA key
        return key.verify(hash, signature); // Verify the signature using the user's public key
    }

    public String getCurrentChallenge() {
        return currentChallenge; // Return the current challenge
    }
}
