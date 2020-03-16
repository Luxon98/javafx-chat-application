package chatclient;

import java.util.Scanner;


public class ClientApplication {
    public static void main(String args[]) {
        boolean loggedUser = false;

        Scanner scanner = new Scanner(System.in);
        while (!loggedUser) {
            System.out.println("Podaj login: ");
            String login = scanner.nextLine();
            System.out.println("Podaj haslo: ");
            String password = scanner.nextLine();

            if (Database.isUser(login, password)) {
                loggedUser = true;
            }
        }

        System.out.println("CLIENT WINDOW:");
        Client client = new Client("127.0.0.1", 4567, 1);
    }
}
