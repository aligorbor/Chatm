package org.openjfx.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Controller {


    @FXML
    private TextField inputField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> listView;

    @FXML
    private ListView<String> listPersons;

    @FXML
    private Label labelPersonChat;


    @FXML
    private TextField textLoginNick;

    @FXML
    private Button btnChangeNick;


    private MultipleSelectionModel<String> personsSelectionModel;

    private ObservableList<String> personList = FXCollections.observableArrayList
            ("Общий чат");
    private final ArrayList<ObservableList<String>> arrChat = new ArrayList<>();

    private Network network;
    private String loginNick;

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    void initialize() {
        personsSelectionModel = listPersons.getSelectionModel();
        personsSelectionModel.selectedItemProperty().addListener((changed, oldValue, newValue) -> {
            labelPersonChat.setText(newValue);
            int index = personsSelectionModel.getSelectedIndex();
            listView.setItems(arrChat.get(index));
        });

        listPersons.setItems(personList);
        while (arrChat.size() < personList.size())
            arrChat.add(FXCollections.observableArrayList());

        personsSelectionModel.select(0);
    }

    @FXML
    void sendMessage() {
        // String timeStamp = DateFormat.getInstance().format(new Date());
        String message = inputField.getText().trim();
        if (!message.isBlank()) {
            // message = timeStamp + " "+ message;
            int index = personsSelectionModel.getSelectedIndex();
            if (index > 0) {
                message = Network.CMD_PREF_INDIVID + " " + personList.get(index) + ": " + message;
            }
            try {
                network.getOut().writeUTF(message);
            } catch (IOException e) {
                System.out.println("Ошибка при отправке сообщения");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Ошибка ввода сообщения");
            alert.setContentText("Нельзя отправлять пустое сообщение");
            alert.show();
        }
        inputField.clear();
        textLoginNick.setText(loginNick);
    }

    public void setLoginNick(String loginNick) {
        this.loginNick = loginNick;
    }

    public void appendMessage(String message) {
        if (message.startsWith(Network.CMD_PREF_CHANGENICK) ) {
            message = changeNick(message);
        }
        if (message.startsWith(Network.CMD_PREF_AUTHOK) || message.startsWith(Network.CMD_PREF_NICK)) {
            message = addNick(message);
        }
        if (message.startsWith(Network.CMD_PREF_INDIVID)) {
            String namePerson;
            if (message.startsWith(Network.CMD_PREF_INDIVIDBACK)) {
                namePerson = message.substring(Network.CMD_PREF_INDIVIDBACK.length() + 1, message.indexOf(":"));
                message = message.substring(Network.CMD_PREF_INDIVIDBACK.length() + 1);
                message = message.replaceFirst(namePerson, loginNick);
            } else {
                namePerson = message.substring(Network.CMD_PREF_INDIVID.length() + 1, message.indexOf(":"));
                message = message.substring(Network.CMD_PREF_INDIVID.length() + 1);
            }
            personsSelectionModel.select(personList.indexOf(namePerson));
        } else {
            personsSelectionModel.select(0);
        }
        if (!message.isBlank())
            listView.getItems().add(message);
    }

    public String addNick(String strNick) {
        String[] parts = strNick.split("\\s");
        String cmdPref = parts[0];
        String msgNick = parts[1];
        if (cmdPref.equals(Network.CMD_PREF_AUTHOK)) {
            loginNick = msgNick;
            textLoginNick.setText(loginNick);
            prepareControls();
            return loginNick + " зашел в чат";
        } else if ((cmdPref.equals(Network.CMD_PREF_NICK) || cmdPref.equals(Network.CMD_PREF_NICKLIST)) && !msgNick.equals(loginNick)) {
            personList.add(msgNick);
            arrChat.add(FXCollections.observableArrayList());
            if (cmdPref.equals(Network.CMD_PREF_NICK))
                return msgNick + " зашел в чат";
        } else if (cmdPref.equals(Network.CMD_PREF_NICKEND) && !msgNick.equals(loginNick)) {
            arrChat.remove(personList.indexOf(msgNick));
            personList.remove(msgNick);
            return parts[1] + " вышел из чата";
        }
        return "";
    }

    public String changeNick(String strNick) {
        String[] parts = strNick.split("\\s");
        String oldNick = parts[1];
        String newNick = parts[2];
        if (loginNick.equals(oldNick)) {
            loginNick=newNick;
            textLoginNick.setText(loginNick);
        }else {
            personList.set(personList.indexOf(oldNick),newNick);
        }
        return oldNick + " сменил ник на " + newNick;
    }


    public void prepareControls() {
        if (network.isAuthorized()) {
            inputField.setDisable(false);
            sendButton.setDisable(false);
        } else {
            inputField.setDisable(true);
            sendButton.setDisable(true);
        }

    }

    @FXML
    void onChangeNick() {
        String newNick = textLoginNick.getText().trim();

        if (newNick.equals(loginNick)) return;

        if (newNick.length() == 0 ) {
            System.out.println("!!Ник не должен быть пустым");
            return;
        }

        try {
            network.getOut().writeUTF(Network.CMD_PREF_CHANGENICK + " " + loginNick + " " + newNick);
            textLoginNick.setText(loginNick);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("!!Ошибка смены ника");
        }
    }
}
