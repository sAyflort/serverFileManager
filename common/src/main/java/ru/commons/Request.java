package ru.commons;

import java.io.Serializable;
import java.nio.file.Path;

public class Request implements Serializable {
    private String login;
    private String pass;
    private Path directPath;
    private FileInfo fileInfo;
    private Commands type;

    public Request(Commands type, String login, String pass, Path directPath, FileInfo fileInfo) {
        this.login = login;
        this.pass = pass;
        this.directPath = directPath;
        this.fileInfo = fileInfo;
        this.type = type;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public Path getDirectPath() {
        return directPath;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public Commands getType() {
        return type;
    }
}
