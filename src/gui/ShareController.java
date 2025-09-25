package gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ShareController {
    @FXML
    TextField usernameTextField;

    @FXML
    TextField shareKeyTextField;

    @FXML
    CheckBox shareCheckBox;

    @FXML
    CheckBox deleteCheckBox;


    private ClientConnection clientConnection;

    private UserController userController;

    @FXML
    private void share() throws IOException {
        String username = usernameTextField.getText();
        if(username.isEmpty()){
            UserController.Alert("Please file the username field");
        }else {
            String shareKey = shareKeyTextField.getText();
            if (shareKey.isEmpty()) {
                UserController.Alert("Please fill the share key field");
            }else if (!isNumeric(shareKey)) {
                UserController.Alert("The share key must contain only digits");
            }else{

                if(clientConnection.checkForValidUsernameAndKey(username,Integer.parseInt(shareKey))){
                    userController.setUserToShareFileWith(username);
                    boolean canShare = shareCheckBox.isSelected();
                    boolean canDelete = deleteCheckBox.isSelected();
                    userController.setRights(canDelete,canShare);
                    Stage shareStage = (Stage) shareKeyTextField.getScene().getWindow();
                    shareStage.close();

                }else{
                    UserController.Alert("Invalid username or share key");
                }

            }
        }

    }

    public void setClientConnection(ClientConnection connection){
        clientConnection = connection;
    }

    public boolean isNumeric(String str) {
        return str.chars().allMatch(Character::isDigit);
    }

    public void setUserController(UserController controller){
        userController = controller;
    }

}
