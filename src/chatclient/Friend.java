package chatclient;

class Friend {
    private int id;
    private String login;
    private boolean activeStatus;

    public Friend(int id, String login, boolean activeStatus) {
        this.id = id;
        this.login = login;
        this.activeStatus = activeStatus;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public boolean isActive() {
        return activeStatus;
    }

    public void setActiveStatus(boolean status) {
        activeStatus = status;
    }
}
