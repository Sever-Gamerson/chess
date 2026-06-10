package server;

import io.javalin.websocket.WsContext;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;

public class GameSessionManager {

    // maps gameID to all sessions connected to that game
    private final ConcurrentHashMap<Integer, Set<WsContext>> gameSessions = new ConcurrentHashMap<>();

    public void addSession(int gameID, WsContext ctx) {
        gameSessions.computeIfAbsent(gameID, k -> new HashSet<>()).add(ctx);
    }

    public void removeSession(int gameID, WsContext ctx) {
        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(ctx);
        }
    }

    // send to everyone in the game
    public void broadcast(int gameID, String message) {
        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions == null) return;
        for (WsContext ctx : sessions) {
            if (ctx.session.isOpen()) {
                ctx.send(message);
            }
        }
    }

    // send to everyone except one session
    public void broadcastExcept(int gameID, WsContext exclude, String message) {
        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions == null) return;
        for (WsContext ctx : sessions) {
            if (ctx.session.isOpen() && !ctx.equals(exclude)) {
                ctx.send(message);
            }
        }
    }
}