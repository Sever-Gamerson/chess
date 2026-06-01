package client;

import model.AuthData;
import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade facade;
    private final Scanner scanner=new Scanner(System.in);

    public PreloginUI(ServerFacade facade){
        this.facade=facade;
    }

    public String eval(String input){
        return switch (input){
            case "help" -> help();
            default -> "Unknown command. Type 'help' for options.";
        };
    }
    private String help(){
        return """
                Heres some helpfull commands:
                help -shows this message
                login -login to your account
                register -creates an account
                quit      -close program
                """;
    }

}
