package models;

public class User {
    private final String username;
    private int shareKey = 0;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }
    public int getShareKey(){return shareKey;}
    public void setShareKey(int key){
        shareKey = key;
    }
}
