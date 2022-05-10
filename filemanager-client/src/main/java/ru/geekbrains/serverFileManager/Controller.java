package ru.geekbrains.serverFileManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ru.commons.Commands;
import ru.commons.FileInfo;
import ru.geekbrains.serverFileManager.netty.NettyClient;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static ru.commons.Commands.*;


public class Controller implements Initializable {
    @FXML
    VBox leftTable, rightTable;
    @FXML
    private VBox authGUI;
    @FXML
    private VBox fmGUI;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;

    private static PanelController lastClickedTable;
    private static Controller controller;
    private String log;
    private String pass;
    private boolean authenticated;

    private PanelController leftPController, rightPController;
    private NettyClient nettyClient;

    public void auth(ActionEvent actionEvent) {
        log = loginField.getText();
        pass = passField.getText();
        sendMsg(AUTH, null, null);
    }

    public void reg(ActionEvent actionEvent) throws Exception {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        leftTable.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.5));
        rightTable.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.5));
        leftPController = (PanelController) leftTable.getProperties().get("ctrl");
        rightPController = (PanelController) rightTable.getProperties().get("ctrl");
        leftPController.updateTable(Paths.get("C:\\Users\\sAyflort\\IdeaProjects\\serverFileManager\\local"));
        nettyClient = new NettyClient();
        controller = this;
        rightPController.setPrimeCtrl(this);
        rightPController.setTypeServerPanel();
    }



    public void move(ActionEvent actionEvent) {
        if (lastClickedTable.getSelectedItem() == null) {
            return;
        } else {
            if (lastClickedTable == leftPController) {
                nettyClient.sendFile(lastClickedTable.getSelectedItem(), log, pass, SEND_FILE);
            }
            if (lastClickedTable == rightPController) {
                nettyClient.sendFile(lastClickedTable.getSelectedItem(), log, pass, GET_FILE);
                System.out.println("Отправка в NettyClient");
            }
        }


    }

    public void setAuthenticated() {
        authenticated = !authenticated;
        authGUI.setVisible(!authenticated);
        authGUI.setManaged(!authenticated);
        fmGUI.setVisible(authenticated);
        fmGUI.setManaged(authenticated);
    }

    public PanelController getRightPController() {
        return rightPController;
    }

    public void exit(ActionEvent actionEvent) {
        setAuthenticated();
    }

    public static void setLastClickedTable(PanelController lastClickedTable) {
        Controller.lastClickedTable = lastClickedTable;
    }

    public String getCurrentPath() {
        return leftPController.getCurrentPath();
    }

    public void updateLeftTable() {
        leftPController.updateTable(Paths.get(leftPController.getCurrentPath()));
    }

    public void sendMsg(Commands command, String path, FileInfo msg) {
        nettyClient.sendMsg(command, log, pass, path, msg);
    }

    public static Controller getInstance() {
        return controller;
    }

    public void delete(ActionEvent actionEvent) {
        if (lastClickedTable == leftPController) {
            try {
                Files.deleteIfExists(Paths.get(lastClickedTable.getSelectedItem().getFilePath()));
                lastClickedTable.updateTable(Paths.get(lastClickedTable.getCurrentPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (lastClickedTable == rightPController) {
            sendMsg(DELETE_FILE, lastClickedTable.getSelectedItem().getFilePath(), null);
        }
    }
}