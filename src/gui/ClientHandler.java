package gui;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import database.DatabaseManager;
import models.FileRecord;


public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String currentUser;
    private int currentUserId;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

            while (true) {
                String command = dis.readUTF();

                switch (command) {
                    case "REGISTER": {
                        String username = dis.readUTF();
                        String password = dis.readUTF();
                        Random rand = new Random();
                        int shareKey = rand.nextInt(1, 1000);
                        if (DatabaseManager.registerUser(username, password, shareKey)) {
                            System.out.println("User has registered successfully!");
                            dos.writeUTF("OK");
                        } else {
                            dos.writeUTF("Something went wrong with the registering");
                        }
                        break;
                    }

                    case "LOGIN": {
                        String username = dis.readUTF();
                        currentUserId = DatabaseManager.getUserId(username);
                        currentUser = username;
                        break;
                    }

                    case "CHECK_CREDENTIALS": {
                        String username = dis.readUTF();
                        String password = dis.readUTF();
                        boolean answer = DatabaseManager.getUserIdIfValid(username,password);
                        dos.writeBoolean(answer);
                        break;
                    }

                    case "UPLOAD": {
                        String fileName = dis.readUTF();
                        long fileSize = dis.readLong();

                        File uploadsDir = new File("serverFiles/" + currentUser + "/");
                        if (!uploadsDir.exists()){
                            uploadsDir.mkdirs();
                        }
                        File file = new File(uploadsDir, fileName);

                        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            byte[] buffer = new byte[8192];
                            long remaining = fileSize;
                            int read;
                            int bufferLength = buffer.length;
                            while (remaining > 0) {
                                read = dis.read(buffer, 0, (int) Math.min(bufferLength, remaining));
                                if (read == -1) {
                                    throw new IOException("Error - end of stream");
                                }
                                fileOutputStream.write(buffer, 0, read);
                                remaining -= read;
                            }
                            fileOutputStream.flush();
                        }

                        String serverPath = "serverFiles\\" + uploadsDir.getName() + "\\" + fileName;
                        String size = humanReadableFileSize(fileSize);
                        boolean answer = DatabaseManager.addFile(fileName, currentUserId, serverPath, size);
                        if (!answer) {
                            dos.writeUTF("ERROR");
                            break;
                        } else {
                            dos.writeUTF("OK");
                        }
                        FileRecord filerecord = DatabaseManager.getFileForUserByName(currentUserId, fileName, currentUser);
                        JSONObject obj = new JSONObject();
                        obj.put("ownerName", filerecord.getOwnerName());
                        obj.put("fileName", filerecord.getFilename());
                        obj.put("uploadTime", filerecord.getUploadTime().toString());
                        obj.put("size", filerecord.getSize());
                        byte[] jsonbytes = obj.toString().getBytes(StandardCharsets.UTF_8);
                        dos.writeInt(jsonbytes.length);
                        dos.write(jsonbytes);
                        dos.flush();
                        break;
                }


                    case "DOWNLOAD": {
                        String fileName = dis.readUTF();
                        String ownerName = dis.readUTF();
                        int id = DatabaseManager.getUserId(ownerName);
                        String path = DatabaseManager.getFilePathByName(fileName, id);
                        File file = new File(path);
                        if (!file.exists()) {
                            dos.writeUTF("ERROR: File missing on server.");
                            return;
                        }

                        long fSize = file.length();
                        dos.writeUTF("OK");
                        dos.writeLong(fSize);

                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) > 0) {
                                dos.write(buffer, 0, bytesRead);
                            }
                        }
                        break;
                }

                    case "DELETE": {
                        String fileName = dis.readUTF();
                        String ownerName = dis.readUTF();
                        String filepath = DatabaseManager.deleteFile(fileName, ownerName);
                        if (filepath == null) {
                            dos.writeUTF("File not found");
                            break;
                        }

                        File fileToDelete = new File(filepath);
                        if (fileToDelete.exists() && fileToDelete.delete()) {
                            dos.writeUTF("OK");
                        } else {
                            dos.writeUTF("Delete - something went wrong");
                        }
                        break;
                }

                    case "GET_FILES": {
                        JSONObject root = new JSONObject();
                        JSONArray arr = new JSONArray();
                        List<FileRecord> files = DatabaseManager.getFilesForUser(currentUserId);
                        for (FileRecord fileRecord : files) {
                            JSONObject fileObj = new JSONObject();
                            fileObj.put("ownerName", fileRecord.getOwnerName());
                            fileObj.put("fileName", fileRecord.getFilename());
                            fileObj.put("uploadTime", fileRecord.getUploadTime().toString());
                            fileObj.put("size", fileRecord.getSize());
                            fileObj.put("sharePermission", fileRecord.getSharePermission());
                            fileObj.put("deletePermission", fileRecord.getDeletePermission());
                            arr.put(fileObj);
                        }
                        root.put("files", arr);
                        byte[] jsonBytes = root.toString().getBytes(StandardCharsets.UTF_8);
                        dos.writeInt(jsonBytes.length);
                        dos.write(jsonBytes);
                        dos.flush();
                        break;
                    }

                    case "GET_SHARE_KEY": {
                        int shareKey = DatabaseManager.getShareKeyByUserId(currentUserId);
                        dos.writeInt(shareKey);
                        break;
                    }

                    case "SHARE": {
                        String fileName = dis.readUTF();
                        String username = dis.readUTF();
                        String ownerName = dis.readUTF();
                        int userID = DatabaseManager.getUserId(username);
                        int ownerId = DatabaseManager.getUserId(ownerName);
                        boolean canShare = dis.readBoolean();
                        boolean canDelete = dis.readBoolean();
                        int fileId = DatabaseManager.getFileIdByNameAndOwner(fileName, ownerId);
                        if (fileId == -1) {
                            dos.writeUTF("Something went wrong");
                        } else if(DatabaseManager.checkIfAlreadyShared(fileId,userID)){
                            dos.writeUTF("OK");
                        } else if(DatabaseManager.checkIfOwnsIt(fileId,userID)){
                            dos.writeUTF("OK");
                        } else if (DatabaseManager.addPermission(fileId, userID, canDelete, canShare)) {
                            dos.writeUTF("OK");
                        } else {
                            dos.writeUTF("Something went wrong");
                        }
                        break;
                    }

                    case "CHANGE_SK": {
                        int shareKey = dis.readInt();
                        if (DatabaseManager.updateShareKey(currentUserId, shareKey)) {
                            dos.writeUTF("OK");
                        } else {
                            dos.writeUTF("Something went wrong");
                        }
                        break;
                    }

                    case "VALID_USERNAME_AND_KEY": {
                        String username = dis.readUTF();
                        int shareKey = dis.readInt();
                        if (DatabaseManager.isValidShareKey(username, shareKey)) {
                            dos.writeUTF("VALID");
                        } else {
                            dos.writeUTF("INVALID");
                        }
                        break;
                    }

                    case "CHANGE_PASSWORD":{
                        String newPassword = dis.readUTF();
                        dos.writeBoolean(DatabaseManager.updatePassword(currentUserId,newPassword));
                        break;
                    }
                    case "CHECK_IF_USERNAME_EXISTS": {
                        String username = dis.readUTF();
                        dos.writeBoolean(DatabaseManager.usernameExists(username));
                        break;
                    }

                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }


        } catch(java.net.SocketException e) {
        //System.out.println("Client disconnected: " + e.getMessage());
    } catch(IOException e){
        e.printStackTrace();
    } finally {
        try {
            clientSocket.close();
        } catch (IOException ignored) {
        }
    }
}

    public static String humanReadableFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

}
