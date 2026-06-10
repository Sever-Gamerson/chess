package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import java.util.HashSet;
import java.util.Set;


public class GameSessionManager {

    // maps the gameID to all sessions connected to that game
    private final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();


    public void addSession(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> new HashSet<>()).add(session);
    }


    public void removeSession(int gameID, Session session) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    // sends a message to everyone in the game
    public void broadcast(int gameID, String message) throws IOException {
        Set<Session> sessions = gameSessions.get(gameID);

        if (sessions == null) return;
        for (Session session : sessions) {
            if (session.isOpen()) {

                session.getRemote().sendString(message);
            }
        }
    }



    // message to everyone except one session
    public void broadcastExcept(int gameID, Session exclude, String message) throws IOException {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions == null) return;

        for (Session session : sessions) {

            if (session.isOpen() && !session.equals(exclude)) {
                session.getRemote().sendString(message);

            }


        }
    }

    // one session
    public void sendToSession(Session session, String message) throws IOException {
        if (session.isOpen()) {

            session.getRemote().sendString(message);
        }
    }
}