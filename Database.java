package chatclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Database {
    private static List<User> users = Arrays.asList(
            new User(1, "roman", "xxx", "roman98@wp.pl"),
            new User(2, "kyxy", "123", "kyxy@gmail.com"),
            new User(3, "malwinka", "qwerty", "malwis14@op.pl"));

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

    public static boolean isLoginTaken(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmailTaken(String emailAddress) {
        for (User user : users) {
            if (user.getEmailAddress().equals(emailAddress)) {
                return true;
            }
        }
        return false;
    }

    public static List<User> getFriends(int id) {
        List<User> list = new ArrayList<>();
        for (User user : users) {
            if (user.getId() != id) {
                list.add(user);
            }
        }
        return list;
    }
}
