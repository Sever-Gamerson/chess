package client;

import model.AuthData;
import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade facade;
    private final Scanner scanner=new Scanner(System.in);
    private final int port;

    public PreloginUI(ServerFacade facade,int port){
        this.facade=facade;
        this.port = port;
    }

    public String eval(String input){
        return switch (input){
            case "help" -> help();
            case "login" -> login();
            case "register"-> register();
            case "quit"-> "Goodbye!";
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

    private String login(){
        System.out.print("Username: ");
        String username=scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try{
            AuthData auth = facade.login(username,password);
            new PostloginUI(facade, auth, port).run();

            return "logged Out!";
        }catch (Exception e){
            return "Login failed: "+e.getMessage();

        }
    }

    private String register(){
        System.out.print("Username: ");
        String username=scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try{
            AuthData auth =facade.register(username,password,email);

            new PostloginUI(facade, auth, port).run();
            return "Logged Out";

        }catch(Exception e){
            return "Registration failed: "+e.getMessage();
        }
    }
}
