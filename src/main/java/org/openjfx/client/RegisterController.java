package org.openjfx.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjfx.App;

import java.io.IOException;


public class RegisterController {
    private static final Logger logger = LogManager.getLogger("client");

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField repasswordField;

    @FXML
    private TextField nickField;

    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    void saveChanges() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String repassword = repasswordField.getText().trim();
        String nick = nickField.getText().trim();


        if (login.length() == 0 || password.length() == 0 || repassword.length() == 0 || nick.length() == 0) {
            registerErrAlert("Ошибка регистрации", "Поля не должны быть пустыми!");
            return;
        }

        if (!password.equals(repassword)) {
            registerErrAlert("Ошибка регистрации", "Пароли не совпадают!");
            return;
        }

        try {
            network.getOut().writeUTF(Network.CMD_PREF_REGISTER + " " + login + " " + password + " " + nick);
            network.setCurrentLogin(login);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    @FXML
    void cancelChanges() {
        Platform.runLater(() -> network.getAppChat().getRegisterStage().close());
    }

    public void registerErrAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
