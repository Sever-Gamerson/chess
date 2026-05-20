package server;
import service.GameService;
import com.google.gson.Gson;


import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.GameData;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;

public class GameHandler {
    private GameService gameService;
    private Gson gson = new Gson();

    public GameHandler(GameService gameService){
        this.gameService=gameService;
    }

    public void listGames(Context ctx){
        try {
            String authToken=ctx.header("authorization");
            List<GameData> gameList=gameService.listGames(authToken);
            ctx.status(200).json(Map.of("games",gameList));
        }catch (DataAccessException e){
            String msg=e.getMessage();

            if(msg.contains("unauthorized")) {
                ctx.status(401).json(new Error("unauthorized"));
            }else {
                ctx.status(500).json(new Error(msg));
            }

        }
    }

    public void createGame(Context ctx){
        try {
            String authToken=ctx.header("authorization");
            GameData game=gson.fromJson(ctx.body(),GameData.class);

            int gameID = gameService.createGame(game.gameName(),authToken);

            ctx.status(200).json(Map.of("gameIDs",gameID));
        }catch (DataAccessException e){
            String msg=e.getMessage();

            if (msg.contains("Bad Request")) {
                ctx.status(400).json(new Error("Bad Request"));
            } else if(msg.contains("unauthorized")) {
                ctx.status(401).json(new Error("unauthorized"));
            }else {
                ctx.status(500).json(new Error(msg));
            }

        }
    }

    public void joinGame(Context ctx){
        try{
            String authToken=ctx.header("authorization");




        } catch (RuntimeException e) {
            String msg=e.getMessage();

            if (msg.contains("Bad Request")) {
                ctx.status(400).json(new Error("Bad Request"));
            } else if(msg.contains("unauthorized")) {
                ctx.status(401).json(new Error("unauthorized"));
            } else if(msg.contains("already taken")) {
                ctx.status(4013).json(new Error("already taken"));
            }else {
                ctx.status(500).json(new Error(msg));
            }
        }
    }
}
