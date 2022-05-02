package ru.geekbrains.serverFileManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.layout.VBox;

import javafx.stage.Stage;
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

    public Stage regStage;
    private RegController regAppController;
    private PanelController leftPController, rightPController;
    private NettyClient nettyClient;

    public void auth(ActionEvent actionEvent) {
        authGUI.setVisible(false);
        authGUI.setManaged(false);
        fmGUI.setVisible(true);
        fmGUI.setManaged(true);

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
    }



    public void move(ActionEvent actionEvent) {
        if (leftPController.getSelectedItem() == null) return;
        nettyClient.sendMessage(leftPController.getSelectedItem());
    }
}