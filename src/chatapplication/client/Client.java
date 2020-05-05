package chatapplication.client;

class Client {
    private final static Client instance = new Client();
    private String username;
    private boolean closedFlag;

    public static Client getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isProgramClosed() {
        return closedFlag;
    }

    public void setClosedFlag(boolean closedFlag) {
        this.closedFlag = closedFlag;
    }
}
