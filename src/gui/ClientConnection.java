package gui;
import models.FileRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ClientConnection {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private UserController userController;

    public ClientConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        connect();
    }

    private void connect() throws IOException {
        socket = new Socket(host, port);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    public void setFilesInTableView() throws IOException {
        dos.writeUTF("GET_FILES");
        FilesObservableList observableList = FilesObservableList.getInstance();
        FileArrayList files = FileArrayList.getInstance();

        int length = dis.readInt();
        byte[] jsonBytes = new byte[length];
        dis.readFully(jsonBytes);
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject(jsonString);
        JSONArray arr = obj.getJSONArray("files");
        length = arr.length();

        for(int i=0;i<length;++i){
            String ownerName = arr.getJSONObject(i).getString("ownerName");
            String fileName = arr.getJSONObject(i).getString("fileName");
            String d = arr.getJSONObject(i).getString("uploadTime");
            LocalDateTime uploadTime = LocalDateTime.parse(d);
            String size = arr.getJSONObject(i).getString("size");
            int sharePermission = arr.getJSONObject(i).getInt("sharePermission");
            int deletePermission = arr.getJSONObject(i).getInt("deletePermission");
            files.addFile(new FileRecord(ownerName,fileName,uploadTime,size,deletePermission,sharePermission));

        observableList.getFiles().setAll(files.getFiles());
        }
    }

    public void loginUser(String username) throws IOException {
        dos.writeUTF("LOGIN");
        dos.writeUTF(username);
    }

    public void downloadFile(String fileName, String owner, String whereToDownload) throws IOException {
        dos.writeUTF("DOWNLOAD");
        dos.writeUTF(fileName);
        dos.writeUTF(owner);
        String answer = dis.readUTF();
        if(answer.equals("OK")){
            long fileSize = dis.readLong();
            File outputFile = new File(whereToDownload);

            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                long remaining = fileSize;
                byte[] buffer = new byte[8192];
                int bytesRead;
                int bufferLength = buffer.length;
                while (remaining > 0) {
                    bytesRead = dis.read(buffer, 0, (int)Math.min(bufferLength, remaining));
                    if (bytesRead == -1) break;
                    fileOutputStream.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                fileOutputStream.flush();
            }
        }else{
            UserController.Alert(answer);
        }
    }

    public void uploadFile(String path, String fileName) throws IOException {
        //path -> the location of the chosen file on the users computer
        dos.writeUTF("UPLOAD");
        dos.writeUTF(fileName);

        File file = new File(path);
        long fileSize = file.length();

        dos.writeLong(fileSize);

        String answer;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }
            dos.flush();

            answer = dis.readUTF();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        if(!answer.equals("OK")){
            UserController.Alert("File could not get stored in the database");
        }else{
            int length = dis.readInt();
            byte[] jsonBytes = new byte[length];
            dis.readFully(jsonBytes);
            String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(jsonString);
            String ownerName = obj.getString("ownerName");
            fileName = obj.getString("fileName");
            String d = obj.getString("uploadTime");
            LocalDateTime uploadTime = LocalDateTime.parse(d);
            String size = obj.getString("size");
            FileRecord f = new FileRecord(ownerName,fileName,uploadTime,size);
           userController.setAddedFileRecord(f);

        }

    }

    public void deleteFile(String name, String ownerName) throws IOException {
        dos.writeUTF("DELETE");
        dos.writeUTF(name);
        dos.writeUTF(ownerName);
        String answer = dis.readUTF();
        if(!answer.equals("OK")){
            UserController.Alert(answer);
        }
    }


    public int getShareKey() throws IOException {
        dos.writeUTF("GET_SHARE_KEY");
        return dis.readInt();
    }

    public void registerUser(String username, String password) throws IOException {
        dos.writeUTF("REGISTER");
        dos.writeUTF(username);
        dos.writeUTF(password);
        String answer = dis.readUTF();
        if(!answer.equals("OK")){
            UserController.Alert(answer);
        }
    }


    public void shareFile(String fileName, String username, String ownerName, boolean canDelete, boolean canShare) throws IOException {
        dos.writeUTF("SHARE");
        dos.writeUTF(fileName);
        dos.writeUTF(username);
        dos.writeUTF(ownerName);
        dos.writeBoolean(canShare);
        dos.writeBoolean(canDelete);
        String answer = dis.readUTF();
        if(!answer.equals("OK")){
            UserController.Alert(answer);
        }
    }



    public void changeShareKey(int newShareKey) throws IOException {
        dos.writeUTF("CHANGE_SK");
        dos.writeInt(newShareKey);
        String answer = dis.readUTF();
        if(!answer.equals("OK")){
            UserController.Alert(answer);
        }
    }

    public void setUserController(UserController userController){
        this.userController = userController;
    }

    public boolean check_credentials(String username, String password) throws IOException {
        dos.writeUTF("CHECK_CREDENTIALS");
        dos.writeUTF(username);
        dos.writeUTF(password);
        return dis.readBoolean();
    }

    public boolean checkForValidUsernameAndKey(String username, int key) throws IOException {
        dos.writeUTF("VALID_USERNAME_AND_KEY");
        dos.writeUTF(username);
        dos.writeInt(key);
        String answer = dis.readUTF();
        return answer.equals("VALID");
    }

    public boolean changePassword(String newPassword) throws IOException {
        dos.writeUTF("CHANGE_PASSWORD");
        dos.writeUTF(newPassword);
        return dis.readBoolean();
    }

    public boolean checkIfUsernameExists(String username) throws IOException {
        dos.writeUTF("CHECK_IF_USERNAME_EXISTS");
        dos.writeUTF(username);
        return dis.readBoolean();
    }

}
