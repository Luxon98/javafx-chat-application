package chatclient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean status;

    Client(String name, String address, int port) {
        try {
            this.address = InetAddress.getByName(address);
            this.port = port;
            this.socket = new DatagramSocket();
            this.status = true;

            listen();
            send("\\con:" + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        try {
            message += "\\e";
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        } catch (Exception e) {
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
                        System.out.println(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        listenThread.start();
    }

}
