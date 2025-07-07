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
