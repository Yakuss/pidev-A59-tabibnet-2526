package edu.connexion3a77.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class DashboardController {

    private void openWindow(String title, String fxmlPath) {
        try {
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                java.io.File file = new java.io.File("src/main/resources" + fxmlPath);
                if (file.exists()) {
                    fxmlUrl = file.toURI().toURL();
                }
            }
            if (fxmlUrl == null) {
                throw new java.io.FileNotFoundException("FXML introuvable: " + fxmlPath);
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = new Stage();
            stage.setTitle(title);
            Scene scene = new Scene(root, 1300, 750);
            
            java.net.URL css = getClass().getResource("/styles/application.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir " + title);
            String cause = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (e.getCause() != null && e.getCause().getCause() != null) {
                cause += "\n" + e.getCause().getCause().getMessage();
            }
            alert.setContentText("Une erreur est survenue.\nSi c'est une erreur de base de données, VEUILLEZ DÉMARRER WAMP/XAMPP (MySQL).\n\nDétails :\n" + cause);
            alert.showAndWait();
        }
    }

    @FXML
    public void openAdminMagazine() {
        openWindow("Espace Admin - Magazines", "/Views/backoffice/magazineAdmin.fxml");
    }

    @FXML
    public void openAdminArticle() {
        openWindow("Espace Admin - Articles", "/Views/backoffice/articleAdmin.fxml");
    }

    @FXML
    public void openPatientMagazine() {
        openWindow("Espace Patient - Magazines", "/Views/frontoffice/magazinePatient.fxml");
    }

    @FXML
    public void openPatientArticle() {
        openWindow("Espace Patient - Articles", "/Views/frontoffice/articlePatient.fxml");
    }
}
