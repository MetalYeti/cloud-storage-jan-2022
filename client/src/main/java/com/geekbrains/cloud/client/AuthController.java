package com.geekbrains.cloud.client;

import com.geekbrains.cloud.message.AbstractMessage;
import com.geekbrains.cloud.message.AuthRequest;
import com.geekbrains.cloud.message.AuthResponse;
import com.geekbrains.cloud.message.MessageType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class AuthController implements Initializable {

    Stage root;
    @FXML
    public TextField userName;
    @FXML
    public PasswordField password;
    @FXML
    public Button login;
    @FXML
    public Label message;

    private NetworkHandler net;

    public AuthController(Stage root) {
        this.root = root;
    }

    public void setNet(NetworkHandler net) {
        this.net = net;
    }

    @FXML
    public void login(ActionEvent actionEvent) {
        net.sendMessage(new AuthRequest(userName.getText(), password.getText()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = NetworkHandler.getInstanceAndSetHandlerCallback((args) -> {
            AbstractMessage msg = (AbstractMessage) args[1];
            if (msg.getType().equals(MessageType.AUTH_RESPONSE)) {
                if (((AuthResponse) msg).isAuthenticated()) {
                    Platform.runLater(() -> {
                        message.setText("");
                        try {
                            Parent parent = FXMLLoader.load(getClass().getResource("client.fxml"));
                            root.setScene(new Scene(parent));
                            root.show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                } else {
                    Platform.runLater(() -> {
                        message.setText("");
                        message.setTextFill(Color.RED);
                        message.setText("Authentication failed " + ((AuthResponse) msg).getResponse());
                    });
                }
            }
        });

    }
}
