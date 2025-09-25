module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;

    opens gui to javafx.fxml;
    opens models to javafx.base;
    exports gui;
}