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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/dashboard.fxml"));
            Scene scene = new Scene(root, 1000, 600);
            applyStylesheet(scene);
            stage.setTitle("Tableau de bord");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(550);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
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
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de lancement");
            alert.setHeaderText("Impossible d'ouvrir la vue : " + title);
            
            String cause = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (e.getCause() != null && e.getCause().getCause() != null) {
                cause += "\n" + e.getCause().getCause().getMessage();
            }
            
            alert.setContentText("Une erreur est survenue.\nSi c'est une erreur de base de données, VEUILLEZ DÉMARRER WAMP/XAMPP (MySQL).\n\nDétails :\n" + cause);
            alert.showAndWait();
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
