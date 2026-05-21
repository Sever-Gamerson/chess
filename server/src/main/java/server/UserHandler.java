package server;


import service.UserService;

import com.google.gson.Gson;

import dataaccess.DataAccessException;
import io.javalin.http.Context;

import model.AuthData;
import model.UserData;

//handles all the functions we will be calling in user service
public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

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
            checkAllErrors(ctx,e.getMessage());
        }
    }
    //checks if logged in in user service
    public void login(Context ctx){
        try{
            UserData user = gson.fromJson(ctx.body(),UserData.class);
            AuthData auth=userService.login(user.username(),user.password());

            ctx.status(200).result(gson.toJson(auth));
        }catch(DataAccessException e) {
            checkAllErrors(ctx,e.getMessage());
        }
    }

    public void logout(Context ctx){
        try{
            String authToken = ctx.header("authorization");
            userService.logout(authToken);

            ctx.status(200).result("{}");
        }catch (DataAccessException e){
            checkAllErrors(ctx,e.getMessage());
        }
    }
    private void checkAllErrors(Context ctx,String msg){
        if(msg.contains("Bad Request")){
            sendError(ctx, 400, "Error: bad request");
        }else if (msg.contains("Already Taken")) {
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
