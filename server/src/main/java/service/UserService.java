package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;


public class UserService {
    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess =dataAccess;
    }

    public AuthData register(UserData user,String userName) throws DataAccessException{
        if(user.username()==null|| user.email()==null||user.password()==null){
            throw new DataAccessException("Bad Request");
        }

        if(dataAccess.getUser(user.username())!=null){
            throw new DataAccessException("Already Taken");
        }

        dataAccess.createUser(user);

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token,userName);
        dataAccess.createAuth(auth);
        return auth;
    }


    public AuthData login(String userName,String password) throws DataAccessException{
        if(userName == null || password == null){
            throw new DataAccessException("Bad Request");
        }

        UserData user= dataAccess.getUser(userName);

        if(user==null || !user.password().equals(password)){
            throw new DataAccessException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token,userName);
        dataAccess.createAuth(auth);
        return auth;

    }

    public void logout(String authToken) throws DataAccessException{
        if(dataAccess.getAuth(authToken)==null){
            throw new DataAccessException("unauthorized");//user not loged in
        }
        dataAccess.deleteAuth(authToken);
    }


}
