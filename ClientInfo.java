package chatserver;

import java.net.InetAddress;

public class ClientInfo {
    private int userId;
    private InetAddress address;
    private int port;

    public ClientInfo(int userId, InetAddress address, int port) {
        this.userId = userId;
        this.address = address;
        this.port = port;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
