package client;

import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostloginUI {
    private final ServerFacade facade;
    private final AuthData auth;

    private final Scanner scanner = new Scanner(System.in);
    private List<GameData> lastGameList = new ArrayList<>();

    public PostloginUI(ServerFacade facade, AuthData auth) {
        this.facade = facade;

        this.auth = auth;
    }

    public void run() {
        System.out.println("Logged in as " + auth.username() + ". Type 'help' for options.");

        while (true) {
            System.out.print("[LOGGED_IN] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            String result = eval(input);

            System.out.println(result);


            if (result.equals("__logout__")) {
                break;
            }
        }
    }

    private String eval(String input) {
        return switch (input) {
            case "help" -> help();
            case "logout" -> logout();
            case "create game" -> createGame();
            case "list games" -> listGames();
            case "play game" -> playGame();
            case "observe game" -> observeGame();
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String help() {
        return """
                Available commands:
                  help  - shows this message
                  list games  - show all games
                  create game - create a new game
                  play game - join a game as a player
                  observe game - watch a game
                  logout  - log out
                """;
    }

    private String logout() {
        try {
            facade.logout(auth.authToken());

            return "__logout__";
        } catch (Exception e) {

            return "Logout failed: " + e.getMessage();
        }
    }

    private String createGame() {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            return "Game name cannot be empty.";
        }

        try {

            facade.createGame(auth.authToken(), gameName);
            return "Game '" + gameName + "' created!";

        } catch (Exception e) {

            return "Failed to create game: " + e.getMessage();
        }
    }

    private String listGames() {
        try {
            lastGameList = facade.listGames(auth.authToken());

            if (lastGameList.isEmpty()) {
                return "No games available. Create one!";
            }

            StringBuilder sb = new StringBuilder("Current games:\n");
            for (int i = 0; i < lastGameList.size(); i++) {
                GameData game = lastGameList.get(i);

                String white = game.whiteUsername() != null ? game.whiteUsername() : "open";
                String black = game.blackUsername() != null ? game.blackUsername() : "open";


                sb.append(String.format("  %d. %s | White: %s | Black: %s%n",
                        i + 1, game.gameName(), white, black));
            }

            return sb.toString();

        } catch (Exception e) {

            return "Failed to list games: " + e.getMessage();
        }
    }

    private String playGame() {
        if (lastGameList.isEmpty()) {
            return "Please run 'list games' first.";
        }

        System.out.print("Game number: ");
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(scanner.nextLine().trim());

        } catch (NumberFormatException e) {
            return "Invalid number. Please enter a valid game number.";
        }

        if (gameNumber < 1 || gameNumber > lastGameList.size()) {
            return "Invalid game number. Please list games first to see valid numbers.";
        }

        System.out.print("Color (WHITE/BLACK): ");
        String color = scanner.nextLine().trim().toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Invalid color. Please enter WHITE or BLACK.";
        }

        try {
            GameData game = lastGameList.get(gameNumber - 1);
            facade.joinGame(auth.authToken(), color, game.gameID());

            //render board

            return "Joined game as " + color + ".";
        } catch (Exception e) {
            return "Failed to join game: " + e.getMessage();
        }
    }

    private String observeGame() {
        //player isnt playing

        if (lastGameList.isEmpty()) {
            return "Please run 'list games' first.";
        }

        System.out.print("Game number: ");
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(scanner.nextLine().trim());

        } catch (NumberFormatException e) {

            return "Invalid number. Please enter a valid game number.";
        }

        if (gameNumber < 1 || gameNumber > lastGameList.size()) {
            return "Invalid game number. Please list games first to see valid numbers.";
        }

        // observers see white's perspective and render board
        return "Observing game " + gameNumber + ".";
    }
}