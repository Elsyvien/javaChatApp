import WebSocketHandling.ChatClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;
/*
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
*/
import utils.ToggleSwitches; 
import model.Message;
import model.User;
import Authentication.Authentication;
import utils.LoginDialog;
import utils.CredentialsManager;
import java.util.Properties;
import java.math.BigInteger;

public class MChat {
    // Stores the current chat partner's username
    private static String currentChatPartner = "";
    
    // Chat management
    private static Map<String, JTextPane> chatTabs = new HashMap<>();
    private static JTabbedPane tabbedPane;
    
    // Current settings for the chat client
    private static boolean morseMode = false; // Morse code mode 
    
    // Encryption key management
    private static Map<String, BigInteger[]> publicKeys = new HashMap<>(); // username -> {n, e}
    private static User user; // Make user accessible throughout the class
    private static ChatClientEndpoint chatClient; // Make chatClient accessible throughout the class 
    /**
     * Main method to start the chat client application.
     * It checks for existing user credentials, allows user login or registration,
     * and sets up the WebSocket connection to the chat server.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {

        // Always show login dialog (supports login and registration)
        LoginDialog loginDialog = new LoginDialog(null);
        String username = loginDialog.showDialog();
        if (username == null) {
            System.err.println("[CLIENT] No username provided, exiting.");
            JOptionPane.showMessageDialog(null, "No username provided, exiting.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Load RSA credentials for this user if they exist
        Properties userCredentials = CredentialsManager.loadCredentials(username);
        if (userCredentials != null) {
            System.out.println("[CLIENT] Loaded credentials for user: " + username);
            user = new User(username, true, userCredentials);
        } else {
            System.out.println("[CLIENT] No credentials found for user: " + username + " - generating new keys.");
            user = new User(username); // generates and saves new credentials
        }

        boolean isNewUser = false; // kept for compatibility, ChatClientEndpoint ignores this
        
        JOptionPane.showMessageDialog(null, "Welcome " + username + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("[CLIENT] Username: " + username);
        System.out.println("[CLIENT] User created: " + user.getUsername() + "Continuing with chat client setup...");
        Authentication authentication = new Authentication(user);
        
        // Only one ChatClientEndpoint, constructed with authentication
        chatClient = new ChatClientEndpoint(authentication, isNewUser);
        System.out.println("[CLIENT] Public key: " + user.getKey().getE().toString(16) + " " + user.getKey().getN().toString(16));
        
        String currentURI = "ws://localhost:8081/Gradle___com_maxstaneker_chatapp___chatApp_backend_1_0_SNAPSHOT_war/chat";

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        try {
            container.connectToServer(chatClient, URI.create(currentURI)); // Connect to the WebSocket server
        } catch (jakarta.websocket.DeploymentException | java.io.IOException e) {
            System.err.println("[CLIENT] Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame frame = new JFrame("Chat Client - Mehrere Chats");
        JTextField messageField = new JTextField(30);
        JButton sendButton = new JButton("Senden");
        JButton newChatButton = new JButton("Neuer Chat");

        // Create toggle Button for Morse code mode
        ToggleSwitches morseToggle = new ToggleSwitches();
        morseToggle.setOn(false); // Default is off
        
        // Label for the toggle switch
        JLabel morseLabel = new JLabel("Morse Code Modus:");

        // Add item listener to toggle switch
        morseToggle.addToggleListener(isOn -> {
            if (isOn) {
                morseMode = true;
                System.out.println("[CLIENT] Morse Code Mode enabled.");
            } else {
                morseMode = false;
                System.out.println("[CLIENT] Morse Code Mode disabled.");
            }
        });

        // Create tabbed pane for multiple chats
        tabbedPane = new JTabbedPane();
        
        // Add welcome tab
        JTextPane welcomePane = new JTextPane();
        welcomePane.setEditable(false);
        welcomePane.setText("Willkommen " + username + "!\n\nKlicke auf 'Neuer Chat' um ein Gespräch zu beginnen.");
        JScrollPane welcomeScroll = new JScrollPane(welcomePane);
        tabbedPane.addTab("Willkommen", welcomeScroll);

        // Erstelle verschiedene Panels für bessere Anordnung
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel für die Nachrichteneingabe (unten)
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        
        // Panel für den Button (ganz unten)
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newChatButton);
        buttonPanel.add(morseLabel);
        buttonPanel.add(morseToggle);
        
        // Kombiniere beide untere Panels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Alles zusammenfügen
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        // Add right-click close functionality to tabs
        addTabCloseFeature();

        newChatButton.addActionListener(e -> {
            // Create popup asking for username of the person to chat with
            String chatWithUsername = JOptionPane.showInputDialog(
                frame, // parent component
                "Bitte gib den Benutzernamen ein:", // message
                "Neuer Chat", // title
                JOptionPane.PLAIN_MESSAGE // message type
            );
            
            if (chatWithUsername == null || chatWithUsername.trim().isEmpty()) {
                System.out.println("[CLIENT] No username provided for new chat.");
                JOptionPane.showMessageDialog(frame, "Fehler: kein Username angegeben", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String chatPartner = chatWithUsername.trim();
            
            // Check if tab already exists
            if (chatTabs.containsKey(chatPartner)) {
                // Switch to existing tab
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getTitleAt(i).equals(chatPartner)) {
                        tabbedPane.setSelectedIndex(i);
                        currentChatPartner = chatPartner;
                        frame.setTitle("Chat Client - Chat mit " + currentChatPartner);
                        break;
                    }
                }
                return;
            }
            
            System.out.println("[CLIENT] Starting new chat with: " + chatPartner);
            
            // Create new chat tab
            JTextPane chatPane = new JTextPane();
            chatPane.setEditable(false);
            JScrollPane chatScroll = new JScrollPane(chatPane);
            
            // Add to tab management
            chatTabs.put(chatPartner, chatPane);
            tabbedPane.addTab(chatPartner, chatScroll);
            
            // Switch to new tab
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            currentChatPartner = chatPartner;
            frame.setTitle("Chat Client - Chat mit " + currentChatPartner);

            // Notify Server about the new Chat and request public key
            try {
                Message chatInitMessage = new Message(user.getUsername(), "init-chat:" + currentChatPartner);
                System.out.println("[CLIENT] Sending chat initialization message: " + chatInitMessage);
                chatClient.sendMessage(chatInitMessage);
                
                // Request public key for encryption
                requestPublicKey(currentChatPartner);
            } catch (Exception ex) {
                System.err.println("[CLIENT] Error sending chat initialization message: " + ex.getMessage());
                JOptionPane.showMessageDialog(frame, "Fehler beim Starten des Chats: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
        });

        
        // Add tab change listener to update current chat partner
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex > 0) { // Skip welcome tab (index 0)
                String tabTitle = tabbedPane.getTitleAt(selectedIndex);
                // Remove new message indicator (*)
                if (tabTitle.endsWith(" *")) {
                    tabTitle = tabTitle.substring(0, tabTitle.length() - 2);
                    tabbedPane.setTitleAt(selectedIndex, tabTitle);
                }
                currentChatPartner = tabTitle;
                frame.setTitle("Chat Client - Chat mit " + currentChatPartner);
            } else {
                currentChatPartner = "";
                frame.setTitle("Chat Client - Mehrere Chats");
            }
        });

        // Send on button click
        sendButton.addActionListener(e -> {
            String originalMessage = messageField.getText();
        
            if (originalMessage.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Fehler: Nachricht ist leer", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (currentChatPartner.isEmpty()) {
                System.err.println("[CLIENT] No chat partner selected, cannot send message!");
                JOptionPane.showMessageDialog(frame, "Fehler: Kein Chatpartner ausgewählt", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 1. Apply Morse code if enabled
            String processedMessage = originalMessage;
            if (morseMode) {
                processedMessage = utils.Morsecode.toMorse(originalMessage);
                System.out.println("[CLIENT] Converted to Morse: " + processedMessage);
            }
            
            // 2. Apply encryption
            String finalMessage = processedMessage;
            BigInteger[] recipientKey = publicKeys.get(currentChatPartner);
            
            if (recipientKey != null) {
                try {
                    finalMessage = user.getKey().encryptLongString(processedMessage, recipientKey[0], recipientKey[1]);
                    System.out.println("[CLIENT] Message encrypted for: " + currentChatPartner);
                } catch (Exception ex) {
                    System.err.println("[CLIENT] Encryption failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(frame, "Verschlüsselung fehlgeschlagen: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Request public key first
                requestPublicKey(currentChatPartner);
                JOptionPane.showMessageDialog(frame, "Lade Verschlüsselungsschlüssel...", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Message message = new Message(user.getUsername(), finalMessage, currentChatPartner);
            chatClient.sendMessage(message);
            messageField.setText("");

            // Display the sent message in the current chat tab (show original message)
            SwingUtilities.invokeLater(() -> {
                JTextPane currentChatPane = chatTabs.get(currentChatPartner);
                if (currentChatPane != null) {
                    String currentText = currentChatPane.getText();
                    if (currentText == null || currentText.trim().isEmpty()) {
                        currentText = "";
                    }
                    String newText = currentText + "Du: " + originalMessage + " 🔒\n";
                    currentChatPane.setText(newText);
                    
                    // Auto-scroll to bottom
                    currentChatPane.setCaretPosition(currentChatPane.getDocument().getLength());
                }
            });
        });

        // Waiting for new Messages
        chatClient.setMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Handle public key responses
                if (message.getContent().startsWith("public-key:")) {
                    handlePublicKeyResponse(message.getContent());
                    return;
                }
                
                // Handle system messages (like chat initialization confirmations)
                if (message.getContent().startsWith("chat-init-success:")) {
                    String confirmedPartner = message.getContent().substring("chat-init-success:".length());
                    System.out.println("[CLIENT] Chat initialization confirmed for: " + confirmedPartner);
                    JOptionPane.showMessageDialog(frame, "Chat mit " + confirmedPartner + " erfolgreich gestartet.", "Chat bereit", JOptionPane.INFORMATION_MESSAGE);
                    return;
                } else if (message.getContent().startsWith("chat-init-failure:")) {
                    String errorMessage = message.getContent().substring("chat-init-failure:".length());
                    System.err.println("[CLIENT] Chat initialization failed: " + errorMessage);
                    JOptionPane.showMessageDialog(frame, "Chat-Start fehlgeschlagen: " + errorMessage, "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Handle regular chat messages (encrypted)
                String sender = message.getSender();
                if (!sender.equals("system")) {
                    // Get or create chat tab for this sender
                    JTextPane chatPane = chatTabs.get(sender);
                    if (chatPane == null) {
                        // Create new tab for unknown sender
                        chatPane = new JTextPane();
                        chatPane.setEditable(false);
                        JScrollPane chatScroll = new JScrollPane(chatPane);
                        
                        chatTabs.put(sender, chatPane);
                        tabbedPane.addTab(sender, chatScroll);
                        
                        // Show notification for new chat
                        JOptionPane.showMessageDialog(frame, "Neue Nachricht von " + sender, "Neue Nachricht", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Request public key for this new sender
                        requestPublicKey(sender);
                    }
                    
                    // Decrypt the message
                    String encryptedContent = message.getContent();
                    String decryptedContent = encryptedContent;
                    
                    try {
                        // Decrypt with own private key
                        decryptedContent = user.getKey().decryptLongString(encryptedContent);
                        System.out.println("[CLIENT] Message decrypted from: " + sender);
                        
                        // Decode Morse if detected
                        if (decryptedContent.matches(".*[.-]{2,}.*")) {
                            String decodedMorse = utils.Morsecode.fromMorse(decryptedContent);
                            decryptedContent = decodedMorse + " (Morse decoded)";
                        }
                        
                    } catch (Exception ex) {
                        System.err.println("[CLIENT] Decryption failed: " + ex.getMessage());
                        decryptedContent = "[Verschlüsselte Nachricht - Entschlüsselung fehlgeschlagen]";
                    }
                    
                    // Add message to the appropriate chat tab
                    String currentText = chatPane.getText();
                    if (currentText == null || currentText.trim().isEmpty()) {
                        currentText = "";
                    }
                    
                    String displayMessage = sender + ": " + decryptedContent + " 🔒\n";
                    String newText = currentText + displayMessage;
                    System.out.println("[MESSAGE HANDLING] Received encrypted message from: " + sender);
                    chatPane.setText(newText);
                    
                    // Auto-scroll to bottom
                    chatPane.setCaretPosition(chatPane.getDocument().getLength());
                    
                    // Highlight tab if not currently selected
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        if (tabbedPane.getTitleAt(i).equals(sender)) {
                            if (tabbedPane.getSelectedIndex() != i) {
                                // Add visual indicator for new message (could be enhanced with colors)
                                tabbedPane.setTitleAt(i, sender + " *");
                            }
                            break;
                        }
                    }
                }
            });
        });

    }

    // Helper method to request public key from server
    private static void requestPublicKey(String username) {
        try {
            Message keyRequest = new Message(user.getUsername(), "get-public-key:" + username);
            chatClient.sendMessage(keyRequest);
            System.out.println("[CLIENT] Requesting public key for: " + username);
        } catch (Exception ex) {
            System.err.println("[CLIENT] Failed to request public key: " + ex.getMessage());
        }
    }
    
    // Helper method to handle public key responses
    private static void handlePublicKeyResponse(String response) {
        // Format: "public-key:username:n:e"
        String[] parts = response.split(":");
        if (parts.length == 4) {
            String username = parts[1];
            BigInteger n = new BigInteger(parts[2], 16);
            BigInteger e = new BigInteger(parts[3], 16);
            
            // Store public key for later use
            publicKeys.put(username, new BigInteger[]{n, e});
            System.out.println("[CLIENT] Public key received and stored for: " + username);
        } else {
            System.err.println("[CLIENT] Invalid public key response format: " + response);
        }
    }

    // Helper method to add close functionality to tabs
    private static void addTabCloseFeature() {
        tabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex > 0) { // Don't allow closing welcome tab
                        String tabTitle = tabbedPane.getTitleAt(tabIndex);
                        if (tabTitle.endsWith(" *")) {
                            tabTitle = tabTitle.substring(0, tabTitle.length() - 2);
                        }
                        
                        int result = JOptionPane.showConfirmDialog(
                            tabbedPane,
                            "Chat mit " + tabTitle + " schließen?",
                            "Chat schließen",
                            JOptionPane.YES_NO_OPTION
                        );
                        
                        if (result == JOptionPane.YES_OPTION) {
                            chatTabs.remove(tabTitle);
                            tabbedPane.removeTabAt(tabIndex);
                            
                            // Update current chat partner if this was the active tab
                            if (tabTitle.equals(currentChatPartner)) {
                                currentChatPartner = "";
                            }
                        }
                    }
                }
            }
        });
    }


    // Deprecated method for sending messages to the server using HTTP POST
    /*public static void sendMessageToServer(String sender, String content) {
    String sender = "User"; // Replace with actual sender name
        try {
            String data = "sender=" + sender + "&content=" + content + System.currentTimeMillis();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/chatapp/chat"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> System.out.println("Server response: " + response.body()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}