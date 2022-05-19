package ru.geekbrains.serverFileManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.commons.FileInfo;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.commons.Commands.*;

public class ServerPanelCtrl implements PanelController {
    @FXML
    private TableView<FileInfo> table;
    @FXML
    private ComboBox disksBox;
    @FXML
    private TextField pathField;
    @FXML
    private TextField searchField;
    @FXML
    private Button upButton;

    private FileInfo selectedItem;
    private Controller primeCtrl;
    private TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");

    private static final Logger LOGGER = LogManager.getLogger(ServerPanelCtrl.class);

    public void setPrimeCtrl(Controller primeCtrl) {
        this.primeCtrl = primeCtrl;
    }

    public void search(ActionEvent actionEvent) {
        upButton.setText(RETURN);
        fileNameColumn.setText("Путь");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilePath()));
        primeCtrl.sendMsg(SEARCH_FILE, searchField.getText(), null);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Тип");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.1));

        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.60));

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long aLong, boolean b) {
                    super.updateItem(aLong, b);
                    if(aLong == null || b) {
                        setText("");
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", aLong);
                        if(aLong == -1L) {
                            text = "";
                        }
                        setText(text);
                    }
                }
            };
        });

        table.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn);
        table.getSortOrder().add(fileTypeColumn);

        disksBox.getItems().clear();
        for (Path p: FileSystems.getDefault().getRootDirectories()
        ) {
            disksBox.getItems().add(p);
        }
        disksBox.getSelectionModel().select(0);

        table.setOnMouseClicked(mouseEvent -> {
            selectedItem = table.getSelectionModel().getSelectedItem();
            if(mouseEvent.getClickCount() >= 1) {
                Controller.setLastClickedTable(ServerPanelCtrl.this);
            }
            if(mouseEvent.getClickCount() == 2) {
                Path path = Paths.get(pathField.getText()).resolve(selectedItem.getFileName());
                if(Files.isDirectory(path)) {
                    primeCtrl.sendMsg(GET_FILE_LIST,
                            path.toString(), null);
                }

            }
        });
    }

    @Override
    public void updateTable(List<File> fileList, String path) {
        if (path != null) {
            pathField.setText(path);
        }
        table.getItems().clear();
        table.getItems().addAll(fileList.stream().map(FileInfo::new).collect(Collectors.toList()));
        table.sort();
    }

    @Override
    public void upperPath(ActionEvent actionEvent) {
        if(upButton.getText().equals(RETURN)) {
            upButton.setText(UP);
            fileNameColumn.setText("Имя");
            fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
            primeCtrl.sendMsg(GET_FILE_LIST, Paths.get(pathField.getText()).toString(), null);
        } else {
            primeCtrl.sendMsg(GET_FILE_LIST, Paths.get(pathField.getText()).getParent().toString(), null);
        }

    }

    @Override
    public String getCurrentPath() {
        return pathField.getText();
    }

    @Override
    public FileInfo getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void createDirectory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New folder");
        dialog.setHeaderText(null);
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            primeCtrl.sendMsg(CREATE_DIRECTORY, getCurrentPath()+"\\"+name, null);
        });
    }
}
