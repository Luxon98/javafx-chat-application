package chatclient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;


class DatabaseQueries {

    public static boolean isUsernameAlreadyTaken(String username) {
        String query = "SELECT * FROM User WHERE username = ?";
        return isVariableExistingInDatabase(username, query);
    }

    public static boolean isEmailAlreadyTaken(String emailAddress) {
        String query = "SELECT * FROM User WHERE email = ?";
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
        String query = "SELECT * FROM User WHERE username = ?";

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

    public static void insertNewUser(String username, String emailAddress, String hashedPassword) {
        String query = "INSERT INTO User(username, email, password) VALUES (?, ?, ?)";

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
        String query = "SELECT username FROM User WHERE id = ?";

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
        String query = "SELECT id FROM User WHERE username = ?";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                }
            }
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
        return id;
    }

    public static int[] getFriendsIds(int userId) {
        String firstQuery = "SELECT friendship_id, second_user_id FROM Friendship WHERE first_user_id = ?";
        Map<Integer, Integer> friends = new TreeMap<>(getFriendsMap(userId, firstQuery));

        String secondQuery = "SELECT friendship_id, first_user_id FROM Friendship WHERE second_user_id = ?";
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

    public static void insertNewFriendship(int firstUserId, int secondUserId) {
        String query = "INSERT INTO Friendship(first_user_id, second_user_id) VALUES (?, ?)";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, firstUserId);
            preparedStatement.setInt(2, secondUserId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }
}