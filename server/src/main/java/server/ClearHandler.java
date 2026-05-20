package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {

    private Gson gson=new Gson();
    private ClearService clearService;

    public ClearHandler(ClearService clearService){
        this.clearService=clearService;
    }
    //calls clear service and returns anything that goes wrong
    public void clear(Context ctx){
        try {

            clearService.clear();//call clear service
            ctx.status(200).result("{}");

        }catch (DataAccessException e){

            if(e.getMessage()!=null){
                ctx.status(500).json(new Error("Error: " + e.getMessage()));
            }

        }
    }
}
