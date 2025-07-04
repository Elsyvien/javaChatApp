import WebSocketHandling.ChatClientEndpoint;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;

import javax.swing.*;
/*
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
*/
import model.Message;
import model.User;
import utils.LoginDialog;

public class MChat {

    public static void main(String[] args) {
        String[] loginInfo = LoginDialog.showLoginDialog();
        if (loginInfo == null || loginInfo[0] == null || loginInfo[0].trim().isEmpty()) {
            System.out.println("Login cancelled. Exiting.");
            return; // Exit if login is cancelled or username is empty
        }
        String username = loginInfo[0];
        User user = new User(username);
        String currentURI = "ws://localhost:8080/Gradle___com_maxstaneker_chatapp___chatApp_backend_1_0_SNAPSHOT_war/chat";

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ChatClientEndpoint chatClient = new ChatClientEndpoint(); // Parameterlos

        try {
            container.connectToServer(chatClient, URI.create(currentURI)); // Connect to the WebSocket server
        } catch (jakarta.websocket.DeploymentException | java.io.IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        } // Handle connection errors

        JFrame frame = new JFrame("Chat Client");
        JTextField messageField = new JTextField(30);
        JButton sendButton = new JButton("Send");
        JTextPane messageReceiver = new JTextPane();

        JPanel panel = new JPanel();
        panel.add(messageField);
        panel.add(sendButton);
        panel.add(messageReceiver);
        messageReceiver.setEditable(false); // Make the message receiver non-editable

        frame.setContentPane(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Send on button click
        sendButton.addActionListener(e -> {
            String messageString = messageField.getText();
            //sendMessageToServer("Max", message); // Replace "Max" with your actual sender name. Deprecated method
            Message message = new Message(user.getName(), messageString);
            chatClient.sendMessage(message); // Send the message using the WebSocket client
            messageField.setText("");
        });


        chatClient.setMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
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