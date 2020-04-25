package chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static chatserver.Command.*;
import static chatserver.DatabaseQueries.*;


class Server {
    private static final int SERVER_PORT = 4597;
    private boolean status;
    private ServerSocket serverSocket;
    private List<ClientResource> connectedClients;

    private class ClientThread implements Runnable {
        private int userId;
        private Socket clientSocket;
        private Thread runner;
        private final Server server = Server.this;

        public ClientThread(int userId, Socket clientSocket) throws IOException {
            //if (runner == null) {}

            this.userId = userId;
            this.clientSocket = clientSocket;

            server.connectedClients.add(new ClientResource(userId, clientSocket.getOutputStream()));
            runner = new Thread(this);
            runner.start();

            System.out.println("Client #" + userId + " connected");
        }

        public void run() {
            DataInputStream dataInputStream;
            try {
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                while (true) {
                    int command = dataInputStream.readInt();
                    if (command == MESSAGE) {
                        handleMessage(dataInputStream);
                    }
                    else if (command == FRIENDS_STATUSES) {
                        handleFriendsStatusRequest(dataInputStream);
                    }
                    else if (command == INVITATION) {
                        handleInvitation(dataInputStream);
                    }
                    else if (command == TEST) {             // do kasacji potem
                        System.out.println("Client #" + userId + " - test");
                        //sendMessage(userId, 2, "test");
                        ClientResource client = getClient(5);
                        if (client != null) {
                            try {
                                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                                dataOutputStream.writeInt(INVITATION);
                                dataOutputStream.writeInt(4);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (command == DISCONNECT) {
                        disconnectClient();
                        return;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleMessage(DataInputStream dataInputStream) {
            try {
                int receiverId = dataInputStream.readInt();
                String text = dataInputStream.readUTF();
                sendMessage(receiverId, userId, text);
                System.out.println("Client #" + userId + " send: " + text + " to a client #" + receiverId);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void sendMessage(int receiverId, int senderId, String message) {          // potem senderId do wywalenia
            ClientResource client = getClient(receiverId);
            if (client != null) {
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                    dataOutputStream.writeInt(MESSAGE);
                    dataOutputStream.writeInt(senderId);
                    dataOutputStream.writeUTF(message);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //else {
            //    wyslij wiadomosc do bazy
            //}
        }

        private void handleFriendsStatusRequest(DataInputStream dataInputStream) {
            int[] friendsIndexes = getFriendsIndexes(dataInputStream);
            sendFriendsStatuses(friendsIndexes);
            System.out.println("Client #" + userId + " - friends statuses send");
        }

        private int[] getFriendsIndexes(DataInputStream dataInputStream) {
            int[] friendsIndexes = null;
            try {
                int length = dataInputStream.readInt();
                friendsIndexes = new int[length];
                for (int i = 0; i < length; ++i) {
                    friendsIndexes[i] = dataInputStream.readInt();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return friendsIndexes;
        }

        private void sendFriendsStatuses(int[] arr) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                boolean[] statuses = new boolean[arr.length];
                for (ClientResource connectedClient : connectedClients) {
                    for (int i = 0; i < arr.length; ++i) {
                        if (arr[i] == connectedClient.getUserId()) {
                            statuses[i] = true;
                        }
                    }
                }

                dataOutputStream.writeInt(FRIENDS_STATUSES);
                for (boolean b : statuses) {
                    dataOutputStream.writeBoolean(b);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleInvitation(DataInputStream dataInputStream) {
            try {
                int receiverId = dataInputStream.readInt();
                System.out.println("User #" + userId + " invited user #" + receiverId + " to friends");
                ClientResource client = getClient(receiverId);
                if (client != null) {
                    try {
                        DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                        dataOutputStream.writeInt(INVITATION);
                        dataOutputStream.writeInt(userId);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    insertNewInvitation(userId, receiverId);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnectClient() {
            System.out.println("Client #" + userId + " disconnected");
            connectedClients.removeIf(client -> client.getUserId() == userId);
            try {
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public Server() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to start the server.");
            return;
        }

        status = true;
        connectedClients = new ArrayList<>();

        System.out.println("Server has started properly.\nWaiting for clients...");
        listen();
    }

    private void listen() {
        while (status) {
            try {
                Socket listeningSocket = serverSocket.accept();

                DataInputStream dataInputStream = new DataInputStream(listeningSocket.getInputStream());
                if (dataInputStream.readInt() == CONNECT) {
                    int id = dataInputStream.readInt();
                    ClientThread clientThread = new ClientThread(id, listeningSocket);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ClientResource getClient(int receiverId) {
        return connectedClients.stream()
                .filter(connectedClient -> receiverId == connectedClient.getUserId())
                .findAny()
                .orElse(null);
    }
}