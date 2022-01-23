package com.geekbrains.cloud.client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientController implements Initializable {

    public ListView<String> clientListView;
    public ListView<String> serverListView;

    public TextField textField;
    public TextField serverTextField;

    private File currentDir;
    private NetworkHandler net;

    public void sendFile(ActionEvent actionEvent) throws IOException {
        String fileName = textField.getText();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();

        try (FileInputStream is = new FileInputStream(currentFile)) {
            byte[] bytes = new byte[(int) currentFile.length()];
            int read = is.read(bytes);
            if (read > 0) {
                FileWrapping fw = new FileWrapping(fileName, bytes);
                net.sendFile(fw);
            }
        }
        refreshServerList();
        textField.clear();
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
        currentDir = new File(System.getProperty("user.home"));
        net = new NetworkHandler(args -> {
            Object msg = args[1];
            if (msg instanceof FileWrapping) {
                FileWrapping fw = (FileWrapping) msg;
                try (FileOutputStream fos = new FileOutputStream(currentDir.toPath().resolve(fw.getName()).toFile())) {
                    fos.write(fw.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(this::fillCurrentDirFiles);
                log.info("FileWrapping {}", msg);
            } else if (msg instanceof List) {
                log.info("FilesList {}", msg);
                Platform.runLater(() -> serverListView.getItems().addAll((List) msg));
            }
        });
        fillCurrentDirFiles();
        initClickListener();
        refreshServerList();
    }

    public void refreshServerList() {
        serverListView.getItems().clear();
        net.sendMessage("#GET#USER#FILES#");
    }

    public void getFile(ActionEvent actionEvent) {
        try {
            log.info("Requesting file...");
            String fileName = serverListView.getSelectionModel().getSelectedItem();
            net.sendMessage("#GET#FILE#" + fileName);
            fillCurrentDirFiles();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
