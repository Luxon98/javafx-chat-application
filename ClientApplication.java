package chatclient;

import java.net.*;
import java.io.*;
import java.util.*;
import static chatclient.Command.*;


public class ClientApplication {
    private int userId;
    private Socket socket;
    private List<User> friendsList;
    private Stack<Message> messagesStack;

    public ClientApplication(String address, int port, int id) {
        userId = id;
        friendsList = new ArrayList<>(Database.getFriends(userId));
        messagesStack = new Stack<>();

        try {
            socket = new Socket(address, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(CONNECT);
            dataOutputStream.writeInt(userId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        listen();
    }

    public void sendMessage(int id, String message) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(MESSAGE);
            dataOutputStream.writeInt(id);
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendCommand(int command) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    if (dataInputStream.available() > 0 && dataInputStream.readInt() == MESSAGE) {
                        int senderId = dataInputStream.readInt();
                        String text = dataInputStream.readUTF();

                        messagesStack.push(new Message(senderId, text));
                    }
                }
                sendCommand(DISCONNECT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    public List<User> getFriendsList() {
        return friendsList;
    }

    public Message getMessage() {
        return messagesStack.pop();
    }

    public boolean containsMessage() {
        return (!messagesStack.empty());
    }
}