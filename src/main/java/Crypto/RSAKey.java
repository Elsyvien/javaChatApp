package Crypto;

/*
 * This Class is going to be used to generate RSA keys.
 * It will contain the public key (n, e) and the private key (n, d).
 * @author Max Staneker, Mia Schienagel
 */

import java.math.BigInteger;
import java.security.SecureRandom;


public class RSAKey {
    private final BigInteger n;
    private final BigInteger e; // Public exponent
    private final BigInteger d; // Private exponent

    public RSAKey() {
        /*
         * ORIGINAL CODE:
         * SecureRandom random = new SecureRandom(); // Allows for secure random number generation
         * 
         * BigInteger p = BigInteger.probablePrime(512, random); // Generate a probable prime number p
         * BigInteger q = BigInteger.probablePrime(512, random); // Generate a probable prime number q
         * 
         * if (p.equals(q)) { // Highly Unlikely, but check if p and q are distinct. Safety check
         *     throw new IllegalArgumentException("RSA KEYGEN ERROR: p and q must be distinct prime numbers.");
         * }
         * n = p.multiply(q); // Calculate n = p * q, multiply is safer than using the `*` operator directly
         * 
         * BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // Calculate Euler's totient function φ(n) = (p-1)(q-1)
         * e = BigInteger.valueOf(65537); // Common choice for e, must be coprime to φ(n)
         * d = e.modInverse(phi); // Calculate d, the modular multiplicative inverse of e mod φ(n)
         */

        // DEBUG VERSION: Use fixed primes for consistent keys during debugging
        BigInteger p = new BigInteger("F7E75FDC469067FFDC4E847C51F452DF", 16);
        BigInteger q = new BigInteger("E85CED54AF57E53E092113E62F436F4F", 16);

        n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.valueOf(65537);
        d = e.modInverse(phi);
    }

    public BigInteger sign(BigInteger hash) { 
        return hash.modPow(d, n); // Sign the hash using the private key (n, d)
    }

    public boolean verify(BigInteger hash, BigInteger signature) {
        BigInteger computedHash = signature.modPow(e, n); // Verify the signature using the public key (n, e)
        return computedHash.equals(hash); // Check if the computed hash matches the original hash
    }
    
    public BigInteger getN() {
        return n; // Get the modulus n
    }

    public BigInteger getE() {
        return e; // Get the public exponent e
    }
}
