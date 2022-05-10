package ru.geekbrains.serverFileManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import ru.commons.FileInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.commons.Commands.GET_FILE_LIST;
import static ru.geekbrains.serverFileManager.PanelController.TYPE.*;


// 1) Нужно создать отдельный класс PanelController, от которого будут наследоваться ServerPanelController и ClientPanelController,
//так как выходит слишком много логики на данный класс, который использует только половину методов в зависимости от того, где он расположен в интерфейсе
// 2) Создать логику поиска файла в текущей директории и внутренних директориях:
//-При нажатии на кнопку "поиск" заблокировать переходы по директориям
//-Чтобы вернуться в режим "проводника" нужно будет нажать на кнопку "Вверх", которая в режиме поиска будет менять текст на "Вернуться"

public class PanelController implements Initializable {
    @FXML
    private TableView<FileInfo> table;
    @FXML
    private ComboBox disksBox;
    @FXML
    private TextField pathField;
    @FXML
    private TextField searchField;

    private FileInfo selectedItem;
    private TYPE type = CLIENT;
    private Controller primeCtrl;

    public void setPrimeCtrl(Controller primeCtrl) {
        this.primeCtrl = primeCtrl;
    }

    public void search(ActionEvent actionEvent) {

    }

    enum TYPE {
        SERVER,
        CLIENT
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Тип");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.1));

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
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

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                selectedItem = table.getSelectionModel().getSelectedItem();
                if(mouseEvent.getClickCount() >= 1) {
                    Controller.setLastClickedTable(PanelController.this);
                }
                if(mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(selectedItem.getFileName());
                    if(Files.isDirectory(path)) {
                        updateTable(path);
                    }
                }
            }
        });
    }

    public void updateTable(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            table.getItems().clear();
            table.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            table.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Нет доступа или не удалось получить файлы из данного пути", ButtonType.OK);
            alert.showAndWait();
        }

    }

    public void updateTable(List<File> fileList, String path) {
        pathField.setText(path);
        table.getItems().clear();
        table.getItems().addAll(fileList.stream().map(FileInfo::new).collect(Collectors.toList()));
        table.sort();

    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateTable(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void upperPath(ActionEvent actionEvent) {
        if (type == CLIENT) {
            Path upPath = Paths.get(pathField.getText()).getParent();
            if (upPath != null) {
                updateTable(upPath);
            }
        } else {
            primeCtrl.sendMsg(GET_FILE_LIST, Paths.get(pathField.getText()).getParent().toString(), null);
        }

    }
    public String getCurrentPath() {
        return pathField.getText();
    }

    public FileInfo getSelectedItem() {
        return selectedItem.getType() == FileInfo.FileType.FILE ? selectedItem : null;
    }

    public void setTypeServerPanel() {
        type = SERVER;
        table.setOnMouseClicked(mouseEvent -> {
            selectedItem = table.getSelectionModel().getSelectedItem();
            if(mouseEvent.getClickCount() >= 1) {
                Controller.setLastClickedTable(PanelController.this);
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

}
