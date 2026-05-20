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

            ctx.status(200).json(auth);

        } catch (DataAccessException e) {
            String msg = e.getMessage();
            if (msg.contains("Bad Request")) {
                //new error is the only thing that worked I don't know
                ctx.status(400).json(new Error("Bad Request"));

            } else if (msg.contains("Already Taken")) {
                ctx.status(403).json(new Error("Already Taken"));
            } else {

                ctx.status(500).json(new Error(msg));
            }
        }
    }
}
