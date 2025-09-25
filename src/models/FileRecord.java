package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileRecord {
    private final String filename;
    private final LocalDateTime uploadTime;
    private final String ownerName;
    private final String size;
    private final String formattedTime;
    private final String permissions;
    private final int sharePermission, deletePermission;



    public FileRecord(String ownerName, String filename, LocalDateTime uploadTime, String size) {
        this.ownerName = ownerName;
        this.filename = filename;
        this.uploadTime = uploadTime;
        this.size = size;
        this.formattedTime = getUploadTimeFormatted();
        sharePermission=1;
        deletePermission=1;
        this.permissions = "download, delete, share";
    }

    public FileRecord(String ownerName,String filename, LocalDateTime uploadTime, String size,int deletePermission, int sharePermission) {
        this.ownerName = ownerName;
        this.filename = filename;
        this.uploadTime = uploadTime;
        this.size = size;
        this.formattedTime = getUploadTimeFormatted();
        String p = "download";
        if(deletePermission == 1){
            p+=", delete";
        }
        if(sharePermission == 1){
            p+=", share";
        }
        this.sharePermission=sharePermission;
        this.deletePermission=deletePermission;
        this.permissions = p;
    }


    public String getUploadTimeFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return uploadTime.format(formatter);
    }

    public String getFilename() { return filename; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public String getOwnerName() {return ownerName; }
    public String getSize(){return size;}
    public String getFormattedTime() {
        return formattedTime;
    }
    public String getPermissions(){return permissions;}
    public int getSharePermission(){
        return sharePermission;
    }
    public int getDeletePermission(){
        return deletePermission;
    }

    public boolean isEqual(FileRecord f){
        return filename.equals(f.getFilename()) && ownerName.equals(f.getOwnerName());
    }

}
