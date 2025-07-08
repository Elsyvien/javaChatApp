package Crypto;

/*
 * This Class is going to be used to generate RSA keys.
 * It will contain the public key (n, e) and the private key (n, d).
 * @author Max Staneker, Mia Schienagel
 */

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;


public class RSAKey {
    private final BigInteger n;
    private final BigInteger e; // Public exponent
    private final BigInteger d; // Private exponent

    public RSAKey() {
        this(false, null);
    }
    
    public RSAKey(boolean loadFromCredentials, Properties credentials) {
        if (loadFromCredentials && credentials != null) {
            // Load existing keys from credentials
            this.n = new BigInteger(credentials.getProperty("public.n"), 16);
            this.e = new BigInteger(credentials.getProperty("public.e"), 16);
            this.d = new BigInteger(credentials.getProperty("private.d"), 16);
            System.out.println("[CLIENT] RSA keys loaded from credentials");
        } else {
            // Generate new keys
            BigInteger[] keys = generateNewKeys();
            this.n = keys[0];
            this.e = keys[1];
            this.d = keys[2];
            System.out.println("[CLIENT] New RSA keys generated");
        }
    }
    
    private BigInteger[] generateNewKeys() {
        SecureRandom random = new SecureRandom(); // Allows for secure random number generation
        
        BigInteger p = BigInteger.probablePrime(512, random); // Generate a probable prime number p
        BigInteger q = BigInteger.probablePrime(512, random); // Generate a probable prime number q
        
        // Ensure p and q are different
        while (p.equals(q)) {
            q = BigInteger.probablePrime(512, random);
        }
        
        BigInteger n = p.multiply(q); // Calculate n = p * q
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // Calculate Euler's totient function φ(n) = (p-1)(q-1)
        BigInteger e = BigInteger.valueOf(65537); // Common choice for e, must be coprime to φ(n)
        BigInteger d = e.modInverse(phi); // Calculate d, the modular multiplicative inverse of e mod φ(n)
        
        System.out.println("[CLIENT] Generated RSA key pair with n=" + n.toString(16).substring(0, 16) + "...");
        
        return new BigInteger[]{n, e, d};
    }

    public BigInteger sign(BigInteger message) {
        return message.modPow(d, n);
    }

    public boolean verify(BigInteger message, BigInteger signature) {
        BigInteger computedHash = signature.modPow(e, n); // Verify the signature using the public key (n, e)
        return computedHash.equals(message); // Check if the computed hash matches the original hash
    }
    
    public String encryptString(String plaintext) {
        byte[] bytes = plaintext.getBytes(); // Convert the plaintext string to bytes
        BigInteger message = new BigInteger(1, bytes);

        // Check if the message is too large for RSA encryption
        if (message.compareTo(n) >= 0) { // A message is too large if it is greater than or equal to n which is approximately 2048 bits
            System.err.println("[ENCRYPTION/CRITICAL] Message is too large for RSA encryption!");
            throw new IllegalArgumentException("Message is too large for RSA encryption");
        }

        // Encrypt the message using the public key (n, e)
        BigInteger encryptedMessage = message.modPow(e, n);
        return encryptedMessage.toString(16); // Return the encrypted message as a hexadecimal string
    }

    public String decryptString(String encryptedHex) {
        BigInteger encryptedMessage = new BigInteger(encryptedHex, 16); // Convert the hexadecimal string back to a BigInteger
        BigInteger decryptedMessage = encryptedMessage.modPow(d, n); // Decrypt the message using the private key (n, d)
        return new String(decryptedMessage.toByteArray()); // Convert the decrypted BigInteger back to a string
    }
    
    public String encryptWithPublicKey(String plaintext, BigInteger publicKeyN, BigInteger publicKeyE) {
        byte[] bytes = plaintext.getBytes(); // Convert the plaintext string to bytes
        BigInteger message = new BigInteger(1, bytes);

        // Check if the message is too large for RSA encryption
        if (message.compareTo(publicKeyN) >= 0) { // A message is too large if it is greater than or equal to n which is approximately 2048 bits
            System.err.println("[ENCRYPTION/CRITICAL] Message is too large for RSA encryption!");
            throw new IllegalArgumentException("Message is too large for RSA encryption");
        }

        // Encrypt the message using the public key (n, e)
        BigInteger encryptedMessage = message.modPow(publicKeyE, publicKeyN);
        return encryptedMessage.toString(16); // Return the encrypted message as a hexadecimal string
    }
    
    // Chunked encryption for longer messages
    private static final int MAX_CHUNK_SIZE = 100; // Safe buffer for 1024-bit RSA
    
    /*
     * Encrypts a long string by splitting it into chunks if necessary.
     * If the string is shorter than or equal to MAX_CHUNK_SIZE, it uses normal
     * @param plaintext The plaintext string to encrypt.
     * @param recipientN The modulus n of the recipient's public key.
     * @param recipientE The public exponent e of the recipient's public key.
     * @return The encrypted string, either as a single chunk or as a chunked message
     */
    public String encryptLongString(String plaintext, BigInteger recipientN, BigInteger recipientE) {
        if (plaintext.length() <= MAX_CHUNK_SIZE) {
            // Short message: normal encryption
            return encryptWithPublicKey(plaintext, recipientN, recipientE);
        }
        
        // Long message: split into chunks
        StringBuilder result = new StringBuilder();
        int offset = 0;
        // Iterate through the plaintext string in chunks
        while (offset < plaintext.length()) { 
            int endIndex = Math.min(offset + MAX_CHUNK_SIZE, plaintext.length()); // Ensure we don't go out of bounds
            String chunk = plaintext.substring(offset, endIndex); // Extract the current chunk
            
            String encryptedChunk = encryptWithPublicKey(chunk, recipientN, recipientE);
            
            if (result.length() > 0) { // Append a separator if this is not the first chunk
                result.append("|"); // Chunk separator
            }
            result.append(encryptedChunk); // Add the Chunks together
            
            offset = endIndex; // Move to the next chunk
        }
        
        return "CHUNKED:" + result.toString();
    }
    /* 
     * Decypts a long string that may be chunked.
     * If the string starts with "CHUNKED:", it splits the string into chunks,
     * decrypts each chunk, and concatenates the results.
     * @param ciphertext The encrypted string, which may be chunked.
     * @return The decrypted string, either as a single chunk or as a concatenated result
     * Note: This method assumes that the ciphertext is either a single encrypted string or a chunked message 
     */
    public String decryptLongString(String ciphertext) {
        if (ciphertext.startsWith("CHUNKED:")) {
            // Chunked message: split and decrypt each part
            String chunkedData = ciphertext.substring(8); // Remove "CHUNKED:" prefix
            String[] chunks = chunkedData.split("\\|");
            
            StringBuilder result = new StringBuilder();
            for (String chunk : chunks) {
                result.append(decryptString(chunk));
            }
            return result.toString();
        } else {
            // Normal single chunk
            return decryptString(ciphertext);
        }
    }
    
    public BigInteger getN() {
        return n; // Get the modulus n
    }

    public BigInteger getE() {
        return e; // Get the public exponent e
    }
    
    public BigInteger getD() {
        return d; // Get the private exponent d
    }
}
