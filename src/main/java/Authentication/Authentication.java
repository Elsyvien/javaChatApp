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
        BigInteger signature = signChallenge();
        return "auth-response:" + signature.toString(16) + ":" + user.getUsername();
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
}
