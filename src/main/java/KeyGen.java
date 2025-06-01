import java.math.BigInteger;
import java.security.SecureRandom;


public class KeyGen {
    public static class RSAKeyPair {
        private final BigInteger publicKey;
        private final BigInteger privateKey;
        private final BigInteger modulus;

        public RSAKeyPair(BigInteger publicKey, BigInteger privateKey, BigInteger modulus) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.modulus = modulus;
        }

        public BigInteger getPublicKey() { return publicKey; }
        public BigInteger getPrivateKey() { return privateKey; }
        public BigInteger getModulus() { return modulus; }
    }

    public static RSAKeyPair generateKeyPair(int bitLength) {
        SecureRandom random = new SecureRandom();

        BigInteger p = BigInteger.probablePrime(bitLength / 2, random);
        BigInteger q;
        do {
            q = BigInteger.probablePrime(bitLength / 2, random);
        } while (q.equals(p));

        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = BigInteger.valueOf(65537);
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO);
        }

        BigInteger d = e.modInverse(phi);

        return new RSAKeyPair(e, d, n);
    }
}