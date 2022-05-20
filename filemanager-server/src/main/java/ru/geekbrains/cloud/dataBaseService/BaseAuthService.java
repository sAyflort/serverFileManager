package ru.geekbrains.cloud.dataBaseService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class BaseAuthService implements AuthService{
    private Connection connection;
    private PreparedStatement prSelectAuth;
    private PreparedStatement prAddAcc;

    private static final Logger LOGGER = LogManager.getLogger(BaseAuthService.class);

    public BaseAuthService() {
        try {
            connect();
            prepareAllStatements();
            LOGGER.info("БД подключена");
        } catch (SQLException throwables) {
            LOGGER.warn(throwables);
        }
    }

    private void prepareAllStatements() throws SQLException {
        prSelectAuth = connection.prepareStatement("SELECT id FROM main.accounts where login=? and password=?");
        prAddAcc = connection.prepareStatement("INSERT INTO main.accounts (login, password) VALUES (?, ?)");
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:identifier.sqlite");
        } catch (SQLException | ClassNotFoundException throwables) {
            LOGGER.warn(throwables);
        }
    }

    private void disconnect() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                LOGGER.warn(throwables);
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
            LOGGER.info("Аутентификация пройдена");
            return true;
        } catch (SQLException e) {
            LOGGER.info("Аутентификация не пройдена");
            return false;
        }
    }

    @Override
    public boolean reg(String login, String pass) {
        try {
            prAddAcc.setString(1, login);
            prAddAcc.setString(2, pass);
            prAddAcc.execute();
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return false;
        }
        return true;
    }
}

