package com.pidev.controllers;

import com.pidev.models.Appointment;
import com.pidev.models.Ordonnance;
import com.pidev.services.OrdonnanceService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrdonnanceController implements Initializable {

    @FXML
    private DatePicker dateOrdonnanceDP;
    @FXML
    private TextField diagnosisTF;
    @FXML
    private TextArea medicamentTF;
    @FXML
    private TextArea posologieTF;
    @FXML
    private TextArea notesTA;
    @FXML
    private TextArea instructionsTA;
    @FXML
    private ComboBox<Appointment> rendezVousCB;

    private OrdonnanceService ordonnanceService = new OrdonnanceService();
    private AppointmentService appointmentService = new AppointmentService();
    private Ordonnance currentOrdonnance = null;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("OrdonnanceController: initialize() called");
        loadAppointments();
        
        // Auto-select appointment from session if available
        Appointment sessionAppointment = AppointmentSessionManager.getInstance().getSelectedAppointment();
        if (sessionAppointment != null) {
            System.out.println("OrdonnanceController: Auto-selecting appointment from session: " + sessionAppointment.getId());
            rendezVousCB.setValue(sessionAppointment);
        }
    }

    private void loadAppointments() {
        try {
            Integer patientId = UserSession.getInstance().getSelectedPatientId();
            Integer medecinId = UserSession.getInstance().getSelectedMedecinId();
            System.out.println("Ordonnance: Loading appointments for Patient=" + patientId + ", Medecin=" + medecinId);
            if (medecinId != null) {
                List<Appointment> list;
                if (patientId != null && patientId > 0) {
                    list = appointmentService.getAppointmentsByPatientAndDoctor(patientId, medecinId);
                } else {
                    list = appointmentService.getAppointmentsByDoctor(medecinId);
                }
                System.out.println("Ordonnance: Found " + list.size() + " appointments");
                rendezVousCB.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            System.err.println("Error loading appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initData(Ordonnance o) {
        this.currentOrdonnance = o;
        if (o.getDateOrdonnance() != null) {
            dateOrdonnanceDP.setValue(o.getDateOrdonnance().toLocalDate());
        } else {
            dateOrdonnanceDP.setValue(java.time.LocalDate.now());
        }
        diagnosisTF.setText(o.getDiagnosis());
        medicamentTF.setText(o.getMedicament());
        posologieTF.setText(o.getPosologie());
        notesTA.setText(o.getNotes());
        instructionsTA.setText(o.getInstructions());
        
        if (o.getAppointmentId() != null && o.getAppointmentId() > 0) {
            for (Appointment a : rendezVousCB.getItems()) {
                if (a.getId() == o.getAppointmentId()) {
                    rendezVousCB.setValue(a);
                    break;
                }
            }
        }
    }

    @FXML
    public void modifierOrdonnance(ActionEvent event) {
        ajouterOrdonnance(event);
    }

    @FXML
    public void ajouterOrdonnance(ActionEvent event) {
        System.out.println("OrdonnanceController: ajouterOrdonnance clicked");
        if (!validateFields()) return;

        Ordonnance ordonnance = (currentOrdonnance != null) ? currentOrdonnance : new Ordonnance();
        try {
            populateEntityFromFields(ordonnance);
            if (ordonnance.getId() > 0) {
                ordonnanceService.update(ordonnance);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance modifiée !");
            } else {
                ordonnanceService.add(ordonnance);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance ajoutée !");
            }
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.toString());
        }
    }

    private void populateEntityFromFields(Ordonnance ordonnance) throws Exception {
        if (dateOrdonnanceDP.getValue() != null) {
            ordonnance.setDateOrdonnance(dateOrdonnanceDP.getValue().atStartOfDay());
        }
        ordonnance.setDiagnosis(diagnosisTF.getText());
        ordonnance.setMedicament(medicamentTF.getText());
        ordonnance.setPosologie(posologieTF.getText());
        ordonnance.setNotes(notesTA.getText());
        ordonnance.setInstructions(instructionsTA.getText());
        
        Integer patientId = UserSession.getInstance().getSelectedPatientId();
        Integer medecinId = UserSession.getInstance().getSelectedMedecinId();

        if (rendezVousCB.getValue() != null) {
            ordonnance.setAppointmentId(rendezVousCB.getValue().getId());
            // Important: Get patient ID from appointment if not already set
            if (patientId == null) patientId = rendezVousCB.getValue().getPatientId();
        }
        
        if (patientId == null || patientId == 0) {
            throw new Exception("Patient non identifié. Veuillez sélectionner un rendez-vous.");
        }

        ordonnance.setPatientId(patientId);
        ordonnance.setMedecinId(medecinId != null ? medecinId : 0);
        
        // ========== AUTO-LINK TO EXISTING DOCUMENT ==========
        // Check if a document exists for this patient + medecin
        try {
            com.pidev.services.DocumentService documentService = new com.pidev.services.DocumentService();
            com.pidev.models.Document existingDocument = documentService.findByPatientAndMedecin(patientId, medecinId);
            if (existingDocument != null) {
                ordonnance.setDocumentId(existingDocument.getId());
                System.out.println("✅ Ordonnance auto-liée au document #" + existingDocument.getId());
            } else {
                System.out.println("ℹ️ Aucun document existant - ordonnance créée sans liaison");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la vérification du document: " + e.getMessage());
            // Continue without linking - not critical
        }
    }

    private boolean validateFields() {
        if (dateOrdonnanceDP.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date est obligatoire.");
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