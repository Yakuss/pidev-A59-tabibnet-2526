package edu.connexion3a77.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

public class MainClass extends Application {
    private static final String STYLESHEET = "/styles/application.css";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Bienvenue - Choix du mode");
        stage.setScene(createLauncherScene());
        stage.setMinWidth(800);
        stage.setMinHeight(520);
        stage.show();
    }

    private Scene createLauncherScene() {
        Label title = new Label("Bienvenue dans Connexion 3A377");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Choisissez votre espace : administration ou consultation patient.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("card-description");

        Button adminButton = createViewButton("Espace Admin", "/Views/backoffice/magazineAdmin.fxml");
        adminButton.setStyle("-fx-min-width: 220px;");
        Button patientButton = createViewButton("Espace Patient", "/Views/frontoffice/magazinePatient.fxml");
        patientButton.setStyle("-fx-min-width: 220px;");

        VBox buttonBox = new VBox(16, adminButton, patientButton);
        buttonBox.setPadding(new Insets(10));

        VBox root = new VBox(28, title, subtitle, buttonBox);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("launcher-root");

        Scene scene = new Scene(root, 820, 520);
        applyStylesheet(scene);
        return scene;
    }

    private Button createViewButton(String label, String fxmlPath) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("launcher-button");
        button.setOnAction(event -> openViewWindow(label, fxmlPath));
        return button;
    }

    private void openViewWindow(String title, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setTitle(title);
            Scene scene = new Scene(root, 1300, 750);
            applyStylesheet(scene);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyStylesheet(Scene scene) {
        URL css = getClass().getResource(STYLESHEET);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
