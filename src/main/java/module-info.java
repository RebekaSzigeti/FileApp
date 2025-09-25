module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;

    opens gui to javafx.fxml;
    exports gui;
    exports server;
    opens server to javafx.fxml;
    exports client;
    opens client to javafx.fxml;
    exports models;
    opens models to javafx.base, javafx.fxml;
    exports com.example.server;
    opens com.example.server to javafx.fxml;
}