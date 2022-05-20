package ru.geekbrains.serverFileManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.commons.Commands;
import ru.commons.FileInfo;
import ru.geekbrains.serverFileManager.netty.NettyClient;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

import static ru.commons.Commands.*;


public class Controller implements Initializable {
    @FXML
    private VBox leftTable, rightTable;
    @FXML
    private VBox authGUI;
    @FXML
    private VBox fmGUI;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private TextArea msgArea;

    private static PanelController lastClickedTable;
    private static Controller controller;
    private RegController regCtrl;
    private String log;
    private String pass;
    private final String basePath = "C:\\Users\\sAyflort\\IdeaProjects\\serverFileManager";
    private boolean authenticated;

    private ClientPanelCtrl leftPController;
    private ServerPanelCtrl rightPController;
    private NettyClient nettyClient;

    private static final Logger LOGGER = LogManager.getLogger(Controller.class);

    public void auth(ActionEvent actionEvent) {
        setAcc(loginField.getText(), passField.getText());
        sendMsg(AUTH, null, null);
    }

    public void setAcc(String log, String pass) {
        this.log = log;
        this.pass = pass;
    }

    public void reg(ActionEvent actionEvent) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(FileManagerClient.class.getResource("/regGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 350);
        Stage stage = new Stage();
        stage.setTitle("Reg");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        regCtrl = fxmlLoader.getController();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        leftTable.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.5));
        rightTable.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.5));
        leftPController = (ClientPanelCtrl) leftTable.getProperties().get("ctrl");
        rightPController = (ServerPanelCtrl) rightTable.getProperties().get("ctrl");
        leftPController.updateTable(Paths.get(basePath));
        nettyClient = new NettyClient();
        controller = this;
        rightPController.setPrimeCtrl(this);
    }

    public void move(ActionEvent actionEvent) {
        if (lastClickedTable.getSelectedItem() == null) {
            return;
        } else {
            if(lastClickedTable.getSelectedItem().getType() == FileInfo.FileType.DIRECTORY ) {
                return;
            }
            if (lastClickedTable == leftPController) {
                nettyClient.sendFile(lastClickedTable.getSelectedItem(), log, pass, SEND_FILE);
            }
            if (lastClickedTable == rightPController) {
                nettyClient.sendFile(lastClickedTable.getSelectedItem(), log, pass, GET_FILE);
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

    public ServerPanelCtrl getRightPController() {
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
        try {
            if (!confDelete(lastClickedTable.getSelectedItem().getFilePath())) {
                return;
            }
            if (lastClickedTable == leftPController) {
                try {
                    Files.deleteIfExists(Paths.get(lastClickedTable.getSelectedItem().getFilePath()));
                    lastClickedTable.updateTable(Paths.get(lastClickedTable.getCurrentPath()));
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else if (lastClickedTable == rightPController) {
                sendMsg(DELETE_FILE, lastClickedTable.getSelectedItem().getFilePath(), null);
            }
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Не выбран файл");
            alert.setHeaderText("Для удаления нажмите лкм по нужному файлу");
            alert.setContentText(null);
            alert.showAndWait();
        }

    }

    private boolean confDelete(String path) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete file"+(lastClickedTable instanceof ClientPanelCtrl ? " in client": " in cloud"));
        alert.setHeaderText("Удалить "+path.split("\\\\")[path.split("\\\\").length-1]+"?");
        alert.setContentText(path);
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            return true;
        } else if (option.get() == ButtonType.CANCEL) {
            return false;
        } else {
            return false;
        }
    }

    public void setFocusPass(ActionEvent actionEvent) {
        passField.requestFocus();
    }

    public void createDirectory(ActionEvent actionEvent) {
        try {
            lastClickedTable.createDirectory();
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Не распознана таблица");
            alert.setHeaderText("Для создания папки нажмите лкм по нужной таблице");
            alert.setContentText(null);
            alert.showAndWait();
        }

    }

    public void appendAuthText(String msg) {
        msgArea.appendText(msg);
    }
    public void appendRegText(String msg) {
        regCtrl.appendRegText(msg);
    }
}