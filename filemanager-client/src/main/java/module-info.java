module filemanager.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.all;
    requires org.apache.logging.log4j;
    requires common;

    opens ru.geekbrains.serverFileManager to javafx.fxml;
    exports ru.geekbrains.serverFileManager;
}