package chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
    static final int PORT = 4321;
    private ServerSocket serverSocket;
    private Socket listeningSocket;
    private List<ClientInfo> connectedClients;

    private class ClientThread implements Runnable {
        private Socket clientSocket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private final Server server = Server.this;

        public ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            server.connectedClients.add(new ClientInfo(1, clientSocket.getInetAddress(), clientSocket.getLocalPort()));
            //System.out.println(clientSocket.getInetAddress() + ":" + clientSocket.getLocalPort());
        }

        public void run() {
            try {
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                return;
            }

            while (true) {
                try {
                    int clientId = dataInputStream.readInt();
                    //System.out.println(clientId);               // REMOVE

                    if (!isDisconnectCommand(clientId)) {
                        String text = dataInputStream.readUTF();
                        System.out.println(text);               // REMOVE
                        send(1, text);
                    } else {
                        System.out.println("Client disconnected.");
                        clientSocket.close();
                        return;
                    }
                } catch (EOFException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void send(int userId, String message) {
//            ClientInfo client = connectedClients.stream()
//                    .filter(c -> userId == c.getUserId())
//                    .findAny()
//                    .orElse(null);

            //if (client != null) {
            try {
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //}
        }
    }

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to start the server.");
        }

        connectedClients = new ArrayList<>();

        System.out.println("Server has started properly.\nWaiting for clients...");
        listen();
    }

    private void listen() {
        while (true) {
            try {
                listeningSocket = serverSocket.accept();
                System.out.println("Client connected");         // REMOVE
                ClientThread clientThread = new ClientThread(listeningSocket);
                clientThread.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDisconnectCommand(int clientId) {
        return (clientId == -1);
    }
}