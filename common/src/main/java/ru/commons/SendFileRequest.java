package ru.commons;

public class SendFileRequest implements Request {
    private String login;
    private String pass;
    private String directPath;
    private FileInfo fileInfo;
    private Commands type;

    public SendFileRequest(Commands type, String login, String pass, String directPath, FileInfo fileInfo) {
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

    public String getDirectPath() {
        return directPath;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public Commands getType() {
        return type;
    }
}
