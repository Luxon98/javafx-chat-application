package chatclient;

import javafx.application.Platform;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ClientApplication {
    private Socket socket;
    private int userId;
    private ChatController chatController;
    private List<User> friendsList;

    public ClientApplication(String address, int port, int id) {
        userId = id;
        friendsList = new ArrayList<>(Database.getFriends(userId));

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

                        Platform.runLater(() -> {
                            chatController.drawReceivedMessageLabel(text);
                        });
                    }
                }
                sendCommand(-1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }

    public List<User> getFriendsList() {
        return friendsList;
    }
}