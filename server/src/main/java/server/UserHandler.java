package server;


import service.UserService;

import com.google.gson.Gson;

import dataaccess.DataAccessException;
import io.javalin.http.Context;

import model.AuthData;
import model.UserData;

//handles all the fuctions we will be calling in user service
public class UserHandler {
    private  UserService userService;
    private Gson gson = new Gson();

    public UserHandler(UserService userService){
        this.userService=userService;
    }
    //register had to get user from gson
    public void register(Context ctx){
        try{

            UserData user = gson.fromJson(ctx.body(), UserData.class);
            AuthData auth = userService.register(user, user.username());

            ctx.status(200).result(gson.toJson(auth));

        } catch (DataAccessException e) {
            String msg = e.getMessage();
            if (msg.contains("Bad Request")) {
                //new error is the only thing that worked I don't know
                ctx.status(400).result(gson.toJson(new ErrorResponse("Error: bad request")));

            } else if (msg.contains("Already Taken")) {
                ctx.status(403).result(gson.toJson(new ErrorResponse("Error: already taken")));
            } else {
                //dont forget anything else
                ctx.status(500).result(gson.toJson(new ErrorResponse("Error: " + msg)));
            }
        }
    }
    //checks if loggedin in userservice
    public void login(Context ctx){
        try{
            UserData user = gson.fromJson(ctx.body(),UserData.class);
            AuthData auth=userService.login(user.username(),user.password());

            ctx.status(200).result(gson.toJson(auth));
        } catch(DataAccessException e) {
            String msg =e.getMessage();
            if(msg.contains("Bad Request")){
                ctx.status(400).result(gson.toJson(new ErrorResponse("Error: bad request")));
            }else if(msg.contains("unauthorized")){
                ctx.status(401).result(gson.toJson(new ErrorResponse("Error: unauthorized")));
            }else{
                ctx.status(500).result(gson.toJson(new ErrorResponse("Error: " + msg)));
            }
        }
    }

    public void logout(Context ctx){
        try{
            String authToken = ctx.header("authorization");
            userService.logout(authToken);

            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            String msg=e.getMessage();
            if(msg.contains("Bad Request")){
                ctx.status(400).result(gson.toJson(new ErrorResponse("Error: bad request")));
            }else if(msg.contains("unauthorized")){
                ctx.status(401).result(gson.toJson(new ErrorResponse("Error: unauthorized")));
            }else{
                ctx.status(500).result(gson.toJson(new ErrorResponse("Error: " + msg)));
            }
        }
    }
}
