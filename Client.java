package chatclient;

public class Client {
    private final static Client instance = new Client();
    private String username;

    public static Client getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
