package utils;

import jakarta.websocket.*;
import jakarta.websocket.ContainerProvider;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.math.BigInteger;

/**
 * WebSocket client für die Benutzername-Überprüfung und Registrierung
 * Wird vor dem eigentlichen Chat-Client verwendet
 */
@ClientEndpoint
public class RegistrationClient {
    private Session session;
    private CountDownLatch latch;
    private String result;
    private boolean connected = false;

    public RegistrationClient() {
        this.latch = new CountDownLatch(1);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.connected = true;
        System.out.println("[REGISTRATION] Connected to server");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[REGISTRATION] Server response: " + message);
        this.result = message;
        latch.countDown();
    }

    @OnClose
    public void onClose(Session session) {
        this.connected = false;
        System.out.println("[REGISTRATION] Connection closed");
        latch.countDown();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[REGISTRATION] Error: " + throwable.getMessage());
        this.result = "error:" + throwable.getMessage();
        latch.countDown();
    }

    /**
     * Überprüft, ob ein Benutzername bereits existiert
     */
    public boolean checkUsernameExists(String username) {
        if (!connected) {
            return false;
        }

        try {
            latch = new CountDownLatch(1);
            session.getBasicRemote().sendText("check-username:" + username);
            
            // Warten auf Antwort (max 5 Sekunden)
            if (latch.await(5, TimeUnit.SECONDS)) {
                return "username-exists".equals(result);
            }
        } catch (Exception e) {
            System.err.println("[REGISTRATION] Error checking username: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registriert einen neuen Benutzer auf dem Server
     * @return ServerRegistrationResult mit Erfolg/Fehler und generierten Schlüsseln
     */
    public ServerRegistrationResult registerUser(String username, BigInteger publicKeyN, BigInteger publicKeyE) {
        if (!connected) {
            return new ServerRegistrationResult(false, "Not connected to server", null, null, null);
        }

        try {
            latch = new CountDownLatch(1);
            String registrationMessage = "register:" + username + ":" + 
                publicKeyN.toString(16) + ":" + publicKeyE.toString(16);
            session.getBasicRemote().sendText(registrationMessage);
            
            // Warten auf Antwort (max 10 Sekunden)
            if (latch.await(10, TimeUnit.SECONDS)) {
                boolean success = "register-success".equals(result);
                if (success) {
                    return new ServerRegistrationResult(true, "Registration successful", publicKeyN, publicKeyE, null);
                } else {
                    return new ServerRegistrationResult(false, result != null ? result : "Unknown error", null, null, null);
                }
            } else {
                return new ServerRegistrationResult(false, "Timeout waiting for server response", null, null, null);
            }
        } catch (Exception e) {
            System.err.println("[REGISTRATION] Error registering user: " + e.getMessage());
            return new ServerRegistrationResult(false, "Error: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Ergebnis einer Server-Registrierung
     */
    public static class ServerRegistrationResult {
        public final boolean success;
        public final String message;
        public final BigInteger publicKeyN;
        public final BigInteger publicKeyE;
        public final BigInteger privateKeyD;

        public ServerRegistrationResult(boolean success, String message, BigInteger publicKeyN, BigInteger publicKeyE, BigInteger privateKeyD) {
            this.success = success;
            this.message = message;
            this.publicKeyN = publicKeyN;
            this.publicKeyE = publicKeyE;
            this.privateKeyD = privateKeyD;
        }
    }

    /**
     * Verbindet sich mit dem Server
     */
    public boolean connect(String serverUri) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(serverUri));
            
            // Warten bis Verbindung hergestellt ist
            Thread.sleep(1000);
            return connected;
        } catch (Exception e) {
            System.err.println("[REGISTRATION] Failed to connect: " + e.getMessage());
            return false;
        }
    }

    /**
     * Schließt die Verbindung
     */
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                System.err.println("[REGISTRATION] Error closing connection: " + e.getMessage());
            }
        }
    }

    public String getLastResult() {
        return result;
    }
}
