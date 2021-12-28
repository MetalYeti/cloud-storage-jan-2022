package com.geekbrains.cloud.client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ClientController implements Initializable {

    public ListView<String> clientListView;
    public ListView<String> serverListView;

    public TextField textField;
    public TextField serverTextField;

    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;

    private byte[] buf;

    public void sendFile(ActionEvent actionEvent) throws IOException {
        String fileName = textField.getText();
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
        refreshServerList();
        os.flush();
        textField.clear();
    }

    private void read() {
        try {
            while (true) {
                String message = is.readUTF();

                if (message.startsWith("#")) {
                    if (message.startsWith("#START#LIST")) {
                        List<String> files = new ArrayList<>();
                        String fileName;
                        while (!(fileName = is.readUTF()).equals("#END#LIST#")) {
                            files.add(fileName);
                        }
                        Platform.runLater(() -> serverListView.getItems().addAll(files));
                    } else if (message.startsWith("#GET#FILE#")) {
                        String fileName = is.readUTF();
                        long size = is.readLong();
                        int bufSize = is.readInt();
                        System.out.println("Created file: " + fileName);
                        System.out.println("File size: " + size);
                        System.out.println("Buffer size: " + bufSize);
                        Path currentPath = currentDir.toPath().resolve(fileName);
                        try (FileOutputStream fos = new FileOutputStream(currentPath.toFile())) {
                            for (int i = 0; i < (size + bufSize - 1) / bufSize; i++) {
                                int read = is.read(buf);
                                fos.write(buf, 0, read);
                            }
                        }
                        os.writeUTF("File successfully uploaded");
                        os.flush();
                        Platform.runLater(this::fillCurrentDirFiles);
                    }
                } else {
                    Platform.runLater(() -> textField.setText(message));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // reconnect to server
        }
    }

    private void fillCurrentDirFiles() {
        clientListView.getItems().clear();
        clientListView.getItems().add("..");
        clientListView.getItems().addAll(currentDir.list());
    }

    private void initClickListener() {
        clientListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = clientListView.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                    fillCurrentDirFiles();
                    textField.clear();
                } else {
                    textField.setText(fileName);
                }
            }
        });

        serverListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = serverListView.getSelectionModel().getSelectedItem();
                serverTextField.setText(fileName);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buf = new byte[256];
            currentDir = new File(System.getProperty("user.home"));
            fillCurrentDirFiles();
            initClickListener();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
            refreshServerList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshServerList() {
        serverListView.getItems().clear();
        try {
            os.writeUTF("#GET#USER#FILES#");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFile(ActionEvent actionEvent) {
        try {
            String fileName = serverListView.getSelectionModel().getSelectedItem();
            os.writeUTF("#GET#FILE#");
            os.writeUTF(fileName);
            os.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
