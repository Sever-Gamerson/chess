package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

public class WebSocketHandler {

    private final DataAccess dataAccess;
    private final GameSessionManager sessionManager;
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess, GameSessionManager sessionManager) {
        this.dataAccess = dataAccess;
        this.sessionManager = sessionManager;
    }

    public void onConnect(WsContext ctx) {
        System.out.println("WebSocket connected");
    }

    public void onClose(WsContext ctx) {
        System.out.println("WebSocket closed");
    }

    public void onError(WsContext ctx) {
        System.out.println("WebSocket error occurred");
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            String message = ctx.message();
            System.out.println("Received message: " + message);
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            System.out.println("Command type: " + command.getCommandType());

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMakeMove(ctx, gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }
        } catch (Exception e) {
            System.out.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) throws Exception {
        System.out.println("Handling connect for gameID: " + command.getGameID());

        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        System.out.println("Auth: " + auth);
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData game = dataAccess.getGame(command.getGameID());
        System.out.println("Game: " + game);
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        sessionManager.addSession(command.getGameID(), ctx);
        System.out.println("Session added");

        ctx.send(gson.toJson(new LoadGameMessage(game)));
        System.out.println("Load game sent");

        String username = auth.username();
        String role;
        if (username.equals(game.whiteUsername())) {
            role = username + " joined as WHITE";
        } else if (username.equals(game.blackUsername())) {
            role = username + " joined as BLACK";
        } else {
            role = username + " is observing";
        }

        System.out.println("Broadcasting: " + role);
        sessionManager.broadcastExcept(command.getGameID(), ctx,
                gson.toJson(new NotificationMessage(role)));
        System.out.println("Connect handled successfully");
    }

    private void handleMakeMove(WsContext ctx, MakeMoveCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(ctx, "Error: game is already over");
            return;
        }

        // make sure it's actually a player and it's their turn
        String username = auth.username();
        ChessGame.TeamColor playerColor = null;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        }

        if (playerColor == null) {
            sendError(ctx, "Error: observers cannot make moves");
            return;
        }

        if (game.getTeamTurn() != playerColor) {
            sendError(ctx, "Error: it is not your turn");
            return;
        }

        try {
            game.makeMove(command.getMove());
        } catch (Exception e) {
            sendError(ctx, "Error: invalid move");
            return;
        }

        // save the updated game
        GameData updated = new GameData(gameData.gameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), game);
        dataAccess.updateGame(updated);

        // broadcast the new board to everyone
        sessionManager.broadcast(command.getGameID(), gson.toJson(new LoadGameMessage(updated)));

        // notify others of the move
        ChessMove move = command.getMove();
        String moveDesc = username + " moved " + move.getStartPosition() + " to " + move.getEndPosition();
        sessionManager.broadcastExcept(command.getGameID(), ctx,
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

    private void handleLeave(WsContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData game = dataAccess.getGame(command.getGameID());
        if (game == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

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

        sessionManager.removeSession(command.getGameID(), ctx);
        sessionManager.broadcastExcept(command.getGameID(), ctx,
                gson.toJson(new NotificationMessage(username + " left the game.")));
    }

    private void handleResign(WsContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game not found");
            return;
        }

        String username = auth.username();

        if (!username.equals(gameData.whiteUsername()) &&
                !username.equals(gameData.blackUsername())) {
            sendError(ctx, "Error: observers cannot resign");
            return;
        }

        if (gameData.game().isGameOver()) {
            sendError(ctx, "Error: game is already over");
            return;
        }

        gameData.game().setGameOver(true);
        dataAccess.updateGame(gameData);

        sessionManager.broadcast(command.getGameID(),
                gson.toJson(new NotificationMessage(username + " resigned. Game over.")));
    }

    private void sendError(WsContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }
}