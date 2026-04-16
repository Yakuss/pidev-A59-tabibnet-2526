package edu.connexion3a77.controllers.ArticleController;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.services.ServiceArticle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class articlePatient {

    @FXML private ListView<Article> listArticles;
    @FXML private Label lblTitre;
    @FXML private Label lblAuteur;
    @FXML private Label lblDatePub;
    @FXML private Label lblStatut;
    @FXML private Label lblMagazine;
    @FXML private TextArea taSummary;
    @FXML private TextField tfSearch;

    private final ServiceArticle serviceArticle = new ServiceArticle();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (listArticles != null) {
            listArticles.setItems(obsArticles);
            listArticles.getSelectionModel().selectedItemProperty().addListener((obs, old, article) -> {
                if (article != null) {
                    afficherDetails(article);
                }
            });
        }
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            obsArticles.clear();
            obsArticles.addAll(serviceArticle.afficherTout());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les articles : " + e.getMessage());
        }
    }

    @FXML
    private void rechercherArticle() {
        if (tfSearch == null || tfSearch.getText().trim().isEmpty()) {
            chargerDonnees();
            return;
        }
        String texte = tfSearch.getText().trim().toLowerCase();
        ObservableList<Article> filtered = obsArticles.filtered(article ->
                article.getTitre().toLowerCase().contains(texte)
                        || article.getResume().toLowerCase().contains(texte)
                        || article.getAuteur().toLowerCase().contains(texte)
        );
        if (listArticles != null) {
            listArticles.setItems(filtered);
        }
    }

    private void afficherDetails(Article article) {
        if (lblTitre != null) lblTitre.setText(article.getTitre());
        if (lblAuteur != null) lblAuteur.setText(article.getAuteur());
        if (lblDatePub != null) lblDatePub.setText(article.getDatePub() != null ? article.getDatePub().toString() : "N/A");
        if (lblStatut != null) lblStatut.setText(article.getStatut());
        if (lblMagazine != null) {
            lblMagazine.setText(article.getMagazine() != null ? article.getMagazine().getTitre() : "Aucun magazine");
        }
        if (taSummary != null) taSummary.setText(article.getSummary());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

