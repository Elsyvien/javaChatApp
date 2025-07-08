package WebSocketHandling;

import model.Message; // Importing the Message model class for handling chat messages

import jakarta.websocket.ContainerProvider; // Provides access to the WebSocket container
import jakarta.websocket.ClientEndpoint; // Annotation to mark a class as a WebSocket client endpoint
import jakarta.websocket.OnMessage; // Annotation to handle incoming messages
import jakarta.websocket.Session; // Represents a WebSocket session
import jakarta.websocket.OnOpen; // Annotation to handle opening a WebSocket connection
import jakarta.websocket.OnClose; // Annotation to handle closing a WebSocket connection
import jakarta.websocket.OnError; // Annotation to handle errors in WebSocket communication
import jakarta.websocket.WebSocketContainer; // Managed Entry Point
import jakarta.json.bind.Jsonb; // JSON Binding API for converting Java objects to JSON and vice versa
import jakarta.json.bind.JsonbBuilder;

import java.net.URI; // For URI handling

/*
 * This class represents a WebSocket client endpoint for handling chat messages.
 * It also handles sending and receiving messages, maintaining a buffer of the last 10 messages,
 * and notifying listeners about new messages.
 * The class uses JSON Binding (Jsonb) for serializing and deserializing Message objects.
 * It implements the MessageListener interface to notify about new messages.
 * The WebSocket annotations (@OnOpen, @OnMessage, @OnClose, @OnError) are used to define methods
 * that handle the respective WebSocket events.
 * The class is designed to be used in a chat application where users can send and receive messages
 * in real-time over a WebSocket connection.
 * @author Max Staneker, Mia Schienagel
 */
import Authentication.Authentication; // Importing the Authentication class for handling user authentication
import model.User; // Importing the User model class for user-related operations
import model.Message;

@ClientEndpoint // Marks this class as a WebSocket client endpoint
public class ChatClientEndpoint {
    
    private static final Jsonb jsonb = JsonbBuilder.create();

    private Session userSession; // Represents the WebSocket session
    private Message lastMessage;
    private final Message[] messageBuffer = new Message[10]; // Buffer for storing the last 10 messages
    private int bufferIndex = 0; // Index for the message buffer

    private MessageListener listener;
    private final Authentication authentication; // Added Authentication field

    // Constructor with Authentication parameter
    public ChatClientEndpoint(Authentication authentication) {
        this.authentication = authentication;
    }
    
    // Constructor for backward compatibility
    public ChatClientEndpoint(Authentication authentication, boolean isNewUser) {
        this.authentication = authentication;
        // isNewUser is ignored since registration happens in LoginDialog
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    @OnOpen // Method to handle opening a WebSocket connection
    public void onOpen(Session userSession) {
        this.userSession = userSession; // Store the session for later use
        System.out.println("[CLIENT] Connected to server: " + userSession.getBasicRemote());
        try {
            // All users (new and existing) need to authenticate
            // Registration happens in LoginDialog, so here we only authenticate
            userSession.getBasicRemote().sendText("auth-request");
            System.out.println("[CLIENT] Sent auth-request to server. " + userSession.getId());
        } catch (Exception e) {
            System.err.println("[CLIENT] Error sending request: " + e.getMessage());
            throw new RuntimeException("Failed to send request", e);
        }
    }

    @OnMessage // Method to handle incoming messages
    public void onMessage(String messageJson) {
        try {
            System.out.println("[CLIENT] Server says: " + messageJson);
            if (messageJson.startsWith("challenge:")) { // Check if the message is a challenge from the server
                String challenge = messageJson.substring("challenge:".length()); // Extract the challenge from the message
                authentication.setChallenge(challenge); // Set the challenge in the Authentication object
                String response = authentication.buildAuthResponse();
                userSession.getBasicRemote().sendText(response);
            } else if (messageJson.equals("auth-success")) { // Check if the authentication was successful
                System.out.println("[CLIENT] Authenticated successfully!" + "\n" + "[CLIENT] Session ID: " + userSession.getId() + "\n" + "[CLIENT] You can now send messages.");
            } else if (messageJson.equals("auth-failure")) { // Check if the authentication failed
                System.out.println("[CLIENT] Auth failed!" + "\n" + "[CLIENT] Please check your credentials and try again.");
                userSession.close(); // Close the session if authentication fails
            } else if (messageJson.startsWith("chat-init-success:")) {
                System.out.println("[CLIENT] Chat initialization successful");
                String initData = messageJson.substring("chat-init-success:".length());
                System.out.println("[CLIENT] Chat partner confirmed: " + initData);
                
                // Notify listener about successful chat initialization
                if (listener != null) {
                    // Create a system message to notify the UI
                    Message systemMessage = new Message("system", messageJson);
                    listener.onNewMessage(systemMessage);
                }
            } else if (messageJson.startsWith("chat-init-failure:")) {
                System.err.println("[CLIENT] Chat initialization failed: " + messageJson);
                
                // Notify listener about failed chat initialization  
                if (listener != null) {
                    // Create a system message to notify the UI
                    Message systemMessage = new Message("system", messageJson);
                    listener.onNewMessage(systemMessage);
                }
                return;
            } else if (messageJson.startsWith("public-key:")) {
                System.out.println("[CLIENT] Received public key response: " + messageJson);
                
                // Forward public key response to listener (non-JSON message)
                if (listener != null) {
                    // Create a special message to carry the public key data
                    Message publicKeyMessage = new Message("public-key", messageJson);
                    listener.onNewMessage(publicKeyMessage);
                }
                return;
            } else { // Handle regular chat messages
                System.out.println("[CLIENT] RAW JSON: " + messageJson);
                Message message = jsonb.fromJson(messageJson, Message.class);
                System.out.println("[CLIENT] Parsed: sender=" + message.getSender() + ", content=" + message.getContent());

                // Save for later retrieval
                this.lastMessage = message;

                // Write to message buffer 
                this.messageBuffer[this.bufferIndex] = message;
                this.bufferIndex = (this.bufferIndex + 1) % this.messageBuffer.length;

                // Alert Listener about new message
                if (listener != null) {
                    listener.onNewMessage(message);
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            if (userSession != null && userSession.isOpen()) {
                String messagejson = jsonb.toJson(message); // Convert the Message object to JSON
                userSession.getBasicRemote().sendText(messagejson); // Send the JSON message over the WebSocket
                System.out.println("[CLIENT] Sent message: " + message.getContent() + " from " + message.getSender()); // Log the sent message
        }   else { // Check if the session is open before sending
                System.err.println("[CLIENT] WebSocket session is not open. Cannot send message.");
                throw new IllegalStateException("WebSocket session is not open");
            }
        } catch (Exception e) { // Handle any exceptions that occur during message sending
            e.printStackTrace();
        }
    }

    @OnClose // Method to handle closing a WebSocket connection
    public void onClose(Session userSession) {
        this.userSession = null; // Clear the session
        System.out.println("[CLIENT] Connection closed: " + userSession.getId() + "\n" + "[CLIENT] Session has been successfully Terminated.");
    }
    
    @OnError // Method to handle errors in WebSocket communication
    public void onError(Session userSession, Throwable throwable) {
        System.err.println("Error in WebSocket connection: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    public Message getLastMessage() {
        return lastMessage;
    }
    
    public Message[] getMessageBuffer() {
        return messageBuffer;
    }

    public int getBufferIndex() {
        return bufferIndex;
    }
}
