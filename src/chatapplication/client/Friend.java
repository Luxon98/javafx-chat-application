package chatapplication.client;


class Friend {
    private int id;
    private String username;
    private boolean activeStatus;

    public Friend(int id, String login, boolean activeStatus) {
        this.id = id;
        this.username = login;
        this.activeStatus = activeStatus;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActive() {
        return activeStatus;
    }

    public void setActiveStatus(boolean status) {
        activeStatus = status;
    }
}
