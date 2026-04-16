package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Appointment;
import edu.connexion3a77.entities.Medecin;
import edu.connexion3a77.entities.Rapport;
import edu.connexion3a77.services.RapportService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RapportController {

    @FXML
    private TextField consultationReasonTF;
    @FXML
    private TextField diagnosisTF;
    @FXML
    private TextArea observationsTA;
    @FXML
    private TextArea recommendationsTA;
    @FXML
    private TextArea treatmentsTA;
    @FXML
    private ComboBox<Appointment> rendezVousCB;

    private RapportService rapportService = new RapportService();
    private Rapport currentRapport = null;

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    /**
     * ✅ IMPORTANT: Cette méthode doit être appelée AVANT stage.show()
     */
    public void initData(Rapport r) {
        this.currentRapport = r;

        if (r == null) {
            System.out.println("⚠️ initData appelé avec rapport null");
            return;
        }

        // Champs texte avec protection null
        consultationReasonTF.setText(getSafeString(r.getConsultationReason()));
        diagnosisTF.setText(getSafeString(r.getDiagnosis()));
        observationsTA.setText(getSafeString(r.getObservations()));
        recommendationsTA.setText(getSafeString(r.getRecommendations()));
        treatmentsTA.setText(getSafeString(r.getTreatments()));

        // ComboBox
        rendezVousCB.setValue(r.getAppointment());

        System.out.println("✅ initData exécuté pour rapport ID: " + r.getId());
    }

    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    @FXML
    public void ajouterRapport(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        Rapport rapport = new Rapport();
        populateEntityFromFields(rapport);

        Medecin currentMedecin = new Medecin("NomMedecin", "PrenomMedecin", "Specialite");
        rapport.setMedecin(currentMedecin);

        try {
            rapportService.add(rapport);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport ajouté avec succès !");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void modifierRapport(ActionEvent event) {
        if (currentRapport == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun rapport sélectionné.");
            return;
        }
        if (!validateFields()) {
            return;
        }

        // ✅ Mettre à jour directement currentRapport
        populateEntityFromFields(currentRapport);

        try {
            rapportService.update(currentRapport);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport modifié avec succès !");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void populateEntityFromFields(Rapport rapport) {
        rapport.setConsultationReason(consultationReasonTF.getText());
        rapport.setDiagnosis(diagnosisTF.getText());
        rapport.setObservations(observationsTA.getText());
        rapport.setRecommendations(recommendationsTA.getText());
        rapport.setTreatments(treatmentsTA.getText());
        rapport.setAppointment(rendezVousCB.getValue());
    }

    private boolean validateFields() {
        if (consultationReasonTF.getText().trim().isEmpty() || consultationReasonTF.getText().length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La raison est obligatoire et max 255 caractères.");
            return false;
        }
        if (diagnosisTF.getText().trim().isEmpty() || diagnosisTF.getText().length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le diagnostic est obligatoire et max 255 caractères.");
            return false;
        }
        if (observationsTA.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Les observations sont obligatoires.");
            return false;
        }
        if (recommendationsTA.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Les recommandations sont obligatoires.");
            return false;
        }
        if (treatmentsTA.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Les traitements sont obligatoires.");
            return false;
        }
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) consultationReasonTF.getScene().getWindow();
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