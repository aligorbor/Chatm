package org.openjfx.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class AuthController {

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

    public void checkAuth() {
        String login = choicePersonAccount.getValue().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            System.out.println("!!Поля не должны быть пустыми");
            return;
        }

        try {
            network.getOut().writeUTF(Network.CMD_PREF_AUTH + " " + login + " " + password);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("!!Ошибка аутентификации");
        }

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
}
