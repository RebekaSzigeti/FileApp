package gui;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                //System.out.println("Client connected!");
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
