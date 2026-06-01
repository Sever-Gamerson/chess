package client;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ Welcome to Chess! Type 'help' to get started.");
        new Repl(8080).run();//starts the loop!
    }
}
