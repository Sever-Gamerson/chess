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
}
