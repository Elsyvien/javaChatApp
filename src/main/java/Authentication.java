import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class Authentication extends User {
    private final BigInteger publicKey;
    private final BigInteger privateKey;
    private final BigInteger modulus;

    public Authentication(String name) {
        super(name);
        KeyGen.RSAKeyPair keys = KeyGen.generateKeyPair(2048);
        this.publicKey = keys.getPublicKey();
        this.privateKey = keys.getPrivateKey();
        this.modulus = keys.getModulus();
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getPrivateKey() { // This should not be referenced directly in production code
        return privateKey;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * Prove identity by encrypting a challenge from the server.
     */
    //public BigInteger respondToChallenge(String challenge) {
        //BigInteger challengeInt = new BigInteger(challenge.getBytes(StandardCharsets.UTF_8));
        //return challengeInt.modPow(privateKey, modulus); // "sign" it
    //}

    /**
     * Sendable format (Base64 or string) – for registration/login.
     */
    public String getPublicKeyString() {
        return publicKey.toString(16); // you can also Base64 it if you prefer
    }

    public String getModulusString() {
        return modulus.toString(16);
    }
}