package gui;

import client.ClientConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.FileArrayList;
import models.FileRecord;
import models.FilesObservableList;
import models.User;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserController {
    @FXML
    private TableView<FileRecord> tableView;

    private ObservableList<FileRecord> fileObservableList;

    @FXML
    private TableColumn<FileRecord, String> filenameCol;

    @FXML
    private TableColumn<FileRecord, String> uploadTimeCol;

    @FXML
    private TableColumn<FileRecord, Integer> ownerCol;

    @FXML
    private TableColumn<FileRecord, String> permissionCol;


    @FXML
    private TableColumn<FileRecord, Long> sizeCol;

    @FXML
    private ToggleGroup group1;

    private User user;

    private ClientConnection clientConnection;

    private String userToShareFileWith = "";
    private boolean canShare, canDelete;

    private FileRecord addedFileRecord;

    private int key=0;
    private String newPassword = "";


    public void initialize() throws IOException {

        FilesObservableList filesObservableList = FilesObservableList.getInstance();
        fileObservableList = filesObservableList.getFiles();
        filenameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));
        uploadTimeCol.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        permissionCol.setCellValueFactory(new PropertyValueFactory<>("permissions"));
        tableView.setItems(fileObservableList);

    }

    @FXML
    private void download() throws IOException {
        FileRecord selectedFile = tableView.getSelectionModel().getSelectedItem();

        if (selectedFile == null) {
           Alert("Please select a file");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(selectedFile.getFilename());
        File saveLocation = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if(saveLocation!=null) {
            clientConnection.downloadFile(selectedFile.getFilename(),selectedFile.getOwnerName(),saveLocation.getAbsolutePath());
        }

    }

    @FXML
    private void upload() throws IOException {
        FileChooser fileChooser = new FileChooser();
        File uploadLocation = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        if(uploadLocation!=null) {
            String whereIsTheFile = uploadLocation.getAbsolutePath();
            String fileName = uploadLocation.getName();
            RadioMenuItem selectedRadioMenuItem = (RadioMenuItem) group1.getSelectedToggle();
            String value = selectedRadioMenuItem.getText();
            clientConnection.setUserController(this);
            clientConnection.uploadFile(whereIsTheFile,fileName);

            if(value.equals("Show all files") || value.equals("Show my files")) {
                FilesObservableList filesObservableList = FilesObservableList.getInstance();
                filesObservableList.addFile(addedFileRecord);
            }

            FileArrayList fileArrayList = FileArrayList.getInstance();
           fileArrayList.addFile(addedFileRecord);
        }
    }

    @FXML
    private  void delete() throws IOException {
        FileRecord selectedFile = tableView.getSelectionModel().getSelectedItem();

        if (selectedFile == null) {
            Alert("Please select a file");
            return;
        }

        if(selectedFile.getDeletePermission()==0){
            Alert("You have no right to delete this file");
            return;
        }

        FileArrayList fileArrayList = FileArrayList.getInstance();
        ArrayList<FileRecord> arrayList = fileArrayList.getFiles();
        FileRecord f = null;
        for(FileRecord file : arrayList){
            if(file.isEqual(selectedFile)){
                f=file;
                break;
            }
        }
        int i = arrayList.indexOf(f);
        arrayList.remove(arrayList.get(i));

        clientConnection.deleteFile(selectedFile.getFilename(), selectedFile.getOwnerName());
        FilesObservableList filesObservableList = FilesObservableList.getInstance();
        filesObservableList.removeFile(selectedFile);

    }

    @FXML
    private void share(){
        FileRecord selectedFile = tableView.getSelectionModel().getSelectedItem();

        if (selectedFile == null) {
            Alert("Please select a file");
            return;
        }

        if(selectedFile.getSharePermission()==0){
            Alert("You have no right to share this file");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("share.fxml"));
            Stage stage = new Stage();
            Parent root = loader.load();
            ShareController shareController = loader.getController();
            shareController.setClientConnection(clientConnection);
            shareController.setUserController(this);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/folder.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
            if(!userToShareFileWith.isEmpty()){
                String ownerName = selectedFile.getOwnerName();
                if(!ownerName.equals(userToShareFileWith)){
                    clientConnection.shareFile(selectedFile.getFilename(),userToShareFileWith,ownerName,canDelete,canShare);
                }
                userToShareFileWith= "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void getShareKey() throws IOException {
        int shareKey;
        if(user.getShareKey()==0) {
            shareKey = clientConnection.getShareKey();
            user.setShareKey(shareKey);
        }else{
            shareKey= user.getShareKey();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("popupShareKey.fxml"));
        Stage stage = new Stage();
        Parent root = loader.load();
        Scene scene = new Scene(root);
        PopupShareKeyController popupShareKeyController = loader.getController();
        popupShareKeyController.setText("Your share key is: " + shareKey);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/folder.png")));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();

    }

    @FXML
    private void showMyFiles(){
        FilesObservableList filesObservableList = FilesObservableList.getInstance();
        ObservableList<FileRecord> files = filesObservableList.getFiles();
        files.setAll(FileArrayList.getInstance().getFiles());
        String username = user.getUsername();
        files.removeIf(f-> !f.getOwnerName().equals(username));
    }

    @FXML
    private void showAllFiles(){
        FilesObservableList filesObservableList = FilesObservableList.getInstance();
        filesObservableList.getFiles().setAll(FileArrayList.getInstance().getFiles());
    }

    @FXML
    private void showSharedFiles(){
        FilesObservableList filesObservableList = FilesObservableList.getInstance();
        ObservableList<FileRecord> files = filesObservableList.getFiles();
        files.setAll(FileArrayList.getInstance().getFiles());
        String username = user.getUsername();
        files.removeIf(f-> f.getOwnerName().equals(username));
    }

    public static void Alert(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFiles() throws IOException {
        clientConnection.loginUser(user.getUsername());
        clientConnection.setFilesInTableView();
    }

    public void setConnection(ClientConnection connection) {
        clientConnection = connection;
    }

    public void setUserToShareFileWith(String name){
        userToShareFileWith=name;
    }

    public void setRights(boolean canDelete, boolean canShare){
        this.canDelete = canDelete;
        this.canShare = canShare;
    }

    public void setAddedFileRecord(FileRecord f){
        addedFileRecord = f;
    }

    @FXML
    private void changeShareKey(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("changeShareKey.fxml"));
            Stage stage = new Stage();
            Parent root = loader.load();
            ChangeSKController changeSKController = loader.getController();
            changeSKController.setUserController(this);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/folder.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
            if(key!=0){
                clientConnection.changeShareKey(key);
                user.setShareKey(key);
                key=0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNewShareKey(int key){
        this.key = key;
    }

    @FXML
    private void sortByUploadTime(){
        fileObservableList.sort((FileRecord f1, FileRecord f2) -> {
            LocalDateTime date1 = f1.getUploadTime();
            LocalDateTime date2 = f2.getUploadTime();
            return date1.compareTo(date2);
        });
    }

    @FXML
    private void sortByFileName(){
        fileObservableList.sort((FileRecord f1, FileRecord f2) -> {
            String name1 = f1.getFilename().toLowerCase();
            String name2 = f2.getFilename().toLowerCase();
            return name1.compareTo(name2);
        });
    }

    @FXML
    private void changePassword(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("changePassword.fxml"));
            Stage stage = new Stage();
            Parent root = loader.load();
            ChangePasswordController changePasswordController = loader.getController();
            changePasswordController.setUserController(this);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/image/folder.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
            if(!newPassword.isEmpty()){
                if(!clientConnection.changePassword(newPassword)) {
                    Alert("Something went wrong - The password was not modified");
                }
                newPassword = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNewPassword(String newPassword){
        this.newPassword = newPassword;
    }

}
