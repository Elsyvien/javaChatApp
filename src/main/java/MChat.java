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
    /**
     * Main method to start the chat client application.
     * It checks for existing user credentials, allows user login or registration,
     * and sets up the WebSocket connection to the chat server.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Check if existing credentials exist
        Properties existingCredentials = CredentialsManager.loadCredentials();
        String username;
        User user;
        boolean isNewUser = false;
        
        if (existingCredentials != null) {
            // User already exists, use existing credentials
            username = existingCredentials.getProperty("username");
            System.out.println("[CLIENT] Found existing user: " + username);
            
            // Ask if user wants to use existing credentials or create new ones
            int choice = JOptionPane.showConfirmDialog(
                null, 
                "Found existing user: " + username + "\nUse existing credentials?", 
                "Existing User Found", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                // Use existing credentials - this is a login, not a new registration
                user = new User(username, true, existingCredentials);
                System.out.println("[CLIENT] Using existing credentials for: " + username);
                isNewUser = false; // Existing user, will authenticate
            } else {
                // User wants to create new account - delete old credentials first
                CredentialsManager.deleteCredentials();
                
                // Create new user
                LoginDialog loginDialog = new LoginDialog(null);
                username = loginDialog.showDialog();
                
                if (username == null) {
                    System.err.println("[CLIENT] No username provided, exiting.");
                    JOptionPane.showMessageDialog(null, "No username provided, exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check if this is a newly registered user (has credentials)
                // If LoginDialog returned successfully, the user is already registered
                Properties newCredentials = CredentialsManager.loadCredentials();
                if (newCredentials != null && newCredentials.getProperty("username").equals(username)) {
                    // User was registered successfully, load their credentials
                    user = new User(username, true, newCredentials);
                    isNewUser = false; // User is registered, will authenticate
                } else {
                    System.err.println("[CLIENT] Registration may have failed, exiting.");
                    JOptionPane.showMessageDialog(null, "Registration failed, please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } else {
            // No existing credentials, show login dialog
            LoginDialog loginDialog = new LoginDialog(null);
            username = loginDialog.showDialog();
            
            if (username == null) {
                System.err.println("[CLIENT] No username provided, exiting.");
                JOptionPane.showMessageDialog(null, "No username provided, exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if user was registered or logged in
            Properties credentials = CredentialsManager.loadCredentials();
            if (credentials != null && credentials.getProperty("username").equals(username)) {
                // User logged in or was just registered
                user = new User(username, true, credentials);
                isNewUser = false; // Will authenticate with server
            } else {
                System.err.println("[CLIENT] No valid credentials found, exiting.");
                JOptionPane.showMessageDialog(null, "Login failed, please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
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
        scrollPane.setVisible(false);
        
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
            if (chatWithUsername == "") {
                JDialog dialog = new JDialog(frame, "Fehler kein Username angegeben", true);
            }
        });

        // Send on button click
        sendButton.addActionListener(e -> {
            String messageString = messageField.getText();
            //sendMessageToServer("Max", message); // Replace "Max" with your actual sender name. Deprecated method
            Message message = new Message(user.getUsername(), messageString);
            chatClient.sendMessage(message); // Send the message using the WebSocket client
            messageField.setText("");
        });


        chatClient.setMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                // Zeige das Message Panel bei der ersten Nachricht
                if (!scrollPane.isVisible()) {
                    scrollPane.setVisible(true);
                    frame.revalidate(); // Layout neu berechnen
                    frame.repaint();    // Fenster neu zeichnen
                }
                
                String currentText = messageReceiver.getText();
                if (currentText == null || currentText.trim().isEmpty()) {
                    currentText = "";
                }
                String newText = currentText + message.getSender() + ": " + message.getContent() + "\n";
                System.out.println("Received message: " + newText);
                messageReceiver.setText(newText);
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