package chatclient;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.net.*;
import java.io.*;


public class ClientApplication {
    private Socket socket;
    private int userId;

    public ClientApplication(String address, int port, int id) {
        userId = id;

        try {
            socket = new Socket(address, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(userId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        listen();
    }

    public void sendMessage(int id, String message) {
        if (id > 0) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataOutputStream.writeInt(id);
                dataOutputStream.writeUTF(message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCommand(int command) {
        if (command < 0) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeInt(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    if (dataInputStream.available() > 0) {
                        int senderId = dataInputStream.readInt();
                        String text = dataInputStream.readUTF();
                        String username = Database.getLogin(senderId);
                        System.out.println(username + ": " + text);

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
                        Parent root = loader.load();
                        ChatController c = loader.getController();
                        c.addTextLabel();
                    }
                }
                sendCommand(-1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }
}