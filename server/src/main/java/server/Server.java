package server;

import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import io.javalin.*;

import dataaccess.DataAccess;

import dataaccess.MemoryDataAccess;

import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        DataAccess dataAccess;
        try {
            dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        ClearHandler clearHandler= new ClearHandler(new ClearService(dataAccess));
        UserHandler userHandler= new UserHandler(new UserService(dataAccess));
        GameHandler gameHandler= new GameHandler(new GameService(dataAccess));

        //added and linked to wedsockethandler
        GameSessionManager sessionManager = new GameSessionManager();
        WebSocketHandler wsHandler = new WebSocketHandler(dataAccess, sessionManager);



        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> wsHandler.onConnect(ctx.session));
            ws.onClose(ctx -> wsHandler.onClose(ctx.session, ctx.status(), ctx.reason()));
            ws.onError(ctx -> wsHandler.onError(ctx.session, ctx.error()));
            ws.onMessage(ctx -> wsHandler.onMessage(ctx.session, ctx.message()));
        });



        javalin.delete("/db",clearHandler::clear);

        javalin.post("/user",userHandler::register);
        javalin.post("/session",userHandler::login);
        javalin.delete("/session",userHandler::logout);


        javalin.post("/game",gameHandler::createGame);
        javalin.put("/game",gameHandler::joinGame);
        javalin.get("/game",gameHandler::listGames);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
