package chatclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class AuxiliaryDatabase {
    private static List<User> users = Arrays.asList(
            new User(1, "roman", "xxx", "roman98@wp.pl"),
            new User(2, "kyxy", "123", "kyxy@gmail.com"),
            new User(3, "malwinka", "qwerty", "malwis14@op.pl"));

    public static List<Friend> getFriends(int id) {
        List<Friend> list = new ArrayList<>();
        for (User user : users) {
            if (user.getId() != id) {
                Friend friend = new Friend(user.getId(), user.getLogin(), false);
                list.add(friend);
            }
        }
        return list;
    }
}
