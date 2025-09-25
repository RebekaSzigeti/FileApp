package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PopupShareKeyController {
    @FXML
    private Label showShareKeyLabel;

    public void setText(String text){
        showShareKeyLabel.setText(text);
    }
}
