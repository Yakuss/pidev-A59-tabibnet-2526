package edu.connexion3a77.controllers.MAgazinController;

import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class magazinePatient {

    @FXML private ListView<Magazine> listMagazines;
    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblStatut;
    @FXML private Label lblDateCreate;
    @FXML private Label lblPdf;
    @FXML private TextField tfSearch;

    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Magazine> obsMagazines = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (listMagazines != null) {
            listMagazines.setItems(obsMagazines);
            listMagazines.getSelectionModel().selectedItemProperty().addListener((obs, old, magazine) -> {
                if (magazine != null) {
                    afficherDetails(magazine);
                }
            });
        }
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            obsMagazines.clear();
            obsMagazines.addAll(serviceMagazine.afficherTout());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les magazines : " + e.getMessage());
        }
    }

    @FXML
    private void rechercherMagazine() {
        if (tfSearch == null || tfSearch.getText().trim().isEmpty()) {
            chargerDonnees();
            return;
        }
        String texte = tfSearch.getText().trim().toLowerCase();
        ObservableList<Magazine> filtered = obsMagazines.filtered(magazine ->
                magazine.getTitre().toLowerCase().contains(texte)
                        || magazine.getDescription().toLowerCase().contains(texte)
        );
        if (listMagazines != null) {
            listMagazines.setItems(filtered);
        }
    }

    private void afficherDetails(Magazine magazine) {
        if (lblTitre != null) lblTitre.setText(magazine.getTitre());
        if (lblDescription != null) lblDescription.setText(magazine.getDescription());
        if (lblStatut != null) lblStatut.setText(magazine.getStatut());
        if (lblDateCreate != null) lblDateCreate.setText(magazine.getDateCreate() != null ? magazine.getDateCreate().toString() : "N/A");
        if (lblPdf != null) lblPdf.setText(magazine.getPdfFile());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

