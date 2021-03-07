package org.openjfx.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Controller {

    private static final Logger logger = LogManager.getLogger("client");

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
    private MultipleSelectionModel<String> chatSelectionModel;

    private ObservableList<String> personList = FXCollections.observableArrayList
            ("Общий чат");
    private final ArrayList<ObservableList<String>> arrChat = new ArrayList<>();

    private Network network;
    private String loginNick;
    private ArrayList<Integer> currentHistNumbLines = new ArrayList<>();

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    void initialize() {
        personsSelectionModel = listPersons.getSelectionModel();
        chatSelectionModel = listView.getSelectionModel();

        personsSelectionModel.selectedItemProperty().addListener((changed, oldValue, newValue) -> {
            labelPersonChat.setText(newValue);
            int index = personsSelectionModel.getSelectedIndex();
            listView.setItems(arrChat.get(index));
            refreshChatList();
        });

        listPersons.setItems(personList);
        while (arrChat.size() < personList.size()) {
            arrChat.add(FXCollections.observableArrayList());
            currentHistNumbLines.add(0);
        }
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
                //  System.out.println("Ошибка при отправке сообщения");
                logger.error(e.getMessage());
            }
        } else {
            chatErrAlert("Ошибка ввода сообщения", "Нельзя отправлять пустое сообщение");
        }
        inputField.clear();
        textLoginNick.setText(loginNick);
    }

    public void setLoginNick(String loginNick) {
        this.loginNick = loginNick;
    }

    public void refreshChatList() {
        if (listView.getItems().size() > 0)
            listView.scrollTo(listView.getItems().size() - 1);
        //  chatSelectionModel.select(listView.getItems().size() - 1);
    }

    public void appendMessage(String message) {
        if (message.startsWith(Network.CMD_PREF_CHANGENICK)) {
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
        if (!message.isBlank()) {
            listView.getItems().add(message);
            refreshChatList();
        }
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
            currentHistNumbLines.add(0);
            restoreChatFromFile(arrChat.size() - 1);
            if (cmdPref.equals(Network.CMD_PREF_NICK))
                return msgNick + " зашел в чат";
        } else if (cmdPref.equals(Network.CMD_PREF_NICKEND) && !msgNick.equals(loginNick)) {
            int ind = personList.indexOf(msgNick);
            saveChatToFile(ind);
            arrChat.remove(ind);
            currentHistNumbLines.remove(ind);
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
            loginNick = newNick;
            textLoginNick.setText(loginNick);
        } else {
            personList.set(personList.indexOf(oldNick), newNick);
        }
        changeHistoryFileName(oldNick, newNick);
        return oldNick + " сменил ник на " + newNick;
    }

    private void changeHistoryFileName(String oldNick, String newNick) {
        File fileOld = new File(Network.HISTORY_URL_STARTS + network.getCurrentLogin() + "_" + oldNick + ".txt");
        File fileNew = new File(Network.HISTORY_URL_STARTS + network.getCurrentLogin() + "_" + newNick + ".txt");
        if (fileNew.exists()) fileNew.delete();
        if (fileOld.exists()) fileOld.renameTo(fileNew);

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

        if (newNick.length() == 0) {
            chatErrAlert("Ошибка смены ника", "Ник не должен быть пустым");
            return;
        }

        try {
            network.getOut().writeUTF(Network.CMD_PREF_CHANGENICK + " " + loginNick + " " + newNick);
            textLoginNick.setText(loginNick);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void saveChatToFiles() {
        for (String person : personList) {
            saveChatToFile(personList.indexOf(person));
        }
    }

    public void saveChatToFile(int chatIndex) {
        String fileName = Network.HISTORY_URL_STARTS + network.getCurrentLogin() + "_" + personList.get(chatIndex) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            ObservableList<String> chat = arrChat.get(chatIndex);

            for (int i = currentHistNumbLines.get(chatIndex); i < chat.size(); i++) {
                writer.write(chat.get(i) + "\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    public void restoreChatFromFiles() {
        arrChat.get(0).clear();   //удаляю сообщения от авторизации
        for (String person : personList) {
            restoreChatFromFile(personList.indexOf(person));
        }
    }

    public void restoreChatFromFile(int chatIndex) {
        currentHistNumbLines.set(chatIndex, 0);

        String fileName = Network.HISTORY_URL_STARTS + network.getCurrentLogin() + "_" + personList.get(chatIndex) + ".txt";

        if (new File(fileName).exists()) {
            ArrayList<String> history = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String str;
                while ((str = reader.readLine()) != null) {
                    history.add(str);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            int indexFrom;
            if (history.size() > Network.HISTORY_NUMBER_LINES) {
                indexFrom = history.size() - Network.HISTORY_NUMBER_LINES;
                currentHistNumbLines.set(chatIndex, Network.HISTORY_NUMBER_LINES);
            } else {
                indexFrom = 0;
                currentHistNumbLines.set(chatIndex, history.size());
            }
            for (int i = indexFrom; i < history.size(); i++) {
                arrChat.get(chatIndex).add(history.get(i));
            }

        }
    }

    public void chatErrAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
