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
    private final User user; //<- From User
    private String currentChallenge;

    public Authentication(User user) {
        this.user = user;
    }

    // Random Challenge String
    public String generateChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        this.currentChallenge = new BigInteger(1, bytes).toString(16); // Convert to hex string
        return this.currentChallenge;
    }

    // Sign the challenge with the user's private key
    public BigInteger signChallenge() {
       if (currentChallenge == null) {
            throw new IllegalStateException("Challenge has not been generated yet. Call generateChallenge() first.");
        }
        RSAKey key = user.getKey(); // Get the user's RSA key
        BigInteger challengeHash = new BigInteger(currentChallenge, 16); // Convert the challenge to a BigInteger
        return key.sign(challengeHash); // Sign the challenge using the user's private key
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
