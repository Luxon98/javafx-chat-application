package chatclient;


class User {
    private int id;
    private String login;
    private String password;
    private String emailAddress;

    public User(int id, String login, String password, String emailAddress) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.emailAddress = emailAddress;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
