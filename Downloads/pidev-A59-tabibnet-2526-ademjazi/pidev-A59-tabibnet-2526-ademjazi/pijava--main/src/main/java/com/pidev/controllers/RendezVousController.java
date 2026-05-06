package com.pidev.controllers;

import com.pidev.models.RendezVous;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.RendezVousService;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import com.pidev.utils.AppointmentSessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Unified Controller for RendezVous (Appointments) operations.
 * Combines features from the legacy RendezVousController and AppointmentController.
 */
public class RendezVousController {

    // ── Table Components ───────────────────────────────────────────────────
    @FXML private TableView<RendezVous> tableAppointments;
    @FXML private TableView<RendezVous> tableView; // Legacy alias
    
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String> colPatient;
    @FXML private TableColumn<RendezVous, String> colNom; // Legacy alias
    @FXML private TableColumn<RendezVous, String> colDoctor;
    @FXML private TableColumn<RendezVous, String> colMedecin; // Legacy alias
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure; // Legacy alias
    @FXML private TableColumn<RendezVous, Integer> colDuration;
    @FXML private TableColumn<RendezVous, String> colStatus;
    @FXML private TableColumn<RendezVous, String> colStatut; // Legacy alias
    @FXML private TableColumn<RendezVous, String> colDepartment;

    // ── Form Components ────────────────────────────────────────────────────
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private ComboBox<Medecin> cbDoctor;
    @FXML private ComboBox<String> medecinCombo; // Legacy alias
    @FXML private DatePicker dpDate;
    @FXML private TextField dateField; // Legacy alias
    @FXML private TextField tfStartTime, heureField; // Legacy heureField alias
    @FXML private TextField tfDuration;
    @FXML private TextField nomField; // Legacy alias for patient name
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cbDepartment, cbStatus, statutCombo; // Legacy statutCombo alias
    @FXML private TextArea taMessage;
    @FXML private Label totalLabel;

    private final RendezVousService appointmentService = new RendezVousService();
    private final PatientService patientService = new PatientService();
    private final MedecinService medecinService = new MedecinService();
    private ObservableList<RendezVous> appointmentList = FXCollections.observableArrayList();
    private RendezVous selectedAppointment = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRowFactory();
        loadAppointments();
        loadPatientCombo();
        loadDoctorCombo();
        
        if (statutCombo != null && statutCombo.getItems().isEmpty()) {
            statutCombo.getItems().addAll("En attente", "Terminé", "Annulé");
        }

