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
    private String log;
    private String pass;

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
        if (leftPController.getSelectedItem() == null) return;
        nettyClient.sendFile(leftPController.getSelectedItem(), log, pass);
    }

    public void setAuthenticated() {
        authGUI.setVisible(false);
        authGUI.setManaged(false);
        fmGUI.setVisible(true);
        fmGUI.setManaged(true);
    }

    public static Controller getInstance() {
        return controller;
    }
}