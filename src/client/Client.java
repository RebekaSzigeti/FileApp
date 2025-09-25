package client;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to server!");
            String message;
            System.out.println("Enter LOGIN/REGISTER username password OR UPLOAD OR DOWNLOAD filename OR DELETE filename");

            while(!(message = userInput.readLine()).isEmpty()) {
                out.println(message);
                String[] parts = message.split(" ");
                if(parts[0].equals("UPLOAD")){
                    System.out.println("Enter the path");
                    String path = userInput.readLine();
                    File file = new File(path);
                    long fileSize = file.length();
                    String fileName = file.getName();

                    out.println(fileName);
                    out.println(fileSize);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int read;
                        OutputStream socketOut = socket.getOutputStream();

                        while ((read = fis.read(buffer)) > 0) {
                            socketOut.write(buffer, 0, read);
                        }
                        socketOut.flush();
                    }

                }else if(parts[0].equals("DOWNLOAD")){
                    String response = in.readLine();
                    if("OK".equals(response)){
                        String fileName = parts[1];
                        long fileSize = Long.parseLong(in.readLine());

                        File saveDir = new File("client_downloads/");
                        if (!saveDir.exists()) saveDir.mkdir();
                        File outputFile = new File(saveDir, fileName);

                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            InputStream socketIn = socket.getInputStream();
                            byte[] buffer = new byte[4096];
                            long remaining = fileSize;

                            while (remaining > 0) {
                                int read = socketIn.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                                if (read == -1) break;
                                fos.write(buffer, 0, read);
                                remaining -= read;
                            }
                            fos.flush();
                            System.out.println("File downloaded: " + outputFile.getAbsolutePath());
                        }
                    }else{
                        System.out.println("Server response: " + response);
                    }
                }else if(parts[0].equals("DELETE")){
                    String response = in.readLine();
                    System.out.println(response);
                }
                System.out.println("Enter LOGIN/REGISTER username password OR UPLOAD OR DOWNLOAD filename OR DELETE filename");
                }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
