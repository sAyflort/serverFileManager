package ru.geekbrains.cloud.dataBaseService;

public interface AuthService {
    void start();
    boolean isAuth(String login, String pass);
    public boolean reg(String login, String pass);
    void stop();
}
