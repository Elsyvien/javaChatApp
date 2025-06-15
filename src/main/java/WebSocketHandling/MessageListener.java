package WebSocketHandling;

import model.Message; // Importing the Message model class for handling chat messages

public interface MessageListener {
    void onNewMessage(Message message);
}

