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
}
