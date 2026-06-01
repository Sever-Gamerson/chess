package client;
import java.util.Scanner;

public class Repl {
    private final PreloginUI preloginUI;

    public Repl(int port) {
        ServerFacade facade = new ServerFacade(port);
        this.preloginUI = new PreloginUI(facade);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");

            String input = scanner.nextLine().trim().toLowerCase();


            String result = preloginUI.eval(input);
            System.out.println(result);

            if (result.equals("Goodbye!")) {//for the break statement
                break;
            }
        }
    }
}
