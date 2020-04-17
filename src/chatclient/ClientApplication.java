package chatclient;

import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static chatclient.Command.*;


public class ClientApplication {
    private int userId;
    private Socket socket;
    private List<Friend> friendsList;
    private Stack<Message> messagesStack;

    public ClientApplication(String address, int port, int id) {
        userId = id;
        friendsList = new ArrayList<>(AuxiliaryDatabase.getFriends(userId));
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
        checkFriendsStatuses();
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    if (dataInputStream.available() > 0) {
                        int command = dataInputStream.readInt();
                        if (command == MESSAGE) {
                            receiveMessage(dataInputStream);
                        }
                        else if (command == FRIENDS_STATUSES) {
                            updateFriendsStatus(dataInputStream);
                        }
                    }
                }
                disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    private void receiveMessage(DataInputStream dataInputStream) {
        try {
            int senderId = dataInputStream.readInt();
            String text = dataInputStream.readUTF();
            messagesStack.push(new Message(senderId, text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFriendsStatus(DataInputStream dataInputStream) {
        try {
            for (int i = 0; i < friendsList.size(); ++i) {
                friendsList.get(i).setActiveStatus(dataInputStream.readBoolean());
                System.out.println(friendsList.get(i).getLogin() + " - " + (friendsList.get(i).isActive() ? "online" : "offline"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFriendsStatuses() {
        Thread thread = new Thread(() -> {
            try {
                Instant start = Instant.now();
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    Instant finish = Instant.now();
                    if (Duration.between(start, finish).toMillis() > 15000) {
                        dataOutputStream.writeInt(FRIENDS_STATUSES);
                        dataOutputStream.writeInt(friendsList.size());
                        for (int i = 0; i < friendsList.size(); ++i) {
                            dataOutputStream.writeInt(friendsList.get(i).getId());
                        }
                        start = finish;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
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

    private void disconnect() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(DISCONNECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TO REMOVE
    public void sendTest() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(TEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getListIndex(int id) {
        for (int i = 0; i < friendsList.size(); ++i) {
            if (friendsList.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public List<Friend> getFriendsList() {
        return friendsList;
    }

    public boolean containsMessage() {
        return (!messagesStack.empty());
    }

    public Message getMessage() {
        return messagesStack.pop();
    }

    public Boolean[] getFriendsStatuses() {
        return friendsList.stream()
                .map(Friend::isActive)
                .toArray(Boolean[]::new);
    }
}