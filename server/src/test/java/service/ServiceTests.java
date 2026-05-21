package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    private static MemoryDataAccess dataAccess;
    private static UserService userService;
    private static GameService gameService;
    private static ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);
    }


   //ClearService


    @Test
    public void clearPositive() throws DataAccessException {
        userService.register(new UserData("bob", "pass", "bob@bob.com"), "bob");
        clearService.clear();
        assertNull(dataAccess.getUser("bob"));
    }


 //register


    @Test
    public void registerPositive() throws DataAccessException {
        AuthData auth = userService.register(new UserData("alice", "pass", "alice@a.com"), "alice");
        assertNotNull(auth.authToken());
        assertEquals("alice", auth.username());
    }

    @Test
    public void registerNegativeDuplicate() throws DataAccessException {
        userService.register(new UserData("alice", "pass", "alice@a.com"), "alice");
        assertThrows(DataAccessException.class, () ->
                userService.register(new UserData("alice", "pass2", "alice2@a.com"), "alice")
        );
    }

    @Test
    public void registerNegativeMissingFields() {
        assertThrows(DataAccessException.class, () ->
                userService.register(new UserData(null, "pass", "email@a.com"), "null")
        );
    }

    @Test
    public void loginPositive() throws DataAccessException {
        userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        AuthData auth = userService.login("bob", "pass");
        assertNotNull(auth.authToken());
        assertEquals("bob", auth.username());
    }

    @Test
    public void loginNegativeWrongPassword() throws DataAccessException {
        userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        assertThrows(DataAccessException.class, () ->
                userService.login("bob", "wrongpass")
        );
    }


    // UserService - logout


    @Test
    public void logoutPositive() throws DataAccessException {
        AuthData auth = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        userService.logout(auth.authToken());
        assertNull(dataAccess.getAuth(auth.authToken()));
    }

    @Test
    public void logoutNegativeBadToken() {
        assertThrows(DataAccessException.class, () ->
                userService.logout("fake-token-123")
        );
    }


    // GameService - listGames


    @Test
    public void listGamesPositive() throws DataAccessException {
        AuthData auth = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        gameService.createGame("testGame", auth.authToken());
        List<GameData> games = gameService.listGames(auth.authToken());
        assertFalse(games.isEmpty());
    }

    @Test
    public void listGamesNegativeBadToken() {
        assertThrows(DataAccessException.class, () ->
                gameService.listGames("bad-token")
        );
    }
    // GameService - createGame


    @Test
    public void createGamePositive() throws DataAccessException {
        AuthData auth = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        int gameID = gameService.createGame("mygame", auth.authToken());
        assertTrue(gameID > 0);
    }

    @Test
    public void createGameNegativeNullName() throws DataAccessException {
        AuthData auth = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        assertThrows(DataAccessException.class, () ->
                gameService.createGame(null, auth.authToken())
        );
    }


    // GameService - joinGame


    @Test
    public void joinGamePositive() throws DataAccessException {
        AuthData auth = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        int gameID = gameService.createGame("mygame", auth.authToken());
        gameService.joinGame(auth.authToken(), gameID, "WHITE");
        GameData game = dataAccess.getGame(gameID);
        assertEquals("bob", game.whiteUsername());
    }

    @Test
    public void joinGameNegativeColorTaken() throws DataAccessException {
        AuthData auth1 = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        AuthData auth2 = userService.register(new UserData("alice", "pass", "alice@a.com"), "alice");
        int gameID = gameService.createGame("mygame", auth1.authToken());
        gameService.joinGame(auth1.authToken(), gameID, "WHITE");
        assertThrows(DataAccessException.class, () ->
                gameService.joinGame(auth2.authToken(), gameID, "WHITE")
        );
    }

    @Test
    public void joinGameNegativeBadGameID() throws DataAccessException {
        AuthData auth = userService.register(new UserData("bob", "pass", "bob@b.com"), "bob");
        assertThrows(DataAccessException.class, () ->
                gameService.joinGame(auth.authToken(), 9999, "WHITE")
        );
    }
}