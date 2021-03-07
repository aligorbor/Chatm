package org.openjfx.client;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjfx.App;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private static final Logger logger = LogManager.getLogger("client");

    private static final int DEFAULT_SERVER_SOCKET = 8189;
    private static final String DEFAULT_SERVER_HOST = "localhost";

    public static final String CMD_PREF_REGISTER = "/register";
    public static final String CMD_PREF_AUTH = "/auth";
    public static final String CMD_PREF_AUTHOK = "/authok";
    public static final String CMD_PREF_NICK = "/nick";
    public static final String CMD_PREF_NICKLIST = "/nicklist";
    public static final String CMD_PREF_NICKEND = "/nickend";
    public static final String CMD_PREF_LOGINEND = "/loginend";
    public static final String CMD_PREF_END = "/end";
    public static final String CMD_PREF_INDIVID = "/w";
    public static final String CMD_PREF_INDIVIDBACK = "/wback";
    public static final String CMD_PREF_CHANGENICK = "/changenick";

    public static final int HISTORY_NUMBER_LINES = 100;
    public static final String HISTORY_URL_STARTS = "src/main/resources/lib/history_";


    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authorized;
    private String currentLogin;

    private final Object lockAuthorization = new Object();

    private final int port;
    private final String host;

    private Controller controller;
    private AuthController authController;
    private RegisterController registerController;

    private App appChat;

    public App getAppChat() {
        return appChat;
    }

    public void setAppChat(App appChat) {
        this.appChat = appChat;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setAuthController(AuthController authController) {
        this.authController = authController;
    }

    public void setRegisterController(RegisterController registerController) {
        this.registerController = registerController;
    }

    public String getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(String currentLogin) {
        this.currentLogin = currentLogin;
    }

    public Network(String host, int port) {
        this.port = port;
        this.host = host;
        currentLogin = "";
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public Network() {
        this.host = DEFAULT_SERVER_HOST;
        this.port = DEFAULT_SERVER_SOCKET;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            getLoginsFromServer();
            setAuthorized(false);
            startAuthorization();
        } catch (IOException e) {
            //   System.out.println("Соединение не установлено");
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    public void disconnect() {
        try {
            controller.setLoginNick("");
            authController.clearLoginList();
            out.writeUTF(Network.CMD_PREF_END);
            socket.close();
        } catch (IOException e) {
            // System.out.println("Ошибка отключения");
            logger.error(e.getMessage());
        }

    }

    private void getLoginsFromServer() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(Network.CMD_PREF_LOGINEND)) {
                break;
            }
            authController.addLoginList(message);
        }
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void startAuthorization() {
        new Thread(() -> {
            synchronized (lockAuthorization) {
                try {
                    while (true) {
                        String message = in.readUTF();
                        if (message.startsWith(Network.CMD_PREF_AUTHOK)) {
                            setAuthorized(true);
                            Platform.runLater(() -> controller.restoreChatFromFiles());
                            Platform.runLater(() -> controller.appendMessage(message));

                            Platform.runLater(() -> controller.refreshChatList());
                            Platform.runLater(() -> appChat.getAuthStage().close());
                            Platform.runLater(() -> appChat.getRegisterStage().close());
                            Platform.runLater(() -> appChat.getPrimaryStage().setTitle("Chat " + currentLogin));
                            Platform.runLater(() -> appChat.getPrimaryStage().setAlwaysOnTop(true));
                            break;
                        } else
                            //  Platform.runLater(() -> controller.appendMessage(message));
                            Platform.runLater(() -> authController.authErrAlert("Ошибка авторизации", message));

                    }
                } catch (IOException e) {
                    //   System.out.println("Ошибка при авторизации");
                    logger.error(e.getMessage());
                    System.exit(1);
                }
            }
        }).start();

        waitMessage();
    }

    public void waitMessage() {
        Thread thread = new Thread(() -> {
            synchronized (lockAuthorization) {
                if (isAuthorized()) {
                    try {
                        while (true) {
                            String message = in.readUTF();
                            if (message.equalsIgnoreCase(Network.CMD_PREF_END)) {
                                break;
                            }
                            Platform.runLater(() -> controller.appendMessage(message));
                        }
                    } catch (IOException | NullPointerException e) {
                        //   System.out.println("Ошибка подключения");
                        logger.error(e.getMessage());
                        System.exit(1);
                    }
                } else
                    System.exit(1);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

}
