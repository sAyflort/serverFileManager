package ru.geekbrains.serverFileManager;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.commons.Commands.REG;

public class RegController implements Initializable {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField pass1Field;
    @FXML
    private PasswordField pass2Field;
    @FXML
    private TextArea msgArea;

    private Controller primeCtrl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        primeCtrl =  Controller.getInstance();
    }

    public void setFocusPass1(ActionEvent actionEvent) {
        pass1Field.requestFocus();
    }

    public void setFocusPass2(ActionEvent actionEvent) {
        pass2Field.requestFocus();
    }

    public void reg(ActionEvent actionEvent) {
        if(pass1Field.getText().equals(pass2Field.getText())) {
            primeCtrl.setAcc(loginField.getText(), pass1Field.getText());
            primeCtrl.sendMsg(REG, null, null);
        }
    }

    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) loginField.getScene().getWindow();
        stage.close();
    }

    public void appendRegText(String msg) {
        msgArea.appendText(msg);
    }
}