        // Handle selection for both potential table IDs
        TableView<RendezVous> activeTable = (tableAppointments != null) ? tableAppointments : tableView;
        if (activeTable != null) {
            activeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedAppointment = newVal;
                    AppointmentSessionManager.getInstance().setSelectedAppointment(newVal);
                    fillForm(newVal);
                    activeTable.refresh();
                } else {
                    selectedAppointment = null;
                }
            });
        }
    }

    private void setupRowFactory() {
        TableView<RendezVous> activeTable = (tableAppointments != null) ? tableAppointments : tableView;
        if (activeTable == null) return;

        activeTable.setRowFactory(tv -> new TableRow<RendezVous>() {
            @Override
            protected void updateItem(RendezVous item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setText(null);
                } else {
                    if (isSelected()) {
                        setStyle("-fx-background-color: #ff0000; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupTableColumns() {
        if (colId != null) colId.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getId()));
        
        // Handle Patient Column (both variants)
        if (colPatient != null) colPatient.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getNomPatient() != null ? cdf.getValue().getNomPatient() : "ID:" + cdf.getValue().getPatientId()));
        if (colNom != null) colNom.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getNomPatient() != null ? cdf.getValue().getNomPatient() : "ID:" + cdf.getValue().getPatientId()));

        // Handle Doctor Column (both variants)
        if (colDoctor != null) colDoctor.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getMedecin() != null ? cdf.getValue().getMedecin() : "ID:" + cdf.getValue().getMedecinId()));
        if (colMedecin != null) colMedecin.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getMedecin() != null ? cdf.getValue().getMedecin() : "ID:" + cdf.getValue().getMedecinId()));
        
        // Handle Date/Time Columns
        if (colDate != null) colDate.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getDate() + (cdf.getValue().getHeure() != null ? " " + cdf.getValue().getHeure() : "")));
        if (colHeure != null) colHeure.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getHeure()));

        if (colDuration != null) colDuration.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getDurationMinutes()));
        
        // Handle Status Column
        if (colStatus != null) colStatus.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getStatut()));
        if (colStatut != null) colStatut.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getStatut()));
        
        if (colDepartment != null) colDepartment.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getDepartment()));
    }

    private void loadAppointments() {
        try {
            appointmentList.setAll(appointmentService.getAll());
            if (tableAppointments != null) tableAppointments.setItems(appointmentList);
            if (tableView != null) tableView.setItems(appointmentList);
            if (totalLabel != null) totalLabel.setText("Total: " + appointmentList.size());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les RDV: " + e.getMessage());
        }
    }

    private void loadPatientCombo() {
        if (cbPatient == null) return;
        try {
            List<Patient> patients = patientService.getAll();
            cbPatient.setItems(FXCollections.observableArrayList(patients));
            cbPatient.setConverter(new StringConverter<>() {
                @Override public String toString(Patient p) {
                    return p == null ? "" : p.getFullName() + " (ID:" + p.getId() + ")";
                }
                @Override public Patient fromString(String s) { return null; }
            });
        } catch (Exception e) {
            System.err.println("Cannot load patients: " + e.getMessage());
        }
    }

    private void loadDoctorCombo() {
        if (cbDoctor == null) return;
        try {
            List<Medecin> medecins = medecinService.getAll();
            cbDoctor.setItems(FXCollections.observableArrayList(medecins));
            cbDoctor.setConverter(new StringConverter<>() {
                @Override public String toString(Medecin m) {
                    return m == null ? "" : "Dr. " + m.getFullName() + " (" + m.getSpecialty() + ")";
                }
                @Override public Medecin fromString(String s) { return null; }
            });
        } catch (Exception e) {
            System.err.println("Cannot load doctors: " + e.getMessage());
        }
    }

    private void fillForm(RendezVous a) {
        if (nomField != null) nomField.setText(a.getNomPatient());
        
        if (cbPatient != null) {
            for (Patient p : cbPatient.getItems()) {
                if (p.getId() == a.getPatientId()) { cbPatient.setValue(p); break; }
            }
        }
        
        if (cbDoctor != null) {
            for (Medecin m : cbDoctor.getItems()) {
                if (m.getId() == a.getMedecinId()) { cbDoctor.setValue(m); break; }
            }
        } else if (medecinCombo != null) {
            medecinCombo.setValue(a.getMedecin());
        }

        if (dpDate != null) dpDate.setValue(a.getLocalDate());
        if (dateField != null) dateField.setText(a.getDate());
        
        if (tfStartTime != null) tfStartTime.setText(a.getHeure());
        if (heureField != null) heureField.setText(a.getHeure());
        
        if (tfDuration != null) tfDuration.setText(String.valueOf(a.getDurationMinutes()));
        if (cbDepartment != null) cbDepartment.setValue(a.getDepartment());
        
        if (cbStatus != null) cbStatus.setValue(a.getStatut());
        if (statutCombo != null) statutCombo.setValue(a.getStatut());
        
        if (taMessage != null) taMessage.setText(a.getMessage());
    }

    // ── Unified CRUD Actions ───────────────────────────────────────────────

    @FXML public void addAppointment() { handleAjouter(); }
    @FXML public void handleAjouter() {
        if (!validateForm()) return;
        try {
            RendezVous a = buildAppointmentFromForm();
            appointmentService.add(a);
            loadAppointments();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous ajouté !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML public void updateAppointment() { handleModifier(); }
    @FXML public void handleModifier() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un RDV.");
            return;
        }
        if (!validateForm()) return;
        try {
            RendezVous a = buildAppointmentFromForm();
            a.setId(selectedAppointment.getId());
            appointmentService.update(a);
            loadAppointments();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "RDV modifié !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML public void deleteAppointment() { handleSupprimer(); }
    @FXML public void handleSupprimer() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un RDV.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le RDV #" + selectedAppointment.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES || response == ButtonType.OK) {
                try {
                    appointmentService.delete(selectedAppointment.getId());
                    loadAppointments();
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "RDV supprimé !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML public void clearForm() { handleReset(); }
    @FXML public void handleReset() {
        if (cbPatient != null) cbPatient.setValue(null);
        if (cbDoctor != null) cbDoctor.setValue(null);
        if (medecinCombo != null) medecinCombo.setValue(null);
        if (dpDate != null) dpDate.setValue(null);
        if (dateField != null) dateField.clear();
        if (tfStartTime != null) tfStartTime.clear();
        if (heureField != null) heureField.clear();
        if (tfDuration != null) tfDuration.clear();
        if (nomField != null) nomField.clear();
        if (cbDepartment != null) cbDepartment.setValue(null);
        if (cbStatus != null) cbStatus.setValue(null);
        if (statutCombo != null) statutCombo.setValue(null);
        if (taMessage != null) taMessage.clear();
        selectedAppointment = null;
        if (tableAppointments != null) tableAppointments.getSelectionModel().clearSelection();
        if (tableView != null) tableView.getSelectionModel().clearSelection();
    }

    @FXML public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            if (tableAppointments != null) tableAppointments.setItems(appointmentList);
            if (tableView != null) tableView.setItems(appointmentList);
        } else {
            FilteredList<RendezVous> filtered = appointmentList.filtered(a ->
                    (a.getNomPatient() != null && a.getNomPatient().toLowerCase().contains(query)) ||
                    (a.getMedecin() != null && a.getMedecin().toLowerCase().contains(query)) ||
                    (a.getStatut() != null && a.getStatut().toLowerCase().contains(query)) ||
                    (a.getDepartment() != null && (a.getDepartment() != null && a.getDepartment().toLowerCase().contains(query)))
            );
            if (tableAppointments != null) tableAppointments.setItems(filtered);
            if (tableView != null) tableView.setItems(filtered);
        }
    }

    @FXML
    public void openFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/feedback.fxml"));
            Parent root = loader.load();
            // FeedbackController fc = loader.getController(); // Assuming FeedbackController is in right package
            Stage stage = new Stage();
            stage.setTitle("Feedbacks");
            stage.setScene(new Scene(root, 650, 480));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface Feedback.");
        }
    }

    private RendezVous buildAppointmentFromForm() {
        RendezVous a = new RendezVous();
        
        // Handle Patient
        if (cbPatient != null && cbPatient.getValue() != null) {
            a.setPatientId(cbPatient.getValue().getId());
            a.setNomPatient(cbPatient.getValue().getFullName());
        } else if (nomField != null) {
            a.setNomPatient(nomField.getText().trim());
        }

        // Handle Doctor
        if (cbDoctor != null && cbDoctor.getValue() != null) {
            a.setMedecinId(cbDoctor.getValue().getId());
            a.setMedecin(cbDoctor.getValue().getFullName());
        } else if (medecinCombo != null && medecinCombo.getValue() != null) {
            a.setMedecin(medecinCombo.getValue());
        }

        // Handle Date
        if (dpDate != null && dpDate.getValue() != null) {
            a.setLocalDate(dpDate.getValue());
        } else if (dateField != null) {
            a.setDate(dateField.getText().trim());
        }
        
        // Handle Time
        if (tfStartTime != null) a.setHeure(tfStartTime.getText().trim());
        else if (heureField != null) a.setHeure(heureField.getText().trim());

        if (tfDuration != null && !tfDuration.getText().isEmpty()) 
            a.setDurationMinutes(Integer.parseInt(tfDuration.getText().trim()));
        
        if (cbDepartment != null) a.setDepartment(cbDepartment.getValue());
        
        // Handle Status
        if (cbStatus != null && cbStatus.getValue() != null) a.setStatut(cbStatus.getValue());
        else if (statutCombo != null && statutCombo.getValue() != null) a.setStatut(statutCombo.getValue());
        else a.setStatut("En attente");

        if (taMessage != null) a.setMessage(taMessage.getText());
        
        return a;
    }

    private boolean validateForm() {
        boolean hasPatient = (cbPatient != null && cbPatient.getValue() != null) || (nomField != null && !nomField.getText().trim().isEmpty());
        boolean hasDoctor = (cbDoctor != null && cbDoctor.getValue() != null) || (medecinCombo != null && medecinCombo.getValue() != null);
        boolean hasDate = (dpDate != null && dpDate.getValue() != null) || (dateField != null && !dateField.getText().trim().isEmpty());

        if (!hasPatient || !hasDoctor) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Patient et Médecin sont obligatoires.");
            return false;
        }
        if (!hasDate) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date est obligatoire.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
