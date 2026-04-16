package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Appointment;
import edu.connexion3a77.entities.Medecin;
import edu.connexion3a77.entities.Ordonnance;
import edu.connexion3a77.services.OrdonnanceService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

public class OrdonnanceController {

    @FXML
    private DatePicker dateOrdonnanceDP;
    @FXML
    private TextField diagnosisTF;           // TextField pour diagnostic (courte ligne)
    @FXML
    private TextArea medicamentTF;          // ✅ CHANGÉ: TextArea pour médicaments (multi-lignes)
    @FXML
    private TextArea posologieTF;           // ✅ CHANGÉ: TextArea pour posologie (multi-lignes)
    @FXML
    private TextArea notesTA;
    @FXML
    private TextArea instructionsTA;
    @FXML
    private ComboBox<Appointment> rendezVousCB;

    private OrdonnanceService ordonnanceService = new OrdonnanceService();
    private Ordonnance currentOrdonnance = null;

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    @FXML
    public void annuler(ActionEvent event) {
        closeWindow();
    }

    public void initData(Ordonnance o) {
        this.currentOrdonnance = o;
        if (o.getDateOrdonnance() != null) {
            dateOrdonnanceDP.setValue(new java.sql.Date(o.getDateOrdonnance().getTime()).toLocalDate());
        }
        diagnosisTF.setText(o.getDiagnosis());
        medicamentTF.setText(o.getMedicament());
        posologieTF.setText(o.getPosologie());
        notesTA.setText(o.getNotes());
        instructionsTA.setText(o.getInstructions());
    }

    @FXML
    public void ajouterOrdonnance(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        Ordonnance ordonnance = new Ordonnance();
        populateEntityFromFields(ordonnance);

        Medecin currentMedecin = new Medecin("NomMedecin", "PrenomMedecin", "Specialite");
        ordonnance.setMedecin(currentMedecin);

        try {
            ordonnanceService.add(ordonnance);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance ajoutée avec succès !");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifierOrdonnance(ActionEvent event) {
        if (currentOrdonnance == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune ordonnance sélectionnée.");
            return;
        }
        if (!validateFields()) {
            return;
        }

        populateEntityFromFields(currentOrdonnance);

        try {
            ordonnanceService.update(currentOrdonnance);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance modifiée avec succès !");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerOrdonnance(ActionEvent event) {
        if (currentOrdonnance == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune ordonnance sélectionnée pour la suppression.");
            return;
        }
        try {
            ordonnanceService.delete(currentOrdonnance.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance supprimée avec succès !");
            clearFields();
            currentOrdonnance = null;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void populateEntityFromFields(Ordonnance ordonnance) {
        if (dateOrdonnanceDP.getValue() != null) {
            Date date = Date.from(dateOrdonnanceDP.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            ordonnance.setDateOrdonnance(date);
        }
        ordonnance.setDiagnosis(diagnosisTF.getText());
        ordonnance.setMedicament(medicamentTF.getText());
        ordonnance.setPosologie(posologieTF.getText());
        ordonnance.setNotes(notesTA.getText());
        ordonnance.setInstructions(instructionsTA.getText());
        ordonnance.setAppointment(rendezVousCB.getValue());
    }

    private boolean validateFields() {
        if (dateOrdonnanceDP.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date de l'ordonnance est obligatoire.");
            return false;
        }
        if (diagnosisTF.getText().trim().isEmpty() || diagnosisTF.getText().length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le diagnostic est obligatoire et max 255 caractères.");
            return false;
        }
        if (medicamentTF.getText().trim().isEmpty() || medicamentTF.getText().length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le médicament est obligatoire et max 255 caractères.");
            return false;
        }
        if (posologieTF.getText().trim().isEmpty() || posologieTF.getText().length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La posologie est obligatoire et max 255 caractères.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        dateOrdonnanceDP.setValue(null);
        diagnosisTF.clear();
        medicamentTF.clear();
        posologieTF.clear();
        notesTA.clear();
        instructionsTA.clear();
        rendezVousCB.setValue(null);
    }

    private void closeWindow() {
        Stage stage = (Stage) diagnosisTF.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}