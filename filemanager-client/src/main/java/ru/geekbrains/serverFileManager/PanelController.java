package ru.geekbrains.serverFileManager;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import ru.commons.FileInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface PanelController extends Initializable {
    String RETURN = "Вернуться";
    String UP = "Вверх";

    void setPrimeCtrl(Controller primeCtrl);
    default void updateTable(Path path) {
    }
    default void updateTable(List<File> fileList, String path) {
    }
    default void search(ActionEvent actionEvent) {
    }
    default void updateTable(List<Path> paths) {
    }
    default void upperPath(ActionEvent actionEvent) {
    }
    default void createDirectory(){

    }
    default String getCurrentPath() {
        return null;
    }
    FileInfo getSelectedItem();
}
