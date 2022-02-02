package com.geekbrains.cloud.client;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import com.geekbrains.cloud.message.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientController implements Initializable {

    @FXML
    public ListView<String> clientListView;
    @FXML
    public ListView<String> serverListView;
    @FXML
    public Label serverInfo;
    @FXML
    public Label clientInfo;

    private File currentDir;
    private NetworkHandler net;
    private String currentServerItem;

    @FXML
    public void sendFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientListView.getSelectionModel().getSelectedItem();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();

        try (FileInputStream is = new FileInputStream(currentFile)) {
            byte[] bytes = new byte[(int) currentFile.length()];
            int read = is.read(bytes);
            if (read > 0) {
                FileWrapping fw = new FileWrapping(fileName, bytes);
                net.sendMessage(fw);
            }
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
                    clientInfo.setText("Имя папки:" + fileName);
                } else {
                    clientInfo.setText("Имя файла:" + fileName + ", размер: " + path.toFile().length() + " байт");
                }
            }
        });

        serverListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                try {
                    log.info("Requesting file info...");
                    currentServerItem = serverListView.getSelectionModel().getSelectedItem();
                    net.sendMessage(new FileOperation(currentServerItem));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (e.getClickCount() == 1) {
                currentServerItem = serverListView.getSelectionModel().getSelectedItem();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDir = new File(System.getProperty("user.home"));
        net = NetworkHandler.getInstanceAndSetHandlerCallback(args -> {
            AbstractMessage msg = (AbstractMessage) args[1];
            switch (msg.getType()) {
                case FILE_MESSAGE:
                    FileWrapping fw = (FileWrapping) msg;
                    try (FileOutputStream fos = new FileOutputStream(currentDir.toPath().resolve(fw.getName()).toFile())) {
                        fos.write(fw.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(this::fillCurrentDirFiles);
                    log.info("FileWrapping {}", msg);
                    break;
                case FILE_LIST:
                    log.info("FilesList {}", msg);
                    Platform.runLater(() -> {
                        serverListView.getItems().clear();
                        serverListView.getItems().addAll(((FileList) msg).getList());
                    });
                    break;
                case FILE_INFO:
                    FileOperation fi = (FileOperation) msg;
                    log.info("FileInfo {}", msg);
                    Platform.runLater(() -> serverInfo.setText("Имя файла:" + fi.getFilename() + ", размер: " + fi.getSize() + " байт"));
                    break;
            }
        });
        fillCurrentDirFiles();
        initClickListener();
        net.sendMessage(new ReadyMessage());
    }

    @FXML
    public void getFile(ActionEvent actionEvent) {
        try {
            log.info("Requesting file...");
            String fileName = serverListView.getSelectionModel().getSelectedItem();
            net.sendMessage(new FileOperation(fileName, MessageType.FILE_REQUEST));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void removeObject(ActionEvent actionEvent) {
        if (currentServerItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete File");
            alert.setHeaderText("Are you sure want to remove this file?");
            alert.setContentText(currentServerItem);

            Optional<ButtonType> option = alert.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {
                net.sendMessage(new FileOperation(currentServerItem, MessageType.DELETE));
            }
        }
    }

    @FXML
    public void createFolder(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Create a new folder");
        dialog.setHeaderText("Creating a new folder");
        dialog.setContentText("Please new folder name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> net.sendMessage(new FileOperation(name, MessageType.NEW_FOLDER)));
    }

    @FXML
    public void navigateBack(ActionEvent actionEvent) {
        net.sendMessage(new FileOperation(".."));
    }

    @FXML
    public void renameObject(ActionEvent actionEvent) {
        if (currentServerItem != null) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Rename file or folder");
            dialog.setHeaderText("Renaming a file or folder");
            dialog.setContentText("Please enter new file name:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(name -> net.sendMessage(new FileOperation(currentServerItem, name)));
        }
    }
}
