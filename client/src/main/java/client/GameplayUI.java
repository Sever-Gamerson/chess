package client;

import chess.*;
import com.google.gson.Gson;
import model.GameData;

import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements WebSocketCommunicator.MessageHandler {

    private final ServerFacade facade;
    private final String authToken;
    private final int gameID;
    private final String playerColor;
    private WebSocketCommunicator ws;


    private volatile GameData currentGame;
    private final Scanner scanner = new Scanner(System.in);
    private final Gson gson = new Gson();
    private boolean inGame = true;


    public GameplayUI(ServerFacade facade, String authToken,
                      int gameID, String playerColor, int port) throws Exception {
        this.facade = facade;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;

        ws = new WebSocketCommunicator(port, this);

        // give the connection a moment to fully establish
        Thread.sleep(300);

        var connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        ws.sendCommand(connect);

        // wait a moment for the LOAD_GAME response to arrive and draw the board
        Thread.sleep(500);
    }

    public void run() {
        System.out.println("Type 'help' for in-game commands.");

        while (inGame) {
            System.out.print("[IN GAME] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            String result = eval(input);

            if (result != null) {
                System.out.println(result);
            }
        }
    }

    private String eval(String input) {

        return switch (input) {
            case "help" -> help();
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "move" -> makeMove();
            case "resign" -> resign();
            case "highlight" -> highlightMoves();
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String help() {
        return """
                In-game commands:
                  help      - show this message
                  redraw    - redraw the chess board
                  move      - make a move
                  highlight - show legal moves for a piece
                  resign    - forfeit the game
                  leave     - leave the game
                """;
    }


    private String redraw() {
        if (currentGame == null) {
            return "Waiting for game data...";
        }
        BoardRenderer.draw(!"BLACK".equals(playerColor), currentGame.game(), null);
        return null;
    }


    private String leave() {
        try {
            var command = new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE, authToken, gameID);
            ws.sendCommand(command);
            ws.close();
            inGame = false;
            return "You left the game.";
        } catch (Exception e) {
            return "Error leaving game: " + e.getMessage();
        }
    }


    private String makeMove() {
        if (playerColor == null) {
            return "Observers cannot make moves.";
        }

        System.out.print("Enter start position (e.g. e2): ");
        String from = scanner.nextLine().trim().toLowerCase();

        System.out.print("Enter end position (e.g. e4): ");
        String to = scanner.nextLine().trim().toLowerCase();



        // parse the positions
        ChessPosition start = parsePosition(from);
        ChessPosition end = parsePosition(to);

        if (start == null || end == null) {
            return "Invalid position. Use format like 'e2'.";
        }

        // check if promotion is needed
        ChessPiece.PieceType promotion = null;
        if (isPromotionMove(start, end)) {
            System.out.print("Promote to (QUEEN/ROOK/BISHOP/KNIGHT): ");
            String promo = scanner.nextLine().trim().toUpperCase();
            promotion = switch (promo) {
                case "QUEEN" -> ChessPiece.PieceType.QUEEN;
                case "ROOK" -> ChessPiece.PieceType.ROOK;
                case "BISHOP" -> ChessPiece.PieceType.BISHOP;
                case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
                default -> null;
            };
            if (promotion == null) {
                return "Invalid promotion piece.";
            }
        }


        try {
            ChessMove move = new ChessMove(start, end, promotion);
            var command = new MakeMoveCommand(authToken, gameID, move);
            ws.sendCommand(command);
            return null;

        } catch (Exception e) {
            return "Error making move: " + e.getMessage();
        }
    }

    private String resign() {
        if (playerColor == null) {

            return "Observers cannot resign.";
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {

            return "Resignation cancelled.";
        }

        try {
            var command = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN, authToken, gameID);
            ws.sendCommand(command);
            return null;
        } catch (Exception e) {

            return "Error resigning: " + e.getMessage();
        }
    }

    private String highlightMoves() {
        if (currentGame == null) {
            return "Waiting for game data...";
        }

        System.out.print("Enter piece position (e.g. e2): ");
        String input = scanner.nextLine().trim().toLowerCase();
        ChessPosition pos = parsePosition(input);



        if (pos == null) {
            return "Invalid position. Use format like 'e2'.";
        }


        ChessPiece piece = currentGame.game().getBoard().getPiece(pos);
        if (piece == null) {

            return "No piece at that position.";
        }



        Collection<ChessMove> moves = currentGame.game().validMoves(pos);
        BoardRenderer.draw(!"BLACK".equals(playerColor), currentGame.game(), moves);
        return null;
    }


    // converts "e2" into a ChessPosition
    private ChessPosition parsePosition(String input) {
        if (input == null || input.length() != 2) {
            return null;
        }
        int col = input.charAt(0) - 'a' + 1;
        int row = input.charAt(1) - '0';

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            return null;
        }
        return new ChessPosition(row, col);
    }



    private boolean isPromotionMove(ChessPosition start, ChessPosition end) {
        if (currentGame == null){
            return false;
        }
        ChessPiece piece = currentGame.game().getBoard().getPiece(start);
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }
        // white promotes on row 8, black promotes on row 1
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE && end.getRow() == 8)
                || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && end.getRow() == 1);
    }

    // --- WebSocket message callbacks ---

    @Override
    public void onLoadGame(LoadGameMessage message) {
        currentGame = message.getGame();
        System.out.println();
        BoardRenderer.draw(!"BLACK".equals(playerColor), currentGame.game(), null);
        System.out.println();
    }


    @Override
    public void onNotification(NotificationMessage message) {

        System.out.println("\n*** " + message.getMessage() + " ***");
        System.out.print("[IN GAME] >>> ");
    }



    @Override
    public void onError(ErrorMessage message) {

        System.out.println("\nError: " + message.getErrorMessage());
        System.out.print("[IN GAME] >>> ");
    }
}