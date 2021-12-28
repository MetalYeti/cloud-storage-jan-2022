package com.geekbrains.cloud.server;

import javafx.event.ActionEvent;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

public class FileProcessorHandler implements Runnable {

    private File currentDir;
    private static final int SIZE = 256;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;

    public FileProcessorHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[SIZE];
        currentDir = new File("serverDir");
    }

    public void sendFile(String fileName) throws IOException {
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        os.writeUTF("#SEND#FILE#");
        os.writeUTF(fileName);
        os.writeLong(currentFile.length());
        try (FileInputStream is = new FileInputStream(currentFile)) {
            while (true) {
                int read = is.read(buf);
                if (read == -1) {
                    break;
                }
                os.write(buf, 0, read);
            }
        }
        os.flush();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Received: " + command);
                if (command.equals("#SEND#FILE#")) {
                    String fileName = is.readUTF();
                    long size = is.readLong();
                    System.out.println("Created file: " + fileName);
                    System.out.println("File size: " + size);
                    Path currentPath = currentDir.toPath().resolve(fileName);
                    try (FileOutputStream fos = new FileOutputStream(currentPath.toFile())) {
                        for (int i = 0; i < (size + SIZE - 1) / SIZE; i++) {
                            int read = is.read(buf);
                            fos.write(buf, 0, read);
                        }
                    }
                    os.writeUTF("File successfully downloaded");
                    os.flush();
                } else if (command.equals("#GET#USER#FILES#")) {
                    String[] currentDirFiles = currentDir.toPath().toFile().list();
                    if (currentDirFiles != null) {
                        int filesCount = currentDirFiles.length;
                        os.writeUTF("#START#LIST#OF#" + filesCount);
                        for (String item : currentDirFiles) {
                            os.writeUTF(item);
                        }
                        os.writeUTF("#END#LIST#");
                    }
                }else if (command.equals("#GET#FILE#")) {
                    String fileName = is.readUTF();
                    System.out.println("Requested file: " + fileName);
                    File currentFile = currentDir.toPath().resolve(fileName).toFile();
                    os.writeUTF("#GET#FILE#");
                    os.writeUTF(fileName);
                    os.writeLong(currentFile.length());
                    os.writeInt(SIZE);
                    try (FileInputStream fos = new FileInputStream(currentFile)) {
                        while (true) {
                            int read = fos.read(buf);
                            if (read == -1) {
                                break;
                            }
                            os.write(buf, 0, read);
                        }
                    }
                    os.writeUTF("File successfully uploaded");
                    os.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
