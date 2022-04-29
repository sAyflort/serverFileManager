module com.example.filemanagerclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.all;
    requires io.netty.transport;

    opens ru.geekbrains.serverFileManager to javafx.fxml;
    exports ru.geekbrains.serverFileManager;
}