package gui;

import client.ClientConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.User;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button LoginButton;

    @FXML
    private TextField UsernameTextfield;

    @FXML
    private PasswordField passwordField;

    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private ClientConnection clientConnection;


    @FXML
    private void login() throws IOException {
        String username = UsernameTextfield.getText();
        String password = passwordField.getText();
        if(username.isEmpty()){
            UserController.Alert("Please enter your username");
        }else if(password.isEmpty()){
            UserController.Alert("Please enter your password");
        }else {

            boolean answer = clientConnection.check_credentials(username,password);
            if (!answer) {
                UserController.Alert("Invalid username or password");
            } else {
                User user = new User(username);
                Stage loginStage = (Stage) LoginButton.getScene().getWindow();
                loginStage.close();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("user.fxml"));
                    Stage stage = new Stage();
                    Parent root = loader.load();
                    UserController userController = loader.getController();
                    userController.setUser(user);
                    userController.setConnection(clientConnection);
                    userController.setFiles();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("user.css").toExternalForm());
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/folder.png")));
                    stage.setResizable(false);
                    stage.setScene(scene);
                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void register(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Stage stage = new Stage();
            Parent root = loader.load();
            RegisterController registerController = loader.getController();
            registerController.setClientConnection(clientConnection);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            clientConnection = new ClientConnection(HOST,PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
