package com.pidev.controllers;

import com.pidev.models.Feedback;
import com.pidev.models.Medecin;
import com.pidev.models.RendezVous;
import com.pidev.services.FeedbackService;
import com.pidev.services.MedecinService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for feedback dialog
 * Allows patients to leave feedback for completed appointments
 */
public class FeedbackDialogController {

    @FXML private Label doctorNameLabel;
    @FXML private Label appointmentDateLabel;
    @FXML private RadioButton rating1, rating2, rating3, rating4, rating5;
    @FXML private TextArea commentArea;
    @FXML private Label ratingErrorLabel;
    @FXML private Label commentErrorLabel;
    @FXML private ToggleGroup ratingGroup;

    private RendezVous appointment;
    private FeedbackService feedbackService = new FeedbackService();
    private MedecinService medecinService = new MedecinService();
    private Feedback existingFeedback;

    @FXML
    public void initialize() {
        // Clear error labels when user interacts
        if (ratingGroup != null) {
            ratingGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                ratingErrorLabel.setText("");
            });
        }

        commentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            commentErrorLabel.setText("");
        });
    }

    /**
     * Set the appointment for this feedback
     */
    public void setAppointment(RendezVous rdv) {
        this.appointment = rdv;
        
        // Load doctor info
        try {
            Medecin doctor = medecinService.getById(rdv.getMedecinId());
            if (doctor != null) {
                doctorNameLabel.setText("Dr. " + doctor.getFullName());
            }
        } catch (Exception e) {
            doctorNameLabel.setText("Médecin #" + rdv.getMedecinId());
        }
        
        // Set appointment date and time
        appointmentDateLabel.setText("📅 " + rdv.getDate() + "  🕐 " + rdv.getHeure());
        
        // Check if feedback already exists for this appointment
        loadExistingFeedback();
    }

    /**
     * Load existing feedback if it exists
     */
    private void loadExistingFeedback() {
        try {
            java.util.List<Feedback> feedbackList = feedbackService.getByRendezVous(appointment.getId());
            if (!feedbackList.isEmpty()) {
                existingFeedback = feedbackList.get(0);
                // Populate form with existing feedback
                commentArea.setText(existingFeedback.getCommentaire());
                
                // Set rating
                int note = existingFeedback.getNote();
                switch (note) {
                    case 1: rating1.setSelected(true); break;
                    case 2: rating2.setSelected(true); break;
                    case 3: rating3.setSelected(true); break;
                    case 4: rating4.setSelected(true); break;
                    case 5: rating5.setSelected(true); break;
                }
            }
        } catch (Exception e) {
            System.out.println("No existing feedback found");
        }
    }

    /**
     * Handle feedback submission
     */
    @FXML
    public void handleSubmit() {
        // Clear previous errors
        ratingErrorLabel.setText("");
        commentErrorLabel.setText("");

        // Validate rating
        if (ratingGroup.getSelectedToggle() == null) {
            ratingErrorLabel.setText("⚠️ Veuillez sélectionner une note.");
            return;
        }

        // Get rating value
        int rating = 0;
        if (rating1.isSelected()) rating = 1;
        else if (rating2.isSelected()) rating = 2;
        else if (rating3.isSelected()) rating = 3;
        else if (rating4.isSelected()) rating = 4;
        else if (rating5.isSelected()) rating = 5;

        // Validate comment
        String comment = commentArea.getText().trim();
        if (comment.isEmpty()) {
            commentErrorLabel.setText("⚠️ Veuillez écrire un commentaire.");
            return;
        }

        if (comment.length() < 5) {
            commentErrorLabel.setText("⚠️ Le commentaire doit contenir au moins 5 caractères.");
            return;
        }

        if (comment.length() > 1000) {
            commentErrorLabel.setText("⚠️ Le commentaire ne peut pas dépasser 1000 caractères.");
            return;
        }

        try {
            if (existingFeedback != null) {
                // Update existing feedback
                feedbackService.modifier(existingFeedback, comment, rating, appointment.getId());
                
                // Update doctor's rating
                medecinService.updateDoctorRating(appointment.getMedecinId());
                
                showAlert(Alert.AlertType.INFORMATION, "✓ Succès", 
                    "Avis mis à jour avec succès!");
            } else {
                // Create new feedback
                Feedback feedback = new Feedback();
                feedback.setRendezVousId(appointment.getId());
                feedback.setCommentaire(comment);
                feedback.setNote(rating);
                feedbackService.ajouter(feedback);
                
                // Update doctor's rating
                medecinService.updateDoctorRating(appointment.getMedecinId());
                
                showAlert(Alert.AlertType.INFORMATION, "✓ Succès", 
                    "Avis soumis avec succès!");
            }

            // Close dialog
            closeDialog();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                "Erreur lors de la soumission de l'avis:\n" + e.getMessage());
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
        Stage stage = (Stage) commentArea.getScene().getWindow();
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
