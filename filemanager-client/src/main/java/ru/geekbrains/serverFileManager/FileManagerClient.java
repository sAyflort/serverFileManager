package ru.geekbrains.serverFileManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FileManagerClient extends Application {
    private static final Logger LOGGER = LogManager.getLogger(FileManagerClient.class);
    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("Запуск приложения");
        FXMLLoader fxmlLoader = new FXMLLoader(FileManagerClient.class.getResource("/fmGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("File manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}