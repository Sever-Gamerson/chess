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

    public int createGame(String gameName,String  authToken)throws DataAccessException{
        checkIfAuth(authToken);
        if(gameName==null){throw new DataAccessException("Bad Request");}

        return dataAccess.createGame(gameName);
    }
    //just check if the player is logged in
    private AuthData checkIfAuth(String authToken)throws DataAccessException{
        AuthData auth=dataAccess.getAuth(authToken);
        if (auth == null) {

            throw new DataAccessException("unauthorized");
        }
        return auth;
    }

    public void joinGame(String authToken, int gameID,String playerColor)throws DataAccessException{
        AuthData auth = checkIfAuth(authToken);
        GameData game = dataAccess.getGame(gameID);

        if(game==null){
            throw new DataAccessException("Bad Request");
        }

        String whiteName=game.whiteUsername();
        String blackName=game.blackUsername();

        if(playerColor == null || playerColor.isEmpty()){
            throw new DataAccessException("Bad Request");
        }
        if(playerColor.equalsIgnoreCase("WHITE")){
            if(whiteName!=null){
                throw new DataAccessException("Team Taken");
            }
            whiteName=auth.username();
        }else if(playerColor.equalsIgnoreCase("BLACK")){
            if(blackName!=null){
                throw new DataAccessException("Team Taken");
            }
            blackName=auth.username();
        }else{
            throw new DataAccessException("Bad Request");
        }


        GameData updatedGame= new GameData(game.gameID(),whiteName,blackName, game.gameName(), game.game());
        dataAccess.updateGame(updatedGame);//make sure we update that the player has joined

    }

}
