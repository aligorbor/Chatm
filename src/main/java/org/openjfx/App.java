package org.openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.openjfx.client.AuthController;
import org.openjfx.client.Controller;
import org.openjfx.client.Network;

import java.io.IOException;

public class App extends Application {
    private Stage primaryStage;
    private Stage authStage;
    private Network network;
    private Controller controller;
    private AuthController authLoaderController;

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getAuthStage() {
        return authStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        chatDialog();
        authDialig();
        network = new Network();
        network.setController(controller);
        network.setAuthController(authLoaderController);
        network.setMainChat(this);
        controller.setNetwork(network);
        authLoaderController.setNetwork(network);

        if (!network.connect())
            System.exit(1);
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
        primaryStage.show();
    }

}