package chatclient;

import java.util.Arrays;
import java.util.List;


public class Database {
    private static List<User> users = Arrays.asList(new User(1, "roman", "xxx"),
            new User(2, "kyxy", "123"), new User(3, "malwinka", "qwerty"));

    public Database() { }

    public static boolean isUser(String login, String password) {
        for (User user : users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static int getId(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                return user.getId();
            }
        }
        return -1;
    }

    public static String getLogin(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user.getLogin();
            }
        }
        return null;
    }
}
