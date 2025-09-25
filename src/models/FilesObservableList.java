package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FilesObservableList {
    private static FilesObservableList instance;

    private final ObservableList<FileRecord> files = FXCollections.observableArrayList();

    public static FilesObservableList getInstance(){
        if(instance == null){
            instance = new FilesObservableList();
        }
        return instance;
    }

    public ObservableList<FileRecord> getFiles() {
        return files;
    }
    public void addFile(FileRecord t) {
        files.add(t);
    }
    public void removeFile(FileRecord t) {
        files.remove(t);
    }
}
