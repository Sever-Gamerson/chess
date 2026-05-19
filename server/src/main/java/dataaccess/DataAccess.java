package dataaccess;

//get our made models
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.List;

public interface DataAccess {

    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    //create the game, then get the game, have the list for those searching, then update a game on going.

    int  createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;

    //allows us to alter the auth tokens
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;
}
