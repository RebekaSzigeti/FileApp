package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField UsernameTextField;

   @FXML
   private PasswordField passwordField;

    @FXML
    private Button registerButton;

    private ClientConnection clientConnection;


    @FXML
    private void registerUser() throws IOException {
        String username = UsernameTextField.getText();
        String password = passwordField.getText();
        String title = "Something went wrong";
        if(username.isEmpty()){
            UserController.Alert("Please enter your username");
            return;
        }
        int length = username.length();
        if(length <4 || length > 20){
            UserController.Alert("The length of your username must be between 4 and 20");
            return;
        }
        if(clientConnection.checkIfUsernameExists(username)){
            UserController.Alert("The provided username already exists");
            return;
        }
        if(password.isEmpty()){
            UserController.Alert("Please enter your password");
            return;
        }
        length = password.length();
        if(length <4 || length > 32){
            UserController.Alert("The length of your password must be between 4 and 32");
            return;
        }
        clientConnection.registerUser(username,password);
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();

    }

    public void setClientConnection(ClientConnection connection){
        clientConnection = connection;
    }

}
