package server;

import chess.ChessGame;
import chess.ChessMove;

import com.google.gson.Gson;
import dataaccess.DataAccess;

import model.AuthData;
import model.GameData;


import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

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
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session,
                        gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
            }

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());

        }
    }


    private void handleConnect(Session session, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }

        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(session, "Error: game not found");
            return;
        }



        sessionManager.addSession(command.getGameID(), session);

        // send current game state to connecting client
        sessionManager.sendToSession(session, gson.toJson(new LoadGameMessage(game)));

        // notify others
        String who = auth.username();
        String role;
        if (who.equals(game.whiteUsername())) {

            role = who + " joined as WHITE";
        } else if (who.equals(game.blackUsername())) {

            role = who + " joined as BLACK";
        } else {

            role = who + " is observing";
        }
        sessionManager.broadcastExcept(command.getGameID(), session,
                gson.toJson(new NotificationMessage(role)));
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {

            sendError(session, "Error: unauthorized");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(session, "Error: game not found");

            return;
        }

        ChessGame game = gameData.game();

        // check game is not over
        if (game.isGameOver()) {
            sendError(session, "Error: game is already over");
            return;
        }

        // check it's the player's turn

        String username = auth.username();
        ChessGame.TeamColor playerColor = null;

        if (username.equals(gameData.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;

        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;

        }

        if (playerColor == null) {
            sendError(session, "Error: observers cannot make moves");
            return;
        }

        if (game.getTeamTurn() != playerColor) {
            sendError(session, "Error: it is not your turn");
            return;
        }


        // attempt the move
        ChessMove move = command.getMove();
        try {
            game.makeMove(move);
        } catch (Exception e) {
            sendError(session, "Error: invalid move");
            return;
        }

        // save updated game
        GameData updated = new GameData(gameData.gameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), game);
        dataAccess.updateGame(updated);

        // broadcast updated board to everyone
        sessionManager.broadcast(command.getGameID(), gson.toJson(new LoadGameMessage(updated)));

        // notify others of the move
        String moveDesc = username + " moved " + move.getStartPosition() + " to " + move.getEndPosition();
        sessionManager.broadcastExcept(command.getGameID(), session,
                gson.toJson(new NotificationMessage(moveDesc)));


        // check for check/checkmate/stalemate
        ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        String opponentName = (opponent == ChessGame.TeamColor.WHITE)
                ? gameData.whiteUsername() : gameData.blackUsername();



        if (game.isInCheckmate(opponent)) {
            game.setGameOver(true);
            dataAccess.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), game));
            sessionManager.broadcast(command.getGameID(),
                    gson.toJson(new NotificationMessage(opponentName + " is in checkmate! Game over.")));

        } else if (game.isInStalemate(opponent)) {
            game.setGameOver(true);
            dataAccess.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), game));

            sessionManager.broadcast(command.getGameID(),
                    gson.toJson(new NotificationMessage("Stalemate! Game over.")));


        } else if (game.isInCheck(opponent)) {
            sessionManager.broadcast(command.getGameID(),
                    gson.toJson(new NotificationMessage(opponentName + " is in check!")));

        }

    }



    private void handleLeave(Session session, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }



        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(session, "Error: game not found");
            return;
        }


        // remove player from game if they were a player
        String username = auth.username();
        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (username.equals(white)) {
            white = null;

        } else if (username.equals(black)) {
            black = null;

        }

        dataAccess.updateGame(new GameData(game.gameID(), white, black,
                game.gameName(), game.game()));

        sessionManager.removeSession(command.getGameID(), session);

        sessionManager.broadcastExcept(command.getGameID(), session,
                gson.toJson(new NotificationMessage(username + " left the game.")));
    }


    private void handleResign(Session session, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {

            sendError(session, "Error: unauthorized");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {

            sendError(session, "Error: game not found");
            return;
        }

        String username = auth.username();

        // only players can resign
        if (!username.equals(gameData.whiteUsername()) &&
                !username.equals(gameData.blackUsername())) {

            sendError(session, "Error: observers cannot resign");
            return;
        }

        if (gameData.game().isGameOver()) {

            sendError(session, "Error: game is already over");
            return;
        }

        // mark game as over
        gameData.game().setGameOver(true);

        dataAccess.updateGame(gameData);


        sessionManager.broadcast(command.getGameID(),
                gson.toJson(new NotificationMessage(username + " resigned. Game over.")));
    }

    private void sendError(Session session, String message) {
        try {

            sessionManager.sendToSession(session, gson.toJson(new ErrorMessage(message)));

        } catch (IOException e) {

            System.out.println("Failed to send error: " + e.getMessage());
        }
    }
}