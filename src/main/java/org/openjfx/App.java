package org.openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjfx.client.AuthController;
import org.openjfx.client.Controller;
import org.openjfx.client.Network;
import org.openjfx.client.RegisterController;
import org.openjfx.server.ServerApp;

import java.io.IOException;

public class App extends Application {
    private static final Logger logger = LogManager.getLogger("client");

    private Stage primaryStage;
    private Stage authStage;
    private Stage registerStage;

    private Network network;
    private Controller controller;
    private AuthController authLoaderController;
    private RegisterController registerLoaderController;

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getRegisterStage() {
        return registerStage;
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            chatDialog();
            authDialig();
            registerDialig();
            network = new Network();
            network.setController(controller);
            network.setAuthController(authLoaderController);
            network.setRegisterController(registerLoaderController);
            network.setAppChat(this);
            controller.setNetwork(network);
            authLoaderController.setNetwork(network);
            registerLoaderController.setNetwork(network);

            if (!network.connect())
                System.exit(0);

        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void authDialig() throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(App.class.getResource("auth-view.fxml"));

        Parent root = authLoader.load();
        authStage = new Stage();
        authStage.setTitle("Аутентификация");
        authStage.setScene(new Scene(root));
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setOnCloseRequest(evt -> {
            if (!network.isAuthorized()) System.exit(0);
        });
        //authStage.setX(2200);
        authStage.show();
        authLoaderController = authLoader.getController();
    }

    private void chatDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root));
        //primaryStage.setX(2200);
        controller = loader.getController();
        primaryStage.setOnCloseRequest(evt -> {
            controller.saveChatToFiles();
        });
        primaryStage.show();
    }

    private void registerDialig() throws IOException {
        FXMLLoader registerLoader = new FXMLLoader();
        registerLoader.setLocation(App.class.getResource("register-view.fxml"));

        Parent root = registerLoader.load();
        registerStage = new Stage();
        registerStage.setTitle("Регистрация");
        registerStage.setScene(new Scene(root));
        registerStage.initModality(Modality.WINDOW_MODAL);
        registerStage.initOwner(authStage);
        registerLoaderController = registerLoader.getController();
    }
}

