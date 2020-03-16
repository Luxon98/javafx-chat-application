package chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
    private boolean status;
    private ServerSocket serverSocket;
    private List<ClientResource> connectedClients;

    private class ClientThread implements Runnable {
        private int userId;
        private Socket clientSocket;
        private Thread runner;
        private final Server server = Server.this;

        public ClientThread(int userId, Socket clientSocket) throws IOException {
            if (runner == null) {
                this.userId = userId;
                this.clientSocket = clientSocket;

                server.connectedClients.add(new ClientResource(userId, clientSocket.getOutputStream()));
                runner = new Thread(this);
                runner.start();

                System.out.println("Client #" + userId + " connected");
            }
        }

        public void run() {
            DataInputStream dataInputStream;
            try {
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                while (true) {
                    try {
                        int receiverId = dataInputStream.readInt();

                        if (!isDisconnectCommand(receiverId)) {
                            String text = dataInputStream.readUTF();
                            System.out.println("Client #" + userId + " send: " + text + " to a client #" + receiverId);
                            send(receiverId, text);
                        } else {
                            System.out.println("Client #" + userId + " disconnected");
                            connectedClients.removeIf(client -> client.getUserId() == userId);
                            clientSocket.close();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void send(int receiverId, String message) {
            ClientResource client = getClient(receiverId);
            if (client != null) {
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                    dataOutputStream.writeInt(userId);
                    dataOutputStream.writeUTF(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Server() {
        try {
            serverSocket = new ServerSocket(4567);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to start the server.");
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
                int id = dataInputStream.readInt();

                ClientThread clientThread = new ClientThread(id, listeningSocket);
            } catch (IOException e) {
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

    private boolean isDisconnectCommand(int clientId) {
        return (clientId == -1);
    }
}
