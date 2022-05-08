package ru.geekbrains.cloud.dataBaseService;

import java.sql.*;

public class BaseAuthService implements AuthService{
    private Connection connection;
    private PreparedStatement prSelectAuth;

    public BaseAuthService() {
        try {
            connect();
            prepareAllStatements();
        } catch (SQLException throwables) {
        }
    }

    private void prepareAllStatements() throws SQLException {
        prSelectAuth = connection.prepareStatement("SELECT id FROM main.accounts where login=? and password=?");
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:identifier.sqlite");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    private void disconnect() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        disconnect();
    }

    @Override
    public boolean isAuth(String login, String pass) {
        int key;
        try {
            prSelectAuth.setString(1, login);
            prSelectAuth.setString(2, pass);
            ResultSet rs = prSelectAuth.executeQuery();
            key = rs.getInt(1);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean reg(String login, String pass) {
        return false;
    }
}

