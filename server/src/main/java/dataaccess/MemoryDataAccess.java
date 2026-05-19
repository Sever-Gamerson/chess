package dataaccess;

import chess.ChessGame;


import model.AuthData;
import model.UserData;
import model.GameData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MemoryDataAccess {


    private final Map<String,UserData> users=new HashMap<>();
    private final Map<String,AuthData> auths=new HashMap<>();
    private final Map<Integer, GameData> games=new HashMap<>();

    private int newGameID=1;

    public void clear(){
        users.clear();
        auths.clear();
        games.clear();
    }


    public void createUser(UserData user) throws DataAccessException{
        if(users.containsKey(user.username())){//checks to see if there's already this username
            throw new DataAccessException("[403] User Already Taken");
        }
        //add user to users map(list)
        users.put(user.username(),user);
    }


    public UserData getUser(String userName) throws DataAccessException{
        return users.get(userName);
    }


    public int createGame(String gameName) throws DataAccessException{
        newGameID+=1;
        int gameID = newGameID;

        GameData game = new GameData(gameID,null,null,gameName,new ChessGame());
        games.put(gameID,game);

        return gameID;
    }
    public GameData getGame(int gameID)throws DataAccessException{
        return games.get(gameID);
    }

    public List<GameData> listGames() throws DataAccessException{
        return new ArrayList<>(games.values());
    }

    public void updateGame(GameData game) throws DataAccessException{
        if(!games.containsKey(game.gameID())){
            throw new DataAccessException("Game Not Found");
        }
        games.put(game.gameID(),game);//replace the game with the updated one
    }
    public void createAuth(AuthData auth) throws DataAccessException{
        auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException{
        return auths.get(authToken);
    }

    public void removeAuth(String authToken) throws DataAccessException{
        auths.remove(authToken);
    }

}
