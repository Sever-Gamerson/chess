package client;


import model.AuthData;
import model.GameData;

import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {

        server = new Server();
        var port = server.run(0);

        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }


    //register


    @Test
    void registerSuccess() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@email.com");
        assertNotNull(auth);

        assertTrue(auth.authToken().length() > 10);

        assertEquals("player1", auth.username());
    }

    @Test
    void registerDuplicateFails() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        assertThrows(Exception.class, () ->
                facade.register("player1", "password", "p1@email.com"));
    }

    //Login

    @Test
    void loginSuccess() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        AuthData auth = facade.login("player1", "password");


        assertNotNull(auth);
        assertEquals("player1", auth.username());
    }


    @Test
    void loginWrongPasswordFails() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        assertThrows(Exception.class, () ->
                facade.login("player1", "wrongpassword"));
    }

    //Logout

    @Test
    void logoutSuccess() throws Exception {

        AuthData auth = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutInvalidTokenFails() {
        assertThrows(Exception.class, () ->
                facade.logout("invalid-token"));
    }

    //Create Game



    @Test
    void createGameSuccess() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@email.com");
        int gameID = facade.createGame(auth.authToken(), "mygame");

        assertTrue(gameID > 0);
    }

    @Test
    void createGameUnauthorizedFails() {

        assertThrows(Exception.class, () ->
                facade.createGame("bad-token", "mygame"));
    }

    //List Games

    @Test
    void listGamesSuccess() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@email.com");
        facade.createGame(auth.authToken(), "game1");

            facade.createGame(auth.authToken(), "game2");

        List<GameData> games = facade.listGames(auth.authToken());
        assertEquals(2, games.size());
    }


    @Test
    void listGamesUnauthorizedFails() {
        assertThrows(Exception.class, () ->
                facade.listGames("bad-token"));


    }

    //join Game

    @Test
    void joinGameSuccess() throws Exception {

        AuthData auth = facade.register("player1", "password", "p1@email.com");
        int gameID = facade.createGame(auth.authToken(), "mygame");
        assertDoesNotThrow(() ->

                facade.joinGame(auth.authToken(), "WHITE", gameID));
    }



    @Test
    void joinGameAlreadyTakenFails() throws Exception {

        AuthData auth1 = facade.register("player1", "password", "p1@email.com");
        AuthData auth2 = facade.register("player2", "password", "p2@email.com");

        int gameID = facade.createGame(auth1.authToken(), "mygame");

        facade.joinGame(auth1.authToken(), "WHITE", gameID);

        assertThrows(Exception.class, () ->
                facade.joinGame(auth2.authToken(), "WHITE", gameID));
    }
}