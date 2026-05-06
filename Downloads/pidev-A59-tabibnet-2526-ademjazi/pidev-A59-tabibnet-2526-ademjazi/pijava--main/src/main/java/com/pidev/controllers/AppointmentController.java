package com.pidev.controllers;

import com.pidev.models.Appointment;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.AppointmentService;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import com.pidev.utils.AppointmentSessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Appointment CRUD operations.
 */
public class AppointmentController {

    @FXML private TableView<Appointment> tableAppointments;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, java.time.LocalDateTime> colDate;
    @FXML private TableColumn<Appointment, Integer> colDuration;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, String> colDepartment;

    @FXML private ComboBox<Patient> cbPatient;
    @FXML private ComboBox<Medecin> cbDoctor;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfStartTime, tfDuration, searchField;
    @FXML private ComboBox<String> cbDepartment, cbStatus;
    @FXML private TextArea taMessage;

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();
    private final MedecinService medecinService = new MedecinService();
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private Appointment selectedAppointment = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRowFactory();
        loadAppointments();
        loadPatientCombo();
        loadDoctorCombo();

        // Add row selection listener with red highlight
        tableAppointments.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedAppointment = newVal;
                // Store in global session manager
                AppointmentSessionManager.getInstance().setSelectedAppointment(newVal);
                fillForm(newVal);
                // Refresh the table to apply row styling
                tableAppointments.refresh();
            } else {
                selectedAppointment = null;
            }
        });
    }

    private void setupRowFactory() {
        tableAppointments.setRowFactory(tv -> new TableRow<Appointment>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setText(null);
                } else {
                    // Check if this row is selected
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
        colId.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getId()));
        colPatient.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getPatientName() != null ? cdf.getValue().getPatientName() : "Inconnu"
        ));
        colDoctor.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(
                cdf.getValue().getDoctorName() != null ? cdf.getValue().getDoctorName() : "Inconnu"
        ));
        
        // Custom formatting for the Date column
        colDate.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getDate()));
        colDate.setCellFactory(column -> new TableCell<Appointment, java.time.LocalDateTime>() {
            @Override
            protected void updateItem(java.time.LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });

        colDuration.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getDuration()));
        colStatus.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getStatus()));
        colDepartment.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getDepartment()));
    }

    private void loadAppointments() {
        try {
            appointmentList.setAll(appointmentService.getAll());
            tableAppointments.setItems(appointmentList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les RDV: " + e.getMessage());
        }
    }

    private void loadPatientCombo() {
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

    private void fillForm(Appointment a) {
        // Select patient in combo
        for (Patient p : cbPatient.getItems()) {
            if (p.getId() == a.getPatientId()) { cbPatient.setValue(p); break; }
        }
        // Select doctor in combo
        for (Medecin m : cbDoctor.getItems()) {
            if (m.getId() == a.getDoctorId()) { cbDoctor.setValue(m); break; }
        }
        if (a.getDate() != null) dpDate.setValue(a.getDate().toLocalDate());
        if (a.getStartTime() != null) tfStartTime.setText(a.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        tfDuration.setText(String.valueOf(a.getDuration()));
        cbDepartment.setValue(a.getDepartment());
        cbStatus.setValue(a.getStatus());
        taMessage.setText(a.getMessage());
    }

    @FXML
    public void addAppointment() {
        if (!validateForm()) return;

        try {
            Appointment a = buildAppointmentFromForm();
            appointmentService.add(a);
            loadAppointments();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous ajouté !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    public void updateAppointment() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un RDV.");
            return;
        }
        if (!validateForm()) return;

        try {
            Appointment a = buildAppointmentFromForm();
            a.setId(selectedAppointment.getId());
            appointmentService.update(a);
            loadAppointments();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "RDV modifié !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void deleteAppointment() {
        if (selectedAppointment == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un RDV.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le RDV #" + selectedAppointment.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
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

    @FXML
    public void clearForm() {
        cbPatient.setValue(null); cbDoctor.setValue(null);
        dpDate.setValue(null); tfStartTime.clear(); tfDuration.clear();
        cbDepartment.setValue(null); cbStatus.setValue(null); taMessage.clear();
        selectedAppointment = null;
        tableAppointments.getSelectionModel().clearSelection();
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            tableAppointments.setItems(appointmentList);
        } else {
            FilteredList<Appointment> filtered = appointmentList.filtered(a ->
                    (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(query)) ||
                    (a.getDoctorName() != null && a.getDoctorName().toLowerCase().contains(query)) ||
                    (a.getStatus() != null && a.getStatus().toLowerCase().contains(query)) ||
                    (a.getDepartment() != null && a.getDepartment().toLowerCase().contains(query))
            );
            tableAppointments.setItems(filtered);
        }
    }

    private Appointment buildAppointmentFromForm() {
        Appointment a = new Appointment();
        Patient selectedPatient = cbPatient.getValue();
        Medecin selectedDoctor = cbDoctor.getValue();
        if (selectedPatient != null) a.setPatientId(selectedPatient.getId());
        if (selectedDoctor != null) a.setDoctorId(selectedDoctor.getId());

        LocalDate date = dpDate.getValue();
        if (date != null) {
            a.setDate(date.atStartOfDay());
            // Parse start time
            try {
                String[] timeParts = tfStartTime.getText().trim().split(":");
                LocalTime time = LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                a.setStartTime(date.atTime(time));
            } catch (Exception e) {
                a.setStartTime(date.atStartOfDay());
            }
        }

        a.setDuration(tfDuration.getText().isEmpty() ? 30 : Integer.parseInt(tfDuration.getText().trim()));
        a.setDepartment(cbDepartment.getValue());
        a.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : "pending");
        a.setMessage(taMessage.getText());
        return a;
    }

    private boolean validateForm() {
        if (cbPatient.getValue() == null || cbDoctor.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Patient et Médecin sont obligatoires.");
            return false;
        }
        if (dpDate.getValue() == null) {
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
