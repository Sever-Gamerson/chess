package dataaccess;

import org.junit.jupiter.api.*;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySqlDataAccessTests {

    private static MySqlDataAccess dataAccess;

    @BeforeAll
    static void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        dataAccess.clear();
    }



    //clear!
    @Test
    @Order(1)
    @DisplayName("Clear - removes all data")
    void clearPositive() throws DataAccessException {
        // add some data then clear it
        dataAccess.createUser(new UserData("user1", "pass1", "e@e.com"));
        dataAccess.createGame("game1");
        dataAccess.createAuth(new AuthData("token1", "user1"));

        dataAccess.clear();

        // everything should be gone
        assertNull(dataAccess.getUser("user1"));

        assertNull(dataAccess.getAuth("token1"));

        assertTrue(dataAccess.listGames().isEmpty());
    }


    //  USER

    @Test
    @Order(2)
    @DisplayName("Create User - success")
    void createUserPositive() throws DataAccessException {
        // create a user and verify they exist
        dataAccess.createUser(new UserData("nate", "pass123", "nate@email.com"));
        UserData user = dataAccess.getUser("nate");

        assertNotNull(user);
        assertEquals("nate", user.username());

        assertEquals("nate@email.com", user.email());
    }

    @Test
    @Order(3)
    @DisplayName("Create User - duplicate username fails")
    void createUserNegative() throws DataAccessException {

        // creating two users with the same username should fail

        dataAccess.createUser(new UserData("nate", "pass123", "nate@email.com"));

        assertThrows(DataAccessException.class, () ->
                dataAccess.createUser(new UserData("nate", "different", "other@email.com"))
        );
    }

    @Test
    @Order(4)
    @DisplayName("Get User - success")
    void getUserPositive() throws DataAccessException {
        // user we put in should come back out
        dataAccess.createUser(new UserData("aiva", "pass456", "aiva@email.com"));
        UserData user = dataAccess.getUser("aiva");

        assertNotNull(user);

        assertEquals("aiva", user.username());

        assertEquals("aiva@email.com", user.email());
    }

    @Test
    @Order(5)
    @DisplayName("Get User - nonexistent user returns null")
    void getUserNegative() throws DataAccessException {
        // looking up a user that doesnt exist should return null

        UserData user = dataAccess.getUser("nobody");
        assertNull(user);
    }


    // auth

    @Test
    @Order(6)
    @DisplayName("Create Auth - success")
    void createAuthPositive() throws DataAccessException {
        // create a user first, then create an auth token for them
        dataAccess.createUser(new UserData("nate", "pass123", "nate@email.com"));
        dataAccess.createAuth(new AuthData("token123", "nate"));

        AuthData auth = dataAccess.getAuth("token123");
        assertNotNull(auth);
        assertEquals("token123", auth.authToken());
        assertEquals("nate", auth.username());
    }

    @Test
    @Order(7)
    @DisplayName("Create Auth - duplicate token fails")
    void createAuthNegative() throws DataAccessException {
        // two auth tokens with the same value should fail
        dataAccess.createUser(new UserData("nate", "pass123", "nate@email.com"));
        dataAccess.createAuth(new AuthData("token123", "nate"));

        assertThrows(DataAccessException.class, () ->
                dataAccess.createAuth(new AuthData("token123", "nate"))
        );
    }

    @Test
    @Order(8)
    @DisplayName("Get Auth - success")
    void getAuthPositive() throws DataAccessException {
        // token we put in should come back out with correct username
        dataAccess.createUser(new UserData("nate", "pass123", "nate@email.com"));
        dataAccess.createAuth(new AuthData("mytoken", "nate"));

        AuthData auth = dataAccess.getAuth("mytoken");
        assertNotNull(auth);
        assertEquals("nate", auth.username());
    }

    @Test
    @Order(9)
    @DisplayName("Get Auth - nonexistent token returns null")
    void getAuthNegative() throws DataAccessException {
        // looking up a token that doesnt exist should return null
        AuthData auth = dataAccess.getAuth("faketoken");
        assertNull(auth);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Auth - success")
    void deleteAuthPositive() throws DataAccessException {
        // token should be gone after deleting it
        dataAccess.createUser(new UserData("nate", "pass123", "nate@email.com"));
        dataAccess.createAuth(new AuthData("token123", "nate"));

        dataAccess.deleteAuth("token123");

        assertNull(dataAccess.getAuth("token123"));
    }

    @Test
    @Order(11)
    @DisplayName("Delete Auth - deleting nonexistent token does not throw")
    void deleteAuthNegative() throws DataAccessException {
        // deleting a token that doesnt exist should not crash
        assertDoesNotThrow(() -> dataAccess.deleteAuth("doesnotexist"));
    }
    // GAME
    @Test
    @Order(12)
    @DisplayName("Create Game - success")
    void createGamePositive() throws DataAccessException {
        // create a game and verify it got a valid ID back
        int gameID = dataAccess.createGame("testgame");
        assertTrue(gameID > 0);

        GameData game = dataAccess.getGame(gameID);
        assertNotNull(game);
        assertEquals("testgame", game.gameName());
    }

    @Test
    @Order(13)
    @DisplayName("Create Game - null name fails")
    void createGameNegative() throws DataAccessException {
        // game name cannot be null
        assertThrows(DataAccessException.class, () ->
                dataAccess.createGame(null)
        );
    }

    @Test
    @Order(14)
    @DisplayName("Get Game - success")
    void getGamePositive() throws DataAccessException {
        // game we created should come back with correct data
        int gameID = dataAccess.createGame("mygame");
        GameData game = dataAccess.getGame(gameID);

        assertNotNull(game);
        assertEquals(gameID, game.gameID());
        assertEquals("mygame", game.gameName());
        assertNull(game.whiteUsername());  // no players yet
        assertNull(game.blackUsername());
        assertNotNull(game.game());        // chess game object should exist
    }

    @Test
    @Order(15)
    @DisplayName("Get Game - nonexistent game returns null")
    void getGameNegative() throws DataAccessException {
        // looking up a game that doesnt exist should return null
        GameData game = dataAccess.getGame(99999);
        assertNull(game);
    }

    @Test
    @Order(16)
    @DisplayName("List Games - success")
    void listGamesPositive() throws DataAccessException {
        // all created games should show up in the list
        dataAccess.createGame("game1");
        dataAccess.createGame("game2");
        dataAccess.createGame("game3");

        var games = dataAccess.listGames();
        assertEquals(3, games.size());
    }

    @Test
    @Order(17)
    @DisplayName("List Games - empty list when no games")
    void listGamesNegative() throws DataAccessException {
        // empty database should return empty list not null
        var games = dataAccess.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    @Order(18)
    @DisplayName("Update Game - success")
    void updateGamePositive() throws DataAccessException {
        // create a game then add a white player to it
        int gameID = dataAccess.createGame("testgame");
        GameData original = dataAccess.getGame(gameID);

        // add white player
        GameData updated = new GameData(gameID, "whiteplayer", null, "testgame", original.game());
        dataAccess.updateGame(updated);

        GameData result = dataAccess.getGame(gameID);
        assertEquals("whiteplayer", result.whiteUsername());
    }

    @Test
    @Order(19)
    @DisplayName("Update Game - chess game state persists")
    void updateGameNegative() throws DataAccessException {
        // make sure the actual chess game state saves correctly
        int gameID = dataAccess.createGame("testgame");
        GameData original = dataAccess.getGame(gameID);

        // update with a new game state
        ChessGame newGameState = new ChessGame();
        GameData updated = new GameData(gameID, "white", "black", "testgame", newGameState);
        dataAccess.updateGame(updated);

        GameData result = dataAccess.getGame(gameID);
        // both players should be saved
        assertEquals("white", result.whiteUsername());
        assertEquals("black", result.blackUsername());
        // game object should not be null after update
        assertNotNull(result.game());
    }
}


