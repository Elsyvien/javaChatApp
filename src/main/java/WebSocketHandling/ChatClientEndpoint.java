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


@ClientEndpoint // Marks this class as a WebSocket client endpoint
public class ChatClientEndpoint {
    
    private static final Jsonb jsonb = JsonbBuilder.create();

    private Session userSession; // Represents the WebSocket session
    private Message lastMessage;
    private Message[] messageBuffer = new Message[10]; // Buffer for storing the last 10 messages
    private int bufferIndex = 0; // Index for the message buffer

    private MessageListener listener;

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public ChatClientEndpoint() {
        // Default constructor for the WebSocket client endpoint
        // No URI is needed here as the connection will be established later
    }

    @OnOpen // Method to handle opening a WebSocket connection
    public void onOpen(Session userSession) {
        this.userSession = userSession; // Store the session for later use
        System.out.println("Connected to server: " + userSession.getBasicRemote());
    }

    @OnMessage // Method to handle incoming messages
    public void onMessage(String messageJson) {
        try {
            System.out.println("[CLIENT] RAW JSON: " + messageJson);
            Message message = jsonb.fromJson(messageJson, Message.class);
            System.out.println("[CLIENT] Parsed: sender=" + message.getSender() + ", content=" + message.getContent());

            // Speichern für getLastMessage()
            this.lastMessage = message;

            //In den Ringpuffer schreiben
            this.messageBuffer[this.bufferIndex] = message;
            this.bufferIndex = (this.bufferIndex + 1) % this.messageBuffer.length;

            // Listener benachrichtigen
            if (listener != null) {
                listener.onNewMessage(message);
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
                System.out.println("Sent message: " + message.getContent() + " from " + message.getSender()); // Log the sent message
        }   else { // Check if the session is open before sending
                System.err.println("WebSocket session is not open. Cannot send message.");
                throw new IllegalStateException("WebSocket session is not open");
            }
        } catch (Exception e) { // Handle any exceptions that occur during message sending
            e.printStackTrace();
        }
    }

    @OnClose // Method to handle closing a WebSocket connection
    public void onClose(Session userSession) {
        this.userSession = null; // Clear the session
        System.out.println("Connection closed: " + userSession.getId());
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
