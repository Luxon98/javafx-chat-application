package chatapplication.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static chatapplication.server.Command.*;


class Server {
    private static final int SERVER_PORT = 4597;
    private boolean status;
    private ServerSocket serverSocket;
    private List<ClientResource> connectedClients;

    private class ClientThread implements Runnable {
        private int userId;
        private boolean clientStatus = true;
        private Socket clientSocket;
        private Thread runner;
        private final Server server = Server.this;

        public ClientThread(int userId, Socket clientSocket) throws IOException {
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
                while (clientStatus) {
                    int commandNumber = dataInputStream.readInt();
                    Command command = Command.fromInteger(commandNumber);
                    executeCommand(command, dataInputStream);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void executeCommand(Command command, DataInputStream dataInputStream) {
            if (command == MESSAGE) {
                sendMessage(dataInputStream);
            }
            else if (command == FRIENDS_STATUSES) {
                sendFriendsStatuses(dataInputStream);
            }
            else if (command == INVITATION) {
                sendInvitation(dataInputStream);
            }
            else if (command == REDRAW_PANEL) {
                sendRedrawPanelCommand(dataInputStream);
            }
            else if (command == DISCONNECT) {
                disconnectClient();
            }
        }

        private void sendMessage(DataInputStream dataInputStream) {
            try {
                int receiverId = dataInputStream.readInt();
                String messageText = dataInputStream.readUTF();
                ClientResource client = getClient(receiverId);
                if (client != null) {
                    transferMessage(client, messageText);
                }

                System.out.println("Client #" + userId + " send: " + messageText + " to a client #" + receiverId);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void transferMessage(ClientResource client, String message) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                dataOutputStream.writeInt(MESSAGE.getCommandNumber());
                dataOutputStream.writeInt(userId);
                dataOutputStream.writeUTF(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFriendsStatuses(DataInputStream dataInputStream) {
            int[] friendsIndexes = getFriendsIndexes(dataInputStream);
            boolean[] friendsStatuses = getFriendsStatuses(friendsIndexes);

            transferFriendsStatuses(friendsStatuses);
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

        private boolean[] getFriendsStatuses(int[] friendsIndexes) {
            boolean[] friendsStatuses = new boolean[friendsIndexes.length];

            for (ClientResource connectedClient : connectedClients) {
                for (int i = 0; i < friendsIndexes.length; ++i) {
                    if (friendsIndexes[i] == connectedClient.getUserId()) {
                        friendsStatuses[i] = true;
                    }
                }
            }
            return friendsStatuses;
        }

        private void transferFriendsStatuses(boolean[] friendsStatuses) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeInt(FRIENDS_STATUSES.getCommandNumber());
                for (boolean b : friendsStatuses) {
                    dataOutputStream.writeBoolean(b);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendInvitation(DataInputStream dataInputStream) {
            try {
                int receiverId = dataInputStream.readInt();
                ClientResource client = getClient(receiverId);
                if (client != null) {
                    transferInvitation(client);
                }

                System.out.println("User #" + userId + " invited user #" + receiverId + " to friends");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void transferInvitation(ClientResource client) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                dataOutputStream.writeInt(INVITATION.getCommandNumber());
                dataOutputStream.writeInt(userId);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendRedrawPanelCommand(DataInputStream dataInputStream) {
            try {
                int receiverId = dataInputStream.readInt();
                ClientResource client = getClient(receiverId);
                if (client != null) {
                    transferRedrawPanelCommand(client);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void transferRedrawPanelCommand(ClientResource client) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                dataOutputStream.writeInt(REDRAW_PANEL.getCommandNumber());
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
            clientStatus = false;
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
                int commandNumber = dataInputStream.readInt();
                if (Command.fromInteger(commandNumber) == CONNECT) {
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