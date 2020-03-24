package chatclient;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class ClientApplication {
    private Socket socket;
    private int userId;
    private List<User> friendsList;
    private Stack<String> messagesStack;

    public ClientApplication(String address, int port, int id) {
        userId = id;
        friendsList = new ArrayList<>(Database.getFriends(userId));
        messagesStack = new Stack<>();

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

                        messagesStack.push(text);
                    }
                }
                sendCommand(-1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    public List<User> getFriendsList() {
        return friendsList;
    }

    public String getMessage() {
        String message = messagesStack.pop();
        return message;
    }

    public boolean containsMessage() {
        return (!messagesStack.empty());
    }
}