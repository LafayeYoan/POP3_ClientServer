package POP3_ClientServer.Client;

import POP3_ClientServer.common.EMail;
import POP3_ClientServer.common.MailFileManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

/***
 * LAFAYE DE MICHEAUX Yoan - LHOPITAL Sacha
 */
public class ClientApp {

    public static void main(String[] args) {

        Client c = new Client("localhost", 5555);
        Thread clientThread =  new Thread(c);
        clientThread.start();
        
        
    }

    /*private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<EMail> mails = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    *//**
     * Returns the data as an observable list of mail.
     * @return
     *//*
    public ObservableList<EMail> getMails() {
        return mails;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Init de mails todo : à changer
        mails.addAll(new MailFileManager("sacha").getEmails());

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Client POP3 Application");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ClientApp.class.getResource("web/connectionForm.fxml"));
        rootLayout = (BorderPane) loader.load();

        // Show the scene containing the root layout.
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();

        showMailOverview();
    }

    *//**
     * Shows the mails overview inside the root layout.
     *//*
    public void showMailOverview() {
        try {
            // Load mails overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ClientApp.class.getResource("web/mailsOverview.fxml"));
            AnchorPane mailOverview = (AnchorPane) loader.load();

            // Set mail overview into the center of root layout.
            rootLayout.setCenter(mailOverview);

            // Give the controller access to the main app.
            MailsOverview controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
