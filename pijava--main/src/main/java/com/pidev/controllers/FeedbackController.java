package com.pidev.controllers;

import com.pidev.models.Appointment;
import com.pidev.models.Feedback;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.AppointmentService;
import com.pidev.services.FeedbackService;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Controller for Feedback CRUD operations.
 */
public class FeedbackController {

    @FXML private TableView<Feedback> tableFeedbacks;
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private ComboBox<Medecin> cbDoctor;
    @FXML private ComboBox<Appointment> cbAppointment;
    @FXML private ComboBox<String> cbRating;
    @FXML private TextArea taComment;
    @FXML private Label lblStars;
    @FXML private TextField searchField;

    private final FeedbackService feedbackService = new FeedbackService();
    private final PatientService patientService = new PatientService();
    private final MedecinService medecinService = new MedecinService();
    private final AppointmentService appointmentService = new AppointmentService();
    private ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();
    private Feedback selectedFeedback = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadFeedbacks();
        loadCombos();

        // Star display on rating change
        cbRating.setOnAction(e -> {
            String val = cbRating.getValue();
            if (val != null) {
                int rating = Integer.parseInt(val);
                lblStars.setText("⭐".repeat(rating) + "☆".repeat(5 - rating));
            }
        });

        tableFeedbacks.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedFeedback = newVal;
                fillForm(newVal);
            }
        });
    }

    private void setupTableColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<Feedback, Integer> idCol = (TableColumn<Feedback, Integer>) tableFeedbacks.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<Feedback, String> patientCol = (TableColumn<Feedback, String>) tableFeedbacks.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<Feedback, String> doctorCol = (TableColumn<Feedback, String>) tableFeedbacks.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<Feedback, Integer> ratingCol = (TableColumn<Feedback, Integer>) tableFeedbacks.getColumns().get(3);
        @SuppressWarnings("unchecked")
        TableColumn<Feedback, String> commentCol = (TableColumn<Feedback, String>) tableFeedbacks.getColumns().get(4);

        idCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getId()));
        patientCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getPatientName()));
        doctorCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getMedecinName()));
        ratingCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getRating()));
        commentCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getComment()));
    }

    private void loadFeedbacks() {
        try {
            feedbackList.setAll(feedbackService.getAll());
            tableFeedbacks.setItems(feedbackList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les feedbacks: " + e.getMessage());
        }
    }

    private void loadCombos() {
        try {
            List<Patient> patients = patientService.getAll();
            cbPatient.setItems(FXCollections.observableArrayList(patients));
            cbPatient.setConverter(new StringConverter<>() {
                @Override public String toString(Patient p) {
                    return p == null ? "" : p.getFullName() + " (ID:" + p.getId() + ")";
                }
                @Override public Patient fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}

        try {
            List<Medecin> medecins = medecinService.getAll();
            cbDoctor.setItems(FXCollections.observableArrayList(medecins));
            cbDoctor.setConverter(new StringConverter<>() {
                @Override public String toString(Medecin m) {
                    return m == null ? "" : "Dr. " + m.getFullName();
                }
                @Override public Medecin fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}

        try {
            List<Appointment> appointments = appointmentService.getAll();
            cbAppointment.setItems(FXCollections.observableArrayList(appointments));
            cbAppointment.setConverter(new StringConverter<>() {
                @Override public String toString(Appointment a) {
                    return a == null ? "" : "RDV #" + a.getId() + " - " + a.getDate();
                }
                @Override public Appointment fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}
    }

    private void fillForm(Feedback f) {
        for (Patient p : cbPatient.getItems()) {
            if (p.getId() == f.getPatientId()) { cbPatient.setValue(p); break; }
        }
        for (Medecin m : cbDoctor.getItems()) {
            if (m.getId() == f.getMedecinId()) { cbDoctor.setValue(m); break; }
        }
        for (Appointment a : cbAppointment.getItems()) {
            if (a.getId() == f.getAppointmentId()) { cbAppointment.setValue(a); break; }
        }
        cbRating.setValue(String.valueOf(f.getRating()));
        int r = f.getRating();
        lblStars.setText("⭐".repeat(Math.min(r, 5)) + "☆".repeat(Math.max(5 - r, 0)));
        taComment.setText(f.getComment());
    }

    @FXML
    public void addFeedback() {
        if (!validateForm()) return;

        try {
            Feedback f = buildFeedbackFromForm();
            feedbackService.add(f);
            loadFeedbacks();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Feedback ajouté !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void updateFeedback() {
        if (selectedFeedback == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un feedback.");
            return;
        }
        if (!validateForm()) return;

        try {
            Feedback f = buildFeedbackFromForm();
            f.setId(selectedFeedback.getId());
            feedbackService.update(f);
            loadFeedbacks();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Feedback modifié !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void deleteFeedback() {
        if (selectedFeedback == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un feedback.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le feedback #" + selectedFeedback.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    feedbackService.delete(selectedFeedback.getId());
                    loadFeedbacks();
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Feedback supprimé !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void clearForm() {
        cbPatient.setValue(null); cbDoctor.setValue(null);
        cbAppointment.setValue(null); cbRating.setValue(null);
        taComment.clear(); lblStars.setText("⭐⭐⭐⭐⭐");
        selectedFeedback = null;
        tableFeedbacks.getSelectionModel().clearSelection();
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            tableFeedbacks.setItems(feedbackList);
        } else {
            FilteredList<Feedback> filtered = feedbackList.filtered(f ->
                    (f.getPatientName() != null && f.getPatientName().toLowerCase().contains(query)) ||
                    (f.getMedecinName() != null && f.getMedecinName().toLowerCase().contains(query)) ||
                    (f.getComment() != null && f.getComment().toLowerCase().contains(query))
            );
            tableFeedbacks.setItems(filtered);
        }
    }

    private Feedback buildFeedbackFromForm() {
        Feedback f = new Feedback();
        Patient p = cbPatient.getValue();
        Medecin m = cbDoctor.getValue();
        Appointment a = cbAppointment.getValue();
        if (p != null) f.setPatientId(p.getId());
        if (m != null) f.setMedecinId(m.getId());
        if (a != null) f.setAppointmentId(a.getId());
        f.setRating(cbRating.getValue() != null ? Integer.parseInt(cbRating.getValue()) : 0);
        f.setComment(taComment.getText());
        return f;
    }

    private boolean validateForm() {
        if (cbPatient.getValue() == null || cbDoctor.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Patient et Médecin sont obligatoires.");
            return false;
        }
        if (cbRating.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La note est obligatoire.");
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
