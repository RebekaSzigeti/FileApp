# FileApp

A simple JavaFX-based file management application that allows users to upload, download, delete and share files. 

Features:
- User authentication
- Upload and download files
- Delete files
- Share files between users with share keys
- Sort files by name or date
- Filter file view: your files, shared files, or all files
- Change password and manage share key
- Help menu with guidance on share keys and password management
- Files metadata stored in SQLite database

Requirements:
- JavaFX
- a JSON library (e.g., `org.json`)
- a SQLite JDBC library

Setup Instructions:
Add the required JAR files to your project structure. Ensure the database is initialized (TestDatabase). 
Start the server by running the Server class in the server package, then run the Main class from the main package (main/com.example.server/Main.java).
By default, the server runs on localhost for testing. To run across multiple computers, start the server on one machine and have clients connect using the server machine’s IP address.


