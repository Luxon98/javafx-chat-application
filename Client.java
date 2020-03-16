package chatclient;

import java.net.*;
import java.io.*;
import java.util.Scanner;


public class Client {
    private Socket socket;
    private boolean exitProgramFlag;
    private int userId;

    public Client(String address, int port) {
        exitProgramFlag = false;
        userId = 1;
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(userId);
        } catch (IOException e) {
            System.out.println(e);
        }

        listen();
        readInputAndSendMessages();
        //exit();
    }

    private void readInputAndSendMessages() {
        try {
            Scanner scanner = new Scanner(System.in);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while (true) {
                try {
                    String text = scanner.nextLine();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                while (!exitProgramFlag) {
                    if (dataInputStream.available() > 0) {
                        int senderId = dataInputStream.readInt();
                        String text = dataInputStream.readUTF();
                        System.out.println("User #" + senderId + " send to You: " + text);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

//    private void exit() {
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}