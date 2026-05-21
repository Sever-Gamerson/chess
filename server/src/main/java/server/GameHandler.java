package server;

import service.GameService;
import com.google.gson.Gson;


import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.GameData;
import model.JoinData;

import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService){
        this.gameService=gameService;
    }

    public void listGames(Context ctx){
        try {
            String authToken=ctx.header("authorization");
            List<GameData> gameList=gameService.listGames(authToken);
            ctx.status(200).result(gson.toJson(Map.of("games",gameList)));
        }catch (DataAccessException e){
            checkAllErrors(ctx,e.getMessage());
        }
    }

    public void createGame(Context ctx){
        try {
            String authToken=ctx.header("authorization");
            GameData game=gson.fromJson(ctx.body(),GameData.class);

            int gameID = gameService.createGame(game.gameName(),authToken);

            ctx.status(200).result(gson.toJson(Map.of("gameID",gameID)));
        }catch (DataAccessException e){
            checkAllErrors(ctx,e.getMessage());

        }
    }

    public void joinGame(Context ctx){
        try{
            String authToken=ctx.header("authorization");
            JoinData joinData=gson.fromJson(ctx.body(),JoinData.class);

            gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());

            ctx.status(200).result("{}");

        }catch (DataAccessException e) {
            checkAllErrors(ctx,e.getMessage());
        }
    }
    private void checkAllErrors(Context ctx,String msg){
        if(msg.contains("Bad Request")){
            sendError(ctx, 400, "Error: bad request");
        }else if (msg.contains("Team Taken")) {
            sendError(ctx, 403, "Error: already taken");
        }else if(msg.contains("unauthorized")){
            sendError(ctx, 401, "Error: unauthorized");
        }else{
            sendError(ctx, 500, "Error: " + msg);
        }
    }
    private void sendError(Context ctx, int status, String message) {
        ctx.status(status).result(gson.toJson(new ErrorResponse(message)));
    }
}
