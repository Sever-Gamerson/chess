package client;

import com.google.gson.Gson;
import org.glassfish.tyrus.client.ClientManager;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;
import java.net.URI;


@ClientEndpoint
public class WebSocketCommunicator {

    private Session session;
    private final Gson gson = new Gson();
    private final MessageHandler messageHandler;


    // whoever creates this communicator must implement MessageHandler
    // so they can receive incoming messages
    public interface MessageHandler {
        void onLoadGame(LoadGameMessage message);
        void onNotification(NotificationMessage message);
        void onError(ErrorMessage message);
    }

    public WebSocketCommunicator(int port, MessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;

        URI uri = new URI("ws://localhost:" + port + "/ws");
        ClientManager client = ClientManager.createClient();
        client.connectToServer(this, uri);

        // wait until the session is actually open before returning
        int attempts = 0;
        while (session == null && attempts < 20) {
            Thread.sleep(100);
            attempts++;
        }

        if (session == null) {
            throw new Exception("Failed to connect to server");
        }
        // prevent idle timeout from closing the connection
        session.setMaxIdleTimeout(0);
    }



    // called automatically when the connection opens
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String json) {
        try {
            ServerMessage base = gson.fromJson(json, ServerMessage.class);

            switch (base.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                    messageHandler.onLoadGame(msg);
                }
                case NOTIFICATION ->
                        messageHandler.onNotification(gson.fromJson(json, NotificationMessage.class));
                case ERROR ->
                        messageHandler.onError(gson.fromJson(json, ErrorMessage.class));
            }
        } catch (Exception e) {
            System.out.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Connection closed: " + reason.getReasonPhrase());
    }


    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
    }



    // send any command to the server
    public void sendCommand(UserGameCommand command) throws Exception {
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }


    public void close() throws Exception {

        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}