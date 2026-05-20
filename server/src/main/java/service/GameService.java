package service;


import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.List;

public class GameService {

    private DataAccess dataAccess;

    public GameService(DataAccess dataAccess){
        this.dataAccess=dataAccess;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException{
       checkIfAuth(authToken);

        return dataAccess.listGames();
    }

    private AuthData checkIfAuth(String authToken)throws DataAccessException{
        AuthData auth=dataAccess.getAuth(authToken);
        if (auth == null) {

            throw new DataAccessException("unauthorized");
        }
        return auth;
    }

}
