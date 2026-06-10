package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

@WebSocket
public class WebSocketHandler {
    private final DataAccess dataAccess;
    private final GameSessionManager sessionManager;
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess, GameSessionManager sessionManager) {
        this.dataAccess = dataAccess;
        this.sessionManager = sessionManager;
    }
    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }


    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
    }


    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        //use a helper function
    }
}
