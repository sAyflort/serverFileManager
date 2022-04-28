module com.example.filemanagerclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.filemanagerclient to javafx.fxml;
    exports com.example.filemanagerclient;
}