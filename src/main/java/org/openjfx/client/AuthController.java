package org.openjfx.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjfx.App;

import java.io.IOException;

public class AuthController {
    private static final Logger logger = LogManager.getLogger("client");
    @FXML
    public PasswordField passwordField;
    @FXML
    private ChoiceBox<String> choicePersonAccount;

    @FXML
    private Button authButton;

    private Network network;

    private ObservableList<String> loginList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        choicePersonAccount.setItems(loginList);
        choicePersonAccount.setOnAction(actionEvent -> {
            prepareControls();
        });
    }

    @FXML
    public void checkAuth() {
        String login = choicePersonAccount.getValue().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            authErrAlert("Ошибка авторизации", "Поля не должны быть пустыми!");
            return;
        }

        try {
            network.getOut().writeUTF(Network.CMD_PREF_AUTH + " " + login + " " + password);
            network.setCurrentLogin(login);
            passwordField.clear();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    @FXML
    void register() {
        Platform.runLater(() -> network.getAppChat().getRegisterStage().show());
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void addLoginList(String login) {
        loginList.add(login);
    }

    public void clearLoginList() {
        loginList.removeAll();
    }

    public void prepareControls() {
        if (network.isAuthorized()) {
            choicePersonAccount.setDisable(true);
            passwordField.setDisable(true);
            authButton.setDisable(true);
        } else {
            choicePersonAccount.setDisable(false);
            passwordField.setDisable(false);
            authButton.setDisable(false);
        }
    }

    public void authErrAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
