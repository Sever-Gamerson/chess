package client;

import com.google.gson.Gson;

public class ServerFacade {

    private final String severUrl;

    private final Gson gson=new Gson();

    public ServerFacade(int port){
        this.severUrl="http://localhost:"+port;
    }
}
