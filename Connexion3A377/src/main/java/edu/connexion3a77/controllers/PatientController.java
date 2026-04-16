package edu.connexion3a77.controllers;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceArticle;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
        if (containerMagazines == null) {
            return;
        }
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
                .filter(magazine -> magazine.getTitre().toLowerCase().contains(texte)
                        || magazine.getDescription().toLowerCase().contains(texte))
                .collect(Collectors.toList());
        afficherMagazines(filtered);
    }

    private VBox createMagazineCard(Magazine magazine) {
        Label title = new Label(magazine.getTitre());
        title.getStyleClass().add("card-title");

        Label description = new Label(magazine.getDescription());
        description.getStyleClass().add("card-description");
        description.setWrapText(true);
        description.setMaxWidth(260);

        Label date = new Label(magazine.getDateCreate() != null
                ? dateFormatter.format(magazine.getDateCreate().toLocalDate())
                : "Date indisponible");
        date.getStyleClass().add("card-meta");

        Button actionButton = new Button("Lire maintenant");
        actionButton.getStyleClass().add("primary-button");
        actionButton.setOnAction(event -> ouvrirArticleDuMagazine(magazine));

        VBox card = new VBox(10, title, description, date, actionButton);
        card.getStyleClass().add("magazine-card");
        card.setMinWidth(280);
        card.setMaxWidth(280);
        card.setPrefWidth(280);
        card.setMinHeight(220);
        card.setMaxHeight(260);
        card.setPrefHeight(260);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 18; -fx-border-radius: 16; -fx-background-radius: 16; -fx-effect: dropshadow(two-pass-box, rgba(15,23,42,0.08), 10, 0, 0, 6);");
        return card;
    }

    private void ouvrirArticleDuMagazine(Magazine magazine) {
        try {
            List<Article> articles = serviceArticle.afficherTout().stream()
                    .filter(article -> article.getMagazine() != null
                            && article.getMagazine().getId() == magazine.getId())
                    .collect(Collectors.toList());
            Optional<Article> premierArticle = articles.stream().findFirst();
            if (premierArticle.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucun article", "Aucun article disponible pour ce magazine.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/frontoffice/articlePatient.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof edu.connexion3a77.controllers.ArticleController.ArticleDetailController articleController) {
                articleController.setArticle(premierArticle.get());
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(premierArticle.get().getTitre());
            Scene scene = new Scene(root, 1000, 700);
            var css = getClass().getResource("/styles/application.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.show();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'article : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de l'article : " + e.getMessage());
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
