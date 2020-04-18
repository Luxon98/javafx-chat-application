package chatclient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class SqlQueries {

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
}