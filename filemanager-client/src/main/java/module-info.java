module com.example.filemanagerclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.all;
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.buffer;

    opens ru.geekbrains.serverFileManager to javafx.fxml;
    exports ru.geekbrains.serverFileManager;
    exports ru.geekbrains.serverFileManager.netty;
    opens ru.geekbrains.serverFileManager.netty to javafx.fxml;
}