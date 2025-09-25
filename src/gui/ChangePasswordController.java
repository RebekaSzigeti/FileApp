package gui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    private UserController userController;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmField;

    public void setUserController(UserController controller){
        userController = controller;
    }

    @FXML
    private void save(){
        String password = passwordField.getText();
        String confirmPassword = confirmField.getText();
        if(password.isEmpty() || confirmPassword.isEmpty()){
            UserController.Alert("The textfields must not be empty");
            return;
        }
        if(!password.equals(confirmPassword)){
            UserController.Alert("The passwords do not match");
            return;
        }

        int length = password.length();
        if(length < 4 || length > 32){
            UserController.Alert("The length of the password must be between 4 and 32.");
            return;
        }
        userController.setNewPassword(password);
        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();
    }
}
