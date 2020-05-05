package chatapplication.client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


class DatabaseConnectionPoolManager {
    private final static String DATABASE_URL = "jdbc:mysql://remotemysql.com:3306/LpjSGEW1V2";
    private final static String DATABASE_USERNAME = "LpjSGEW1V2";
    private final static String DATABASE_PASSWORD = "tPLNxeKbt5";
    private final static String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    private final static int INITIAL_POOL_SIZE = 4;
    private final static int MAXIMAL_POOL_SIZE = 10;

    private List<Connection> connectionPool;
    private List<Connection> usedConnections = new ArrayList<>();

    private static DatabaseConnectionPoolManager instance = null;

    public static DatabaseConnectionPoolManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionPoolManager();
        }
        return instance;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
    }

    public DatabaseConnectionPoolManager() {
        try {
            Class.forName(DRIVER_NAME);
            connectionPool = new ArrayList<>(INITIAL_POOL_SIZE);
            for (int i = 0; i < INITIAL_POOL_SIZE; ++i) {
                connectionPool.add(createConnection());
            }
        }
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        if (connectionPool.isEmpty()) {
            if (usedConnections.size() < MAXIMAL_POOL_SIZE) {
                try {
                    connectionPool.add(createConnection());
                }
                catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
            else {
                throw new RuntimeException("Allowable number of database connections exceeded.");
            }
        }

        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    public void releaseConnection(Connection connection) {
        usedConnections.remove(connection);
        connectionPool.add(connection);
    }

    private void closeConnections() {
        usedConnections.forEach(this::releaseConnection);
        for (Connection c : connectionPool) {
            try {
                c.close();
            }
            catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
        connectionPool.clear();
    }
}
