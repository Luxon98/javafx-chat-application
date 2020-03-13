package chatserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {
    private DatagramSocket socket;
    private int port;
    private boolean status;
    private int clientId;
    private ArrayList<ClientParameters> clients;

    public Server(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            status = true;
            clientId = 0;
            clients = new ArrayList<>();
            listen();
            System.out.println("Server has started properly on port " + port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        Thread listenThread = new Thread() {
            public void run() {
                try {
                    while (status) {
                        byte[] data = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);

                        String message = new String(data);
                        message = message.substring(0, message.indexOf("\\e"));


                        if (!isCommand(message, packet)) {
                            broadcast(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        listenThread.start();
    }

    private void broadcast(String message) {
        for (ClientParameters param : clients) {
            send(message, param.getAddress(), param.getPort());
        }
    }

    private void send(String message, InetAddress address, int port) {
        try {
            message += "\\e";
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            System.out.println("Send message to: " + address.getHostAddress() + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        SERVER COMMANDS:
        \con:[name] - client dissconected
        \dis:[id] - client connected

     */
    private boolean isCommand(String message, DatagramPacket packet) {
        if (message.startsWith("\\con:")) {
            //run connection code
            String name = message.substring(message.indexOf(":") + 1);
            clients.add(new ClientParameters(name, ++clientId, packet.getAddress(), packet.getPort()));
            System.out.println("User " + name + " connected");
            broadcast("User " + name + " connected");

            return true;
        }

        return false;
    }

    public void stop() {
        status = false;
    }
}
