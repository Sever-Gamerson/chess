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

    public void clear(Context ctx){
        try {

            clearService.clear();
            ctx.status(200).result("{}");

        }catch (DataAccessException e){

            ctx.status(500).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
