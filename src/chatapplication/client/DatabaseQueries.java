package chatapplication.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;


class DatabaseQueries {

    public static boolean isExistingUsername(String username) {
        String query = "SELECT * FROM Users WHERE username = ?";

        return isVariableExistingInDatabase(username, query);
    }

    public static boolean isExistingEmail(String emailAddress) {
        String query = "SELECT * FROM Users WHERE email = ?";

        return isVariableExistingInDatabase(emailAddress, query);
    }

    private static boolean isVariableExistingInDatabase(String variable, String query) {
        boolean result = false;

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, variable);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                result = resultSet.next();
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return result;
    }

    public static boolean isExistingUser(String username, String password) {
        boolean result = false;
        String query = "SELECT * FROM Users WHERE username = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    result = BCrypt.checkpw(password, hashedPassword);
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return result;
    }

    public static boolean isExistingInvitation(int firstUserId, int secondUserId) {
        boolean result = false;
        String query = "SELECT * FROM Invitations WHERE inviting_user_id = ? AND invited_user_id = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, firstUserId);
            preparedStatement.setInt(2, secondUserId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                result = resultSet.next();
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return result;
    }

    public static void insertNewUser(String username, String emailAddress, String hashedPassword) {
        String query = "INSERT INTO Users(username, email, password) VALUES (?, ?, ?)";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, emailAddress);
            preparedStatement.setString(3, hashedPassword);
            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }

    public static String getUsername(int userId) {
        String username = null;
        String query = "SELECT username FROM Users WHERE user_id = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    username = resultSet.getString("username");
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return username;
    }

    public static int getId(String username) {
        int id = -1;
        String query = "SELECT user_id FROM Users WHERE username = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt("user_id");
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return id;
    }

    public static int getAvatarType(int userId) {
        int avatarType = 1;
        String query = "SELECT avatar_type FROM Users WHERE user_id = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    avatarType = resultSet.getInt("avatar_type");
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return avatarType;
    }

    public static void updateAvatarType(int userId, int avatarType) {
        String query = "UPDATE Users SET avatar_type = ? WHERE user_id = ?";

        executeUpdateQuery(avatarType, userId, query);
    }

    public static void insertNewInvitation(int invitingUserId, int invitedUserId) {
        String query = "INSERT INTO Invitations(inviting_user_id, invited_user_id) VALUES (?, ?)";

        executeUpdateQuery(invitingUserId, invitedUserId, query);
    }

    public static void removeInvitation(int invitingUserId, int invitedUserId) {
        String query = "DELETE FROM Invitations WHERE inviting_user_id = ? AND invited_user_id = ?";

        executeUpdateQuery(invitingUserId, invitedUserId, query);
    }

    public static void insertNewFriendship(int firstUserId, int secondUserId) {
        String query = "INSERT INTO Friendships(first_user_id, second_user_id) VALUES (?, ?)";

        executeUpdateQuery(firstUserId, secondUserId, query);
    }

    public static void removeFriendship(int firstUserId, int secondUserId) {
        String query = "DELETE FROM Friendships WHERE first_user_id = ? AND second_user_id = ?";

        executeUpdateQuery(firstUserId, secondUserId, query);
        executeUpdateQuery(secondUserId, firstUserId, query);
    }

    private static void executeUpdateQuery(int firstParameter, int secondParameter, String query) {
        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, firstParameter);
            preparedStatement.setInt(2, secondParameter);
            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }

    public static int[] getFriendsIds(int userId) {
        String firstQuery = "SELECT friendship_id, second_user_id FROM Friendships WHERE first_user_id = ?";
        Map<Integer, Integer> friends = new TreeMap<>(getFriendsMap(userId, firstQuery));

        String secondQuery = "SELECT friendship_id, first_user_id FROM Friendships WHERE second_user_id = ?";
        friends.putAll(getFriendsMap(userId, secondQuery));

        return friends.values().stream().mapToInt(i -> i).toArray();
    }

    private static Map<Integer, Integer> getFriendsMap(int userId, String query) {
        Map<Integer, Integer> friends = new TreeMap<>();
        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                friends = getMapFromSet(resultSet, query);
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return friends;
    }

    private static Map<Integer, Integer> getMapFromSet(ResultSet resultSet, String query) {
        Map<Integer, Integer> treeMap = new TreeMap<>();
        try {
            while (resultSet.next()) {
                int key = resultSet.getInt("friendship_id");
                int value = resultSet.getInt(query.contains(", second_user_id") ? "second_user_id" : "first_user_id");
                treeMap.put(key, value);
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return treeMap;
    }

    public static Integer[] getPendingInvitations(int userId) {
        List<Integer> prospectiveFriendsIdsList = new ArrayList<>();
        String query = "SELECT inviting_user_id FROM Invitations WHERE invited_user_id = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    prospectiveFriendsIdsList.add(resultSet.getInt("inviting_user_id"));
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return prospectiveFriendsIdsList.toArray(new Integer[0]);
    }

    public static void removePendingInvitations(int userId) {
        String query = "DELETE FROM Invitations WHERE invited_user_id = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }

    public static void insertNewMessage(int senderId, int receiverId, String text) {
        String query = "INSERT INTO Messages(text, sender_id, receiver_id, was_read) VALUES (?, ?, ?, false)";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, text);
            preparedStatement.setInt(2, senderId);
            preparedStatement.setInt(3, receiverId);

            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }

    public static void updateMessages(int senderId, int receiverId) {
        String query = "UPDATE Messages SET was_read = ? WHERE sender_id = ? AND receiver_id = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, senderId);
            preparedStatement.setInt(3, receiverId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }

    public static boolean isUnreadMessage(int senderId, int receiverId) {
        String query = "SELECT * FROM Messages WHERE sender_id = ? AND receiver_id = ? AND was_read = false";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, senderId);
            preparedStatement.setInt(2, receiverId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return false;
    }

    public static Message[] getMessages(int userId, int friendId) {
        String query = "SELECT message_id, text, sender_id, receiver_id from Messages WHERE sender_id = ? AND receiver_id = ?";
        Map<Integer, Message> messages = new TreeMap<>(getMessagesMap(userId, friendId, query));

        messages.putAll(getMessagesMap(friendId, userId, query));
        for (int i : messages.keySet()) {
            int senderId = messages.get(i).getSenderId();
            if (senderId == userId) {
                Message message = new Message(senderId, messages.get(i).getMessage(), false);
                messages.replace(i, message);
            }
        }

        Collection<Message> messagesValues = messages.values();
        return messages.values().toArray(new Message[messagesValues.size()]);
    }

    private static Map<Integer, Message> getMessagesMap(int userId, int friendId, String query) {
        Map<Integer, Message> messages = new TreeMap<>();

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, friendId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                messages = getMessagesMapFromSet(resultSet);
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);

        return messages;
    }

    private static Map<Integer, Message> getMessagesMapFromSet(ResultSet resultSet) {
        Map<Integer, Message> treeMap = new TreeMap<>();
        try {
            while (resultSet.next()) {
                int key = resultSet.getInt("message_id");
                int senderId = resultSet.getInt("sender_id");
                String text = resultSet.getString("text");

                treeMap.put(key, new Message(senderId, text, true));
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return treeMap;
    }
}