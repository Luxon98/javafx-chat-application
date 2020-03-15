package chatclient;

import chatserver.Server;

import java.net.*;
import java.io.*;
import java.util.Scanner;


public class Client {
    private Socket socket = null;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    boolean exitProgramFlag = false;

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            System.out.println(e);
        }

        listen();

        Scanner scanner = new Scanner(System.in);
        String text = "";

        while (true) {
            try {
                text = scanner.nextLine();
                if (text.length() > 0) {
                    dataOutputStream.writeInt(1);
                    dataOutputStream.writeUTF(text);
                } else {
                    dataOutputStream.writeInt(-1);
                    exitProgramFlag = true;
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            dataOutputStream.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());

                while (!exitProgramFlag) {
                    if (dataInputStream.available() > 0) {
                        String text = dataInputStream.readUTF();
                        System.out.println(text);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 4321);
    }
}