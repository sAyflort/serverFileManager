package ru.geekbrains.serverFileManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ru.geekbrains.serverFileManager.netty.NettyClient;

import java.net.URL;
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

    private static Controller controller;
    private static PanelController lastClickedTable;
    private String log;
    private String pass;
    private boolean authenticated;

    private PanelController leftPController, rightPController;
    private NettyClient nettyClient;

    public void auth(ActionEvent actionEvent) {
        log = loginField.getText();
        pass = passField.getText();
        nettyClient.sendAuth(log, pass);
    }

    public void reg(ActionEvent actionEvent) throws Exception {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        leftTable.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.5));
        rightTable.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.5));
        leftPController = (PanelController) leftTable.getProperties().get("ctrl");
        rightPController = (PanelController) rightTable.getProperties().get("ctrl");
        leftPController.updateTable(Paths.get("."));
        nettyClient = new NettyClient();
        controller = this;
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

    public static Controller getInstance() {
        return controller;
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
}