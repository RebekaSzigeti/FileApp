package database;

public class TestDatabase {
    public static void main(String[] args) {
        DatabaseManager.createDatabase();

        boolean reg = DatabaseManager.registerUser("alice", "password123",55);
        System.out.println("Registered Alice: " + reg);

        boolean login = DatabaseManager.loginUser("alice", "password123");
        System.out.println("Login Alice: " + login);
    }
}
