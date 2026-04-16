package edu.connexion3a77.controllers.ArticleController;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceArticle;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class articleAdmin {

    @FXML
    private TableView<Article> tableArticles;
    @FXML
    private TableColumn<Article, Integer> colId;
    @FXML
    private TableColumn<Article, String> colTitre;
    @FXML
    private TableColumn<Article, String> colResume;
    @FXML
    private TableColumn<Article, String> colAuteur;
    @FXML
    private TableColumn<Article, LocalDateTime> colDatePub;
    @FXML
    private TableColumn<Article, String> colStatut;
    @FXML
    private TableColumn<Article, Integer> colViews;

    @FXML
    private TextField tfTitre;
    @FXML
    private TextArea taResume;
    @FXML
    private TextField tfAuteur;
    @FXML
    private DatePicker dpDatePub;
    @FXML
    private TextArea taSummary;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private ComboBox<Magazine> cbMagazine;

    private final ServiceArticle serviceArticle = new ServiceArticle();
    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();
    private final ObservableList<Magazine> obsMagazines = FXCollections.observableArrayList();
    private Article selectedArticle;

    @FXML
    private void initialize() {
        configurerTable();
        chargerDonnees();
        cbStatut.getItems().addAll("draft", "published");
    }

    private void configurerTable() {
        if (tableArticles == null) {
            return;
        }
        colId.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        colTitre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        colResume.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getResume()));
        colAuteur.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAuteur()));
        colDatePub
                .setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDatePub()));
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut()));
        colViews.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getViews()).asObject());

        tableArticles.setItems(obsArticles);
        tableArticles.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedArticle = newVal;
            if (newVal != null) {
                remplirFormulaire(newVal);
            }
        });
    }

    private void chargerDonnees() {
        try {
            obsMagazines.clear();
            obsMagazines.addAll(serviceMagazine.afficherTout());
            if (cbMagazine != null) {
                cbMagazine.setItems(obsMagazines);
                configureMagazineComboBox(cbMagazine);
            }

            obsArticles.clear();
            obsArticles.addAll(serviceArticle.afficherTout());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les articles : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterArticle() {
        if (!validerArticle()) {
            return;
        }
        try {
            Article article = new Article(
                    tfTitre.getText().trim(),
                    taResume.getText().trim(),
                    tfAuteur.getText().trim(),
                    dpDatePub.getValue() != null ? dpDatePub.getValue().atStartOfDay() : LocalDateTime.now(),
                    taSummary.getText().trim(),
                    cbStatut.getValue(),
                    null);
            if (cbMagazine != null && cbMagazine.getValue() != null) {
                article.setMagazine(cbMagazine.getValue());
            }
            serviceArticle.ajouter(article);
            obsArticles.add(article);
            clearFormulaire();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierArticle() {
        if (selectedArticle == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un article à modifier.");
            return;
        }
        if (!validerArticle()) {
            return;
        }
        try {
            selectedArticle.setTitre(tfTitre.getText().trim());
            selectedArticle.setResume(taResume.getText().trim());
            selectedArticle.setAuteur(tfAuteur.getText().trim());
            selectedArticle.setDatePub(dpDatePub.getValue() != null ? dpDatePub.getValue().atStartOfDay() : null);
            selectedArticle.setSummary(taSummary.getText().trim());
            selectedArticle.setStatut(cbStatut.getValue());
            if (cbMagazine != null) {
                selectedArticle.setMagazine(cbMagazine.getValue());
            }
            serviceArticle.modifier(selectedArticle);
            tableArticles.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article modifié.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerArticle() {
        if (selectedArticle == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un article à supprimer.");
            return;
        }
        try {
            serviceArticle.supprimer(selectedArticle.getId());
            obsArticles.remove(selectedArticle);
            clearFormulaire();
            selectedArticle = null;
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article supprimé.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression : " + e.getMessage());
        }
    }

    private boolean validerArticle() {
        if (tfTitre == null || tfTitre.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre doit contenir au moins 2 caractères.");
            return false;
        }
        if (taResume == null || taResume.getText().trim().length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le résumé doit contenir au moins 5 caractères.");
            return false;
        }
        if (tfAuteur == null || tfAuteur.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'auteur est obligatoire.");
            return false;
        }
        if (cbStatut == null || cbStatut.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le statut est obligatoire.");
            return false;
        }
        return true;
    }

    private void remplirFormulaire(Article article) {
        tfTitre.setText(article.getTitre());
        taResume.setText(article.getResume());
        tfAuteur.setText(article.getAuteur());
        if (article.getDatePub() != null) {
            dpDatePub.setValue(article.getDatePub().toLocalDate());
        }
        taSummary.setText(article.getSummary());
        cbStatut.setValue(article.getStatut());
        if (cbMagazine != null) {
            cbMagazine.setValue(article.getMagazine());
        }
    }

    private void clearFormulaire() {
        if (tfTitre != null)
            tfTitre.clear();
        if (taResume != null)
            taResume.clear();
        if (tfAuteur != null)
            tfAuteur.clear();
        if (dpDatePub != null)
            dpDatePub.setValue(null);
        if (taSummary != null)
            taSummary.clear();
        if (cbStatut != null)
            cbStatut.setValue(null);
        if (cbMagazine != null)
            cbMagazine.setValue(null);
    }

    private void configureMagazineComboBox(ComboBox<Magazine> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(listView -> new ListCell<Magazine>() {
            @Override
            protected void updateItem(Magazine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });
        comboBox.setConverter(new StringConverter<Magazine>() {
            @Override
            public String toString(Magazine magazine) {
                return magazine == null ? null : magazine.getTitre();
            }

            @Override
            public Magazine fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (Magazine magazine : comboBox.getItems()) {
                    if (string.equals(magazine.getTitre())) {
                        return magazine;
                    }
                }
                return null;
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
