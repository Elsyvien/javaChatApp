import WebSocketHandling.ChatClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
/*
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
*/
import model.Message;
import model.User;
import Authentication.Authentication;
import utils.LoginDialog;
import utils.CredentialsManager;
import java.util.Properties;

public class MChat {
    // Stores the current chat partner's username
    private static String currentChatPartner = "";

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
        User user;
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
        ChatClientEndpoint chatClient = new ChatClientEndpoint(authentication, isNewUser);
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

        JFrame frame = new JFrame("Chat Client");
        JTextField messageField = new JTextField(30);
        JButton sendButton = new JButton("Senden");
        JTextPane messageReceiver = new JTextPane();
        JButton newChatButton = new JButton("Neuer Chat");

        // Erstelle verschiedene Panels für bessere Anordnung
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel für die Nachrichteneingabe (unten)
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        
        // Panel für den Button (ganz unten)
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newChatButton);
        
        // Kombiniere beide untere Panels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Scroll-Panel für die Nachrichten (Mitte)
        JScrollPane scrollPane = new JScrollPane(messageReceiver);
        messageReceiver.setEditable(false);
        
        // Initial verstecken - wird sichtbar bei der ersten Nachricht
        scrollPane.setVisible(true);
        
        // Alles zusammenfügen
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        newChatButton.addActionListener(e -> {
            // Clear the message receiver and hide the scroll pane
            scrollPane.setVisible(false);
            // Create popup Asking for Username of the person to chat with
            String chatWithUsername = JOptionPane.showInputDialog(
                frame, // parent component
                "Bitte gib den Benutzernamen ein:", // message
                "Neuer Chat", // title
                JOptionPane.PLAIN_MESSAGE // message type
            );
            if (chatWithUsername.isEmpty() == true) {
                System.out.println("[CLIENT] No username provided for new chat.");
                JOptionPane.showMessageDialog(frame, "Fehler: kein Username angegeben", "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("[CLIENT] Starting new chat with: " + chatWithUsername);
                MChat.currentChatPartner = chatWithUsername.trim(); 
                // Set Current Chat Partner
                currentChatPartner = chatWithUsername.trim(); 
                messageReceiver.setText("");
                
                frame.setTitle("Chat Client - Chat mit" + currentChatPartner);

                // Make ScrollPane visible again
                scrollPane.setVisible(true);
                frame.revalidate(); // Layout neu berechnen
                frame.repaint();    // Fenster neu zeichnen

                // Notify Server about the new Chat and context Switch
                try {
                    Message chatInitMessage = new Message(user.getUsername(), "init-chat:" + currentChatPartner);
                    System.out.println("[CLIENT] Sending chat initialization message: " + chatInitMessage);
                    chatClient.sendMessage(chatInitMessage); 
                } catch (Exception ex) {
                    System.err.println("[CLIENT] Error sending chat initialization message: " + ex.getMessage());
                    JOptionPane.showMessageDialog(frame, "Fehler beim Starten des Chats: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                    return; // Do not proceed
                }
                JOptionPane.showMessageDialog(frame, "Neuer Chat mit " + chatWithUsername + " gestartet.", "Neuer Chat", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Send on button click
        sendButton.addActionListener(e -> {
            String messageString = messageField.getText();
            if (messageString.isEmpty() == true) {
                JOptionPane.showMessageDialog(frame, "Fehler: Nachricht ist leer", "Fehler", JOptionPane.ERROR_MESSAGE);
                return; // Do not send empty messages
            }
            if (currentChatPartner.isEmpty()) {
                System.err.println("[CLIENT] No chat partner selected, cannot send message!");
                JOptionPane.showMessageDialog(frame, "Fehler: Kein Chatpartner ausgewählt", "Fehler", JOptionPane.ERROR_MESSAGE);
                return; // Do not send messages without a chat partner
            }
            // Message message = new Message(user.getUsername(), messageString); deprecated after direct messaging capabilities were added
            Message message = new Message(user.getUsername(), messageString, currentChatPartner);
            chatClient.sendMessage(message); // Send the message using the WebSocket client
            messageField.setText("");

            // Display the sent message in the message receiver
            SwingUtilities.invokeLater(() -> {
            String currentText = messageReceiver.getText();
            if (currentText == null || currentText.trim().isEmpty()) {
                currentText = "";
            }
            String newText = currentText + "Du: " + messageString + "\n";
            messageReceiver.setText(newText);
            
            // Auto-scroll to bottom
            messageReceiver.setCaretPosition(messageReceiver.getDocument().getLength());
        });
        });

        // Waiting for new Messages
        chatClient.setMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Only display the messages from current chat partner or system messages
                if (message.getSender().equals(currentChatPartner) || 
                message.getSender().equals("system") ||
                message.getContent().startsWith("init-chat:")) {
                
                // Show the scroll pane if it was hidden
                if (!scrollPane.isVisible()) {
                    scrollPane.setVisible(true);
                    frame.revalidate();
                    frame.repaint();
                }
                
                // Get the current text from the message receiver
                String currentText = messageReceiver.getText();
                if (currentText == null || currentText.trim().isEmpty()) {
                    currentText = "";
                }
                
                // Format the message for display
                String displayMessage;
                if (message.getSender().equals("system")) {
                    displayMessage = "[System]: " + message.getContent() + "\n";
                } else {
                    displayMessage = message.getSender() + ": " + message.getContent() + "\n";
                }
                
                // Append the new message to the current text
                String newText = currentText + displayMessage;
                System.out.println("[MESSAGE HANDLING] Received message: " + displayMessage.trim());
                messageReceiver.setText(newText);
                
                // Auto-scroll to bottom
                messageReceiver.setCaretPosition(messageReceiver.getDocument().getLength());
            }
            });
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