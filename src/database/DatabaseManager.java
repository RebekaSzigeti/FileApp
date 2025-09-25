package database;
import models.FileRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:files.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createDatabase() {
        try (Connection connection = getConnection()) {
            Statement stmt = connection.createStatement();

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS Users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                share_key INTEGER
            )
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS Files (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                filename TEXT NOT NULL,
                owner_id INTEGER NOT NULL,
                path TEXT NOT NULL,
                upload_time TEXT DEFAULT CURRENT_TIMESTAMP,
                file_size TEXT NOT NULL,
                FOREIGN KEY(owner_id) REFERENCES Users(id)
            )
        """);

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS Permissions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                file_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                can_delete INTEGER,
                can_share INTEGER,
                FOREIGN KEY(file_id) REFERENCES Files(id),
                FOREIGN KEY(user_id) REFERENCES Users(id)
    
            
            )
        """);


            stmt.execute("""
            CREATE TABLE IF NOT EXISTS History (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                file_id INTEGER NOT NULL,
                action TEXT,
                timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES Users(id),
                FOREIGN KEY(file_id) REFERENCES Files(id)
            )
        """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String username, String passwordHash, int share_key) {
        String sql = "INSERT INTO Users(username, password_hash, share_key) VALUES(?, ?, ?)";
        try (Connection conn = getConnection();PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setInt(3,share_key);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public static boolean loginUser(String username, String passwordHash) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getUserId(String username) {
        String sql = "SELECT id FROM Users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean addFile(String filename, int ownerId, String path, String size) {
        String sql = "INSERT INTO Files(filename, owner_id, path, file_size) VALUES(?, ?, ?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filename);
            pstmt.setInt(2, ownerId);
            pstmt.setString(3, path);
            pstmt.setString(4,size);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    public static String getFilePathByName(String fileName, int userId) {
        String sql = "SELECT path FROM files WHERE filename = ? AND owner_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("path");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String deleteFile(String fileName, String ownerName) {
        int userId = getUserId(ownerName);
        String sql = "SELECT path FROM files WHERE filename = ? AND owner_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String filePath = rs.getString("path");
                String deleteSql = "DELETE FROM files WHERE filename = ? AND owner_id = ?";
                try (PreparedStatement pstmt2 = conn.prepareStatement(deleteSql)) {
                    pstmt2.setString(1, fileName);
                    pstmt2.setInt(2, userId);
                    pstmt2.executeUpdate();
                }
                return filePath;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<FileRecord> getFilesForUser(int userId) {
        List<FileRecord> files = new ArrayList<>();
        String sql = "SELECT Users.username, Files.filename, Files.path, Files.upload_time,Files.file_size FROM Files JOIN Users ON Files.owner_id = Users.id WHERE Files.owner_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while (rs.next()) {
                LocalDateTime uploadTime = LocalDateTime.parse(rs.getString("upload_time"), formatter);
                FileRecord file = new FileRecord(
                        rs.getString("username"),
                        rs.getString("filename"),
                        uploadTime,
                        rs.getString("file_size"),
                        1,
                        1
                );
                files.add(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql2 = "SELECT f.id, f.filename, f.owner_id AS oi,f.path, f.upload_time, f.file_size,p.can_share, p.can_delete " +
                "FROM Files f " +
                "JOIN Permissions p ON f.id = p.file_id " +
                "WHERE p.user_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while (rs.next()) {
                LocalDateTime uploadTime = LocalDateTime.parse(rs.getString("upload_time"), formatter);
                int ownerId = rs.getInt("oi");
                String username = getUsernameById(ownerId);
                FileRecord file = new FileRecord(
                        username,
                        rs.getString("filename"),
                        uploadTime,
                        rs.getString("file_size"),
                        rs.getInt("can_delete"),
                        rs.getInt("can_share")
                );
                files.add(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return files;
    }

    public static boolean getUserIdIfValid(String username, String password) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static FileRecord getFileForUserByName(int userId, String filename, String username) {
        String sql = "SELECT * FROM files WHERE owner_id = ? AND filename = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, filename);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDateTime uploadTime = LocalDateTime.parse(rs.getString("upload_time"), formatter);
                return new FileRecord(
                        username,
                        rs.getString("filename"),
                       uploadTime,
                        rs.getString("file_size")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUsernameById(int userId) {
        String sql = "SELECT username FROM Users WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getShareKeyByUserId(int userId) {
        String sql = "SELECT share_key FROM Users WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("share_key");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isValidShareKey(String username, int shareKey) {
       int key = getShareKeyByUserId(getUserId(username));
       return key == shareKey;
    }

    public static boolean addPermission(int fileId, int userId, boolean canDelete, boolean canShare) {
        String sql = "INSERT INTO Permissions(file_id, user_id, can_delete, can_share) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fileId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, canDelete ? 1 : 0);
            pstmt.setInt(4, canShare ? 1 : 0);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static int getFileIdByNameAndOwner(String filename, int ownerId) {
        String sql = "SELECT id FROM Files WHERE filename = ? AND owner_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filename);
            pstmt.setInt(2, ownerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean updateShareKey(int id, int newShareKey) {
        String sql = "UPDATE Users SET share_key = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newShareKey);
            pstmt.setInt(2, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET password_hash = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkIfAlreadyShared(int fileID, int userId){
        String sql = "SELECT COUNT(*) FROM Permissions WHERE user_id = ? AND file_id = ?";
        try(Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1,userId);
            pstmt.setInt(2,fileID);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1)>0;
            }

        }catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public static boolean checkIfOwnsIt(int fileId, int userId){
        String sql = "SELECT COUNT(*) FROM Files WHERE id = ? AND owner_id = ?";
        try(Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1,fileId);
            pstmt.setInt(2,userId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1)>0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}

