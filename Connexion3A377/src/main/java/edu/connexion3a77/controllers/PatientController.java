package edu.connexion3a77.controllers;

import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceArticle;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PatientController {

    @FXML
    private FlowPane containerMagazines;
    @FXML
    private TextField searchBar;

    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ServiceArticle serviceArticle = new ServiceArticle();
    private final ObservableList<Magazine> magazines = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM uuuu");

    @FXML
    private void initialize() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((obs, oldValue, newValue) -> rechercherMagazine());
        }
        chargerMagazines();
    }

    private void chargerMagazines() {
        try {
            magazines.clear();
            magazines.addAll(serviceMagazine.afficherTout());
            afficherMagazines(magazines);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les magazines : " + e.getMessage());
        }
    }

    private void afficherMagazines(List<Magazine> items) {
        if (containerMagazines == null) return;
        containerMagazines.getChildren().clear();
        for (Magazine magazine : items) {
            containerMagazines.getChildren().add(createMagazineCard(magazine));
        }
    }

    @FXML
    private void rechercherMagazine() {
        if (searchBar == null || searchBar.getText().trim().isEmpty()) {
            afficherMagazines(magazines);
            return;
        }
        String texte = searchBar.getText().trim().toLowerCase();
        List<Magazine> filtered = magazines.stream()
                .filter(m -> {
                    String titre = m.getTitre() != null ? m.getTitre().toLowerCase() : "";
                    String description = m.getDescription() != null ? m.getDescription().toLowerCase() : "";
                    return titre.contains(texte) || description.contains(texte);
                })
                .collect(Collectors.toList());
        afficherMagazines(filtered);
    }

    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return loadPlaceholder();
        }

        try {
            // Cas 1 : file:/ ou file:// URL (ce que tu as en base)
            if (imagePath.startsWith("file:")) {
                return new Image(imagePath);
            }

            // Cas 2 : URL HTTP
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                return new Image(imagePath, true);
            }

            // Cas 3 : Chemin Windows absolu (C:/... ou C:\...)
            if (imagePath.length() > 2 && imagePath.charAt(1) == ':') {
                File file = new File(imagePath);
                if (file.exists()) {
                    return new Image(file.toURI().toString());
                }
            }

            // Cas 4 : Chemin absolu Unix (/home/...)
            if (imagePath.startsWith("/")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    return new Image(file.toURI().toString());
                }
            }

        } catch (Exception e) {
            System.err.println("Image load failed [" + imagePath + "] : " + e.getMessage());
        }

        return loadPlaceholder();
    }

    private Image loadPlaceholder() {
        try {
            InputStream stream = getClass().getResourceAsStream("/images/placeholder.png");
            if (stream != null) return new Image(stream);
        } catch (Exception e) {
            System.err.println("Placeholder introuvable : " + e.getMessage());
        }
        return null;
    }

    private VBox createMagazineCard(Magazine magazine) {
        // 1. L'Image avec coins arrondis en haut
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        // Technique pour arrondir les coins supérieurs de l'image
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(280, 160);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);

        Image img = loadImage(magazine.getImage());
        if (img != null) imageView.setImage(img);

        // 2. Les Labels (Utilisation des classes CSS)
        Label title = new Label(magazine.getTitre());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Label description = new Label(magazine.getDescription());
        description.getStyleClass().add("card-description");
        description.setWrapText(true);
        description.setMinHeight(50);
        description.setMaxHeight(50);

        Label date = new Label(magazine.getDateCreate() != null
                ? dateFormatter.format(magazine.getDateCreate().toLocalDate())
                : "Date indisponible");
        date.getStyleClass().add("card-meta");

        // 3. Le Bouton
        Button actionButton = new Button("Lire maintenant");
        actionButton.getStyleClass().add("primary-button");
        actionButton.setPrefWidth(240); // Largeur fixe pour l'esthétique
        actionButton.setOnAction(event -> ouvrirArticleDuMagazine(magazine));

        // 4. La Carte (Conteneur)
        VBox card = new VBox(12);
        card.getStyleClass().add("magazine-card");

        // On ajoute les éléments
        card.getChildren().addAll(imageView, title, description, date, actionButton);

        // Marges internes pour le texte (sauf l'image qui doit toucher le haut)
        Insets sidePadding = new Insets(0, 18, 0, 18);
        VBox.setMargin(title, sidePadding);
        VBox.setMargin(description, sidePadding);
        VBox.setMargin(date, sidePadding);
        VBox.setMargin(actionButton, new Insets(5, 18, 15, 18));

        // On retire les styles "inline" pour laisser le fichier CSS travailler
        card.setStyle("");

        return card;
    }

    private void ouvrirArticleDuMagazine(Magazine magazine) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/Views/frontoffice/articlePatient.fxml");
            if (fxmlUrl == null) {
                java.io.File file = new java.io.File("src/main/resources/Views/frontoffice/articlePatient.fxml");
                if (file.exists()) {
                    fxmlUrl = file.toURI().toURL();
                }
            }
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "FXML introuvable : /Views/frontoffice/articlePatient.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof edu.connexion3a77.controllers.ArticleController.articlePatient pc) {
                pc.filtrerParMagazine(magazine.getId());
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Articles — " + magazine.getTitre());
            Scene scene = new Scene(root, 1100, 720);
            java.net.URL css = getClass().getResource("/styles/application.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la page : " + cause.getMessage());
            cause.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
