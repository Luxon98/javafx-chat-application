package chatserver;

import java.sql.*;


class DatabaseQueries {

    public static void insertNewInvitation(int invitingUserId, int invitedUserId) {
        String query = "INSERT INTO Invitation(inviting_user_id, invited_user_id) VALUES (?, ?)";

        Connection connection = DatabaseConnectionPoolManager.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, invitingUserId);
            preparedStatement.setInt(2, invitedUserId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        DatabaseConnectionPoolManager.getInstance().releaseConnection(connection);
    }
}
