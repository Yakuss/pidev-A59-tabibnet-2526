package edu.connexion3a77.controllers.MAgazinController;

import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class magazineAdmin {

    @FXML private TableView<Magazine> tableMagazines;
    @FXML private TableColumn<Magazine, Integer> colId;
    @FXML private TableColumn<Magazine, String> colTitre;
    @FXML private TableColumn<Magazine, String> colDescription;
    @FXML private TableColumn<Magazine, String> colStatut;
    @FXML private TableColumn<Magazine, String> colPdf;

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField tfPdfFile;

    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Magazine> obsMagazines = FXCollections.observableArrayList();
    private Magazine selectedMagazine;

    @FXML
    private void initialize() {
        configurerTable();
        chargerDonnees();
        cbStatut.getItems().addAll("draft", "published", "archived");
    }

    private void configurerTable() {
        if (tableMagazines == null) {
            return;
        }
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        colTitre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        colDescription.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut()));
        colPdf.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPdfFile()));

        tableMagazines.setItems(obsMagazines);
        tableMagazines.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedMagazine = newVal;
            if (newVal != null) {
                remplirFormulaire(newVal);
            }
        });
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
    private void ajouterMagazine() {
        if (!validerMagazine()) {
            return;
        }
        try {
            Magazine magazine = new Magazine(
                    tfTitre.getText().trim(),
                    taDescription.getText().trim(),
                    cbStatut.getValue(),
                    tfPdfFile.getText().trim()
            );
            serviceMagazine.ajouter(magazine);
            obsMagazines.add(magazine);
            clearFormulaire();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine ajouté.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierMagazine() {
        if (selectedMagazine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un magazine à modifier.");
            return;
        }
        if (!validerMagazine()) {
            return;
        }
        try {
            selectedMagazine.setTitre(tfTitre.getText().trim());
            selectedMagazine.setDescription(taDescription.getText().trim());
            selectedMagazine.setStatut(cbStatut.getValue());
            selectedMagazine.setPdfFile(tfPdfFile.getText().trim());
            serviceMagazine.modifier(selectedMagazine);
            tableMagazines.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine modifié.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerMagazine() {
        if (selectedMagazine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un magazine à supprimer.");
            return;
        }
        try {
            serviceMagazine.supprimer(selectedMagazine.getId());
            obsMagazines.remove(selectedMagazine);
            clearFormulaire();
            selectedMagazine = null;
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Magazine supprimé.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression : " + e.getMessage());
        }
    }

    private boolean validerMagazine() {
        if (tfTitre == null || tfTitre.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre doit contenir au moins 2 caractères.");
            return false;
        }
        if (taDescription == null || taDescription.getText().trim().length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description doit contenir au moins 5 caractères.");
            return false;
        }
        if (cbStatut == null || cbStatut.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le statut est obligatoire.");
            return false;
        }
        return true;
    }

    private void remplirFormulaire(Magazine magazine) {
        tfTitre.setText(magazine.getTitre());
        taDescription.setText(magazine.getDescription());
        cbStatut.setValue(magazine.getStatut());
        tfPdfFile.setText(magazine.getPdfFile());
    }

    private void clearFormulaire() {
        if (tfTitre != null) tfTitre.clear();
        if (taDescription != null) taDescription.clear();
        if (cbStatut != null) cbStatut.setValue(null);
        if (tfPdfFile != null) tfPdfFile.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

