package com.pidev.controllers;

import com.pidev.models.Medecin;
import com.pidev.models.RendezVous;
import com.pidev.services.RendezVousService;
import com.pidev.utils.EmailService;
import com.pidev.utils.UserSession;
import com.pidev.utils.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for appointment booking dialog
 * Allows patients to book appointments with doctors
 */
public class AppointmentBookingController {

    @FXML private Label doctorNameLabel;
    @FXML private Label doctorSpecialtyLabel;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> appointmentTimeCombo;
    @FXML private TextArea notesArea;
    @FXML private Label dateErrorLabel;
    @FXML private Label timeErrorLabel;

    private Medecin selectedDoctor;
    private RendezVousService rdvService = new RendezVousService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Setup time combo with available times (30-minute intervals)
        appointmentTimeCombo.getItems().addAll(
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30"
        );

        // Set minimum date to today
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Clear error labels when user interacts with fields
        appointmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateErrorLabel.setText("");
        });

        appointmentTimeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeErrorLabel.setText("");
        });
    }

    /**
     * Set the selected doctor for this appointment
     */
    public void setDoctor(Medecin doctor) {
        this.selectedDoctor = doctor;
        doctorNameLabel.setText("Dr. " + doctor.getFullName());
        doctorSpecialtyLabel.setText(doctor.getSpecialty() != null ? 
            doctor.getSpecialty().getDisplayName() : "Spécialité non spécifiée");
    }

    /**
     * Handle appointment confirmation
     */
    @FXML
    public void handleConfirm() {
        // Clear previous errors
        dateErrorLabel.setText("");
        timeErrorLabel.setText("");

        // Validate date
        if (appointmentDatePicker.getValue() == null) {
            dateErrorLabel.setText("⚠️ Veuillez sélectionner une date.");
            return;
        }

        LocalDate selectedDate = appointmentDatePicker.getValue();
        if (selectedDate.isBefore(LocalDate.now())) {
            dateErrorLabel.setText("⚠️ La date ne peut pas être dans le passé.");
            return;
        }

        // Validate time
        if (appointmentTimeCombo.getValue() == null || appointmentTimeCombo.getValue().isEmpty()) {
            timeErrorLabel.setText("⚠️ Veuillez sélectionner une heure.");
            return;
        }

        try {
            // Get current patient from session
            int patientId = UserSession.getInstance().getUser().getId();

            // Format date as dd/MM/yyyy
            String date = selectedDate.format(dateFormatter);
            String time = appointmentTimeCombo.getValue();

            // Validate time format
            if (!Validator.isValidTime(time)) {
                timeErrorLabel.setText("⚠️ Format d'heure invalide.");
                return;
            }

            // Create RendezVous object
            RendezVous rdv = new RendezVous();
            rdv.setPatientId(patientId);
            rdv.setMedecinId(selectedDoctor.getId());
            rdv.setDate(date);
            rdv.setHeure(time);
            rdv.setStatut("En attente");

            // Save to database using RendezVousService
            rdvService.ajouter(rdv);

            // Send confirmation email to patient
            String patientEmail = UserSession.getInstance().getUser().getEmail();
            String patientName = UserSession.getInstance().getUser().getFullName();
            
            EmailService.getInstance().sendAppointmentConfirmation(
                patientEmail,
                patientName,
                "Dr. " + selectedDoctor.getFullName(),
                date,
                time,
                () -> {
                    // Success callback - email sent successfully
                    System.out.println("✅ Confirmation email sent to: " + patientEmail);
                },
                (error) -> {
                    // Error callback - email failed to send
                    System.err.println("❌ Failed to send confirmation email: " + error);
                    // Don't show error to user - appointment is still created
                }
            );

            showAlert(Alert.AlertType.INFORMATION, "✓ Succès", 
                "Rendez-vous confirmé!\n\n" +
                "Dr. " + selectedDoctor.getFullName() + "\n" +
                "📅 " + date + "\n" +
                "🕐 " + time + "\n\n" +
                "Statut: En attente de confirmation\n" +
                "📧 Un email de confirmation vous a été envoyé.");

            // Close dialog
            closeDialog();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                "Erreur lors de la création du rendez-vous:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle dialog cancellation
     */
    @FXML
    public void handleCancel() {
        closeDialog();
    }

    /**
     * Close the dialog window
     */
    private void closeDialog() {
        Stage stage = (Stage) appointmentDatePicker.getScene().getWindow();
        stage.close();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
