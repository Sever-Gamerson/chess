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

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

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
