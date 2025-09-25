package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ChangeSKController {

    @FXML
    Button saveButton;

    @FXML
    TextField textField;

    private UserController userController;

    @FXML
    private void save(){
        String text = textField.getText();
        if(text.isEmpty()){
            UserController.Alert("You left the text field empty.");
            return;
        }

        if(!isNumeric(text)){
            UserController.Alert("Your share key must only contain digits.");
            return;
        }

        if(text.length()>3 || text.equals("0")){
            UserController.Alert("The chosen number must be between 1 and 999");
            return;
        }
        int number = Integer.parseInt(text);
        userController.setNewShareKey(number);
        Stage stage = (Stage) textField.getScene().getWindow();
        stage.close();

    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public void setUserController(UserController controller){
        userController = controller;
    }

}
