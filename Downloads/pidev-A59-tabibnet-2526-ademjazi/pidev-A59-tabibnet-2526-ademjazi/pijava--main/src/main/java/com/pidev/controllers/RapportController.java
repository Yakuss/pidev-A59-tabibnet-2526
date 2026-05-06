package com.pidev.controllers;

import com.pidev.models.Appointment;
import com.pidev.models.Rapport;
import com.pidev.services.RapportService;
import com.pidev.services.AppointmentService;
import com.pidev.utils.UserSession;
import com.pidev.utils.AppointmentSessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;

import java.sql.SQLException;

public class RapportController implements Initializable {

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
    private AppointmentService appointmentService = new AppointmentService();
    private Rapport currentRapport = null;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("RapportController: initialize() called");
        loadAppointments();
        
        // Auto-select appointment from session if available
        Appointment sessionAppointment = AppointmentSessionManager.getInstance().getSelectedAppointment();
        if (sessionAppointment != null) {
            System.out.println("RapportController: Auto-selecting appointment from session: " + sessionAppointment.getId());
            rendezVousCB.setValue(sessionAppointment);
        }
    }

    private void loadAppointments() {
        try {
            Integer patientId = UserSession.getInstance().getSelectedPatientId();
            Integer medecinId = UserSession.getInstance().getSelectedMedecinId();
            System.out.println("Rapport: Loading appointments for Patient=" + patientId + ", Medecin=" + medecinId);
            if (medecinId != null) {
                List<Appointment> list;
                if (patientId != null && patientId > 0) {
                    list = appointmentService.getAppointmentsByPatientAndDoctor(patientId, medecinId);
                } else {
                    list = appointmentService.getAppointmentsByDoctor(medecinId);
                }
                System.out.println("Rapport: Found " + list.size() + " appointments");
                rendezVousCB.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            System.err.println("Error loading appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initData(Rapport r) {
        this.currentRapport = r;
        if (r == null) return;

        consultationReasonTF.setText(getSafeString(r.getConsultationReason()));
        diagnosisTF.setText(getSafeString(r.getDiagnosis()));
        observationsTA.setText(getSafeString(r.getObservations()));
        recommendationsTA.setText(getSafeString(r.getRecommendations()));
        treatmentsTA.setText(getSafeString(r.getTreatments()));

        if (r.getAppointmentId() != null && r.getAppointmentId() > 0) {
            for (Appointment a : rendezVousCB.getItems()) {
                if (a.getId() == r.getAppointmentId()) {
                    rendezVousCB.setValue(a);
                    break;
                }
            }
        }
    }

    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    @FXML
    public void modifierRapport(ActionEvent event) {
        ajouterRapport(event);
    }

    @FXML
    public void ajouterRapport(ActionEvent event) {
        System.out.println("RapportController: ajouterRapport clicked");
        if (!validateFields()) return;

        Rapport rapport = (currentRapport != null) ? currentRapport : new Rapport();
        try {
            populateEntityFromFields(rapport);
            if (rapport.getId() > 0) {
                rapportService.update(rapport);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport modifié !");
            } else {
                rapportService.add(rapport);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport ajouté !");
            }
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.toString());
        }
    }

    private void populateEntityFromFields(Rapport rapport) throws Exception {
        rapport.setConsultationReason(consultationReasonTF.getText());
        rapport.setDiagnosis(diagnosisTF.getText());
        rapport.setObservations(observationsTA.getText());
        rapport.setRecommendations(recommendationsTA.getText());
        rapport.setTreatments(treatmentsTA.getText());
        
        Integer patientId = UserSession.getInstance().getSelectedPatientId();
        Integer medecinId = UserSession.getInstance().getSelectedMedecinId();

        if (rendezVousCB.getValue() != null) {
            rapport.setAppointmentId(rendezVousCB.getValue().getId());
            // Important: Get patient ID from appointment if not already set
            if (patientId == null) patientId = rendezVousCB.getValue().getPatientId();
        }
        
        if (patientId == null || patientId == 0) {
            throw new Exception("Patient non identifié. Veuillez sélectionner un rendez-vous.");
        }

        rapport.setPatientId(patientId);
        rapport.setMedecinId(medecinId != null ? medecinId : 0);
        
        // ========== AUTO-LINK TO EXISTING DOCUMENT ==========
        // Check if a document exists for this patient + medecin
        try {
            com.pidev.services.DocumentService documentService = new com.pidev.services.DocumentService();
            com.pidev.models.Document existingDocument = documentService.findByPatientAndMedecin(patientId, medecinId);
            if (existingDocument != null) {
                rapport.setDocumentId(existingDocument.getId());
                System.out.println("✅ Rapport auto-lié au document #" + existingDocument.getId());
            } else {
                System.out.println("ℹ️ Aucun document existant - rapport créé sans liaison");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la vérification du document: " + e.getMessage());
            // Continue without linking - not critical
        }
    }

    private boolean validateFields() {
        if (consultationReasonTF.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La raison est obligatoire.");
            return false;
        }
        if (diagnosisTF.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le diagnostic est obligatoire.");
            return false;
        }
        return true;
    }

    @FXML
    public void annuler(ActionEvent event) {
        closeWindow();
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