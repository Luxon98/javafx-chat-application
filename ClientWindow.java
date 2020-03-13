package chatclient;

import java.util.Scanner;

public class ClientWindow {
    public static void main(String[] args) {
        String name = "Lukasz";
        String message = "";
        Client client = new Client(name, "localhost", 5555);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            message = scanner.nextLine();
            if (message.length() > 1) {
                client.send(name + ": " + message);
            }
            else {
                break;
            }
        }
    }
}

