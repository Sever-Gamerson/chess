package client;

import model.AuthData;
import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade facade;
    private final Scanner scanner=new Scanner(System.in);

    public PreloginUI(ServerFacade facade){
        this.facade=facade;
    }


}
