package gui;

import models.FileRecord;

import java.util.ArrayList;

public class FileArrayList {
    private static FileArrayList instance;
    private final ArrayList<FileRecord> files = new ArrayList<>();

    public static FileArrayList getInstance(){
        if(instance==null){
            instance = new FileArrayList();
        }
        return instance;
    }

    public ArrayList<FileRecord> getFiles(){
        return files;
    }

    public void addFile(FileRecord f){
        files.add(f);
    }

}
