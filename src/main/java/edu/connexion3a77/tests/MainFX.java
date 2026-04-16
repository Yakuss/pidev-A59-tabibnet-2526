package edu.connexion3a77.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            File file = new File("src/main/resources/DashboardView.fxml");
            URL url = file.toURI().toURL();
            Parent root = FXMLLoader.load(url);
            
            // Si vous préférez voir le Rapport par défaut, décommentez ceci et commentez les lignes du dessus:
            // File file = new File("src/main/resources/RapportView.fxml");
            // URL url = file.toURI().toURL();
            // Parent root = FXMLLoader.load(url);

            Scene scene = new Scene(root, 600, 650);
            
            primaryStage.setTitle("Dossier Médical - Gestion");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur: Impossible de charger le fichier FXML. Vérifiez vos chemins.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
