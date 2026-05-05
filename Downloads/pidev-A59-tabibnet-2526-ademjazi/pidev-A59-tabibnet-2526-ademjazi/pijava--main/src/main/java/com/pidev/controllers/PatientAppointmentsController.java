package com.pidev.controllers;

import com.pidev.models.Feedback;
import com.pidev.models.Medecin;
import com.pidev.models.RendezVous;
import com.pidev.services.FeedbackService;
import com.pidev.services.MedecinService;
import com.pidev.services.RendezVousService;
import com.pidev.utils.EmailService;
import com.pidev.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for displaying patient's appointments
 * Shows all appointments with options to cancel or leave feedback
 */
public class PatientAppointmentsController {

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label appointmentCountLabel;
    @FXML private VBox appointmentsContainer;
    @FXML private VBox emptyStateBox;

    private RendezVousService rdvService = new RendezVousService();
    private MedecinService medecinService = new MedecinService();
    private FeedbackService feedbackService = new FeedbackService();
    private ObservableList<RendezVous> appointmentsList = FXCollections.observableArrayList();
    private FilteredList<RendezVous> filteredList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupStatusFilter();
        loadAppointments();
    }

    /**
     * Setup status filter combo box
     */
    private void setupStatusFilter() {
        statusFilterCombo.getItems().addAll(
            "Tous les statuts",
            "En attente",
            "Terminé",
            "Annulé"
        );
        statusFilterCombo.setValue("Tous les statuts");
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Load appointments for current patient
     */
    private void loadAppointments() {
        try {
            int patientId = UserSession.getInstance().getUser().getId();
            appointmentsList.setAll(rdvService.getByPatientId(patientId));
            filteredList = new FilteredList<>(appointmentsList);
            displayAppointments();
            updateAppointmentCount();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                "Impossible de charger les rendez-vous:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply filters to appointments list
     */
    private void applyFilters() {
        String selectedStatus = statusFilterCombo.getValue();
        
        if ("Tous les statuts".equals(selectedStatus)) {
            filteredList.setPredicate(rdv -> true);
        } else {
            filteredList.setPredicate(rdv -> rdv.getStatut().equals(selectedStatus));
        }
        
        displayAppointments();
        updateAppointmentCount();
    }

    /**
     * Display appointments in the container
     */
    private void displayAppointments() {
        appointmentsContainer.getChildren().clear();
        
        if (filteredList == null || filteredList.isEmpty()) {
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
            return;
        }
        
        emptyStateBox.setVisible(false);
        emptyStateBox.setManaged(false);
        
        // Sort the underlying list by date descending (not the filtered list)
        appointmentsList.sort((a, b) -> {
            try {
                LocalDate dateA = LocalDate.parse(a.getDate(), dateFormatter);
                LocalDate dateB = LocalDate.parse(b.getDate(), dateFormatter);
                return dateB.compareTo(dateA);
            } catch (Exception e) {
                return 0;
            }
        });
        
        for (RendezVous rdv : filteredList) {
            appointmentsContainer.getChildren().add(createAppointmentCard(rdv));
        }
    }

    /**
     * Create appointment card UI
     */
    private VBox createAppointmentCard(RendezVous rdv) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setPadding(new Insets(18));
        card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3);"
        );

        // Header with doctor info
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);

        // Doctor avatar
        Label avatar = new Label();
        try {
            Medecin doctor = medecinService.getById(rdv.getMedecinId());
            if (doctor != null) {
                String initials = "";
                if (doctor.getFirstName() != null && !doctor.getFirstName().isEmpty()) {
                    initials += doctor.getFirstName().charAt(0);
                }
                if (doctor.getLastName() != null && !doctor.getLastName().isEmpty()) {
                    initials += doctor.getLastName().charAt(0);
                }
                avatar.setText(initials.toUpperCase());
            }
        } catch (Exception e) {
            avatar.setText("DR");
        }

        avatar.setStyle(
            "-fx-background-color: #22c55e;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 45px;" +
            "-fx-min-height: 45px;" +
            "-fx-max-width: 45px;" +
            "-fx-max-height: 45px;" +
            "-fx-background-radius: 22;" +
            "-fx-alignment: center;"
        );

        // Doctor info
        VBox doctorInfo = new VBox();
        doctorInfo.setSpacing(4);
        HBox.setHgrow(doctorInfo, Priority.ALWAYS);

        String doctorName = "Médecin #" + rdv.getMedecinId();
        try {
            Medecin doctor = medecinService.getById(rdv.getMedecinId());
            if (doctor != null) {
                doctorName = "Dr. " + doctor.getFullName();
            }
        } catch (Exception e) {
            // Use default name
        }

        Label nameLabel = new Label(doctorName);
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label dateTimeLabel = new Label("📅 " + rdv.getDate() + "  🕐 " + rdv.getHeure());
        dateTimeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        doctorInfo.getChildren().addAll(nameLabel, dateTimeLabel);

        // Status badge
        Label statusBadge = new Label();
        String status = rdv.getStatut();
        if ("En attente".equals(status)) {
            statusBadge.setText("⏳ En attente");
            statusBadge.setStyle(
                "-fx-background-color: rgba(245,158,11,0.2);" +
                "-fx-text-fill: #f59e0b;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(245,158,11,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;"
            );
        } else if ("Terminé".equals(status)) {
            statusBadge.setText("✓ Terminé");
            statusBadge.setStyle(
                "-fx-background-color: rgba(34,197,94,0.2);" +
                "-fx-text-fill: #22c55e;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(34,197,94,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;"
            );
        } else if ("Annulé".equals(status)) {
            statusBadge.setText("✕ Annulé");
            statusBadge.setStyle(
                "-fx-background-color: rgba(239,68,68,0.2);" +
                "-fx-text-fill: #ef4444;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(239,68,68,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;"
            );
        }

        header.getChildren().addAll(avatar, doctorInfo, statusBadge);

        // Action buttons
        HBox actionButtons = new HBox();
        actionButtons.setSpacing(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        // Cancel button (only for pending appointments)
        if ("En attente".equals(status)) {
            Button cancelButton = new Button("❌ Annuler");
            cancelButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #ef4444;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #ef4444;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;"
            );
            cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                "-fx-background-color: rgba(239,68,68,0.1);" +
                "-fx-text-fill: #ef4444;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #ef4444;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;"
            ));
            cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #ef4444;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #ef4444;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;"
            ));
            cancelButton.setOnAction(e -> handleCancelAppointment(rdv));
            actionButtons.getChildren().add(cancelButton);
        }

        // Feedback button (only for completed appointments)
        if ("Terminé".equals(status)) {
            // Check if feedback already exists
            java.util.List<Feedback> existingFeedback = feedbackService.getByRendezVous(rdv.getId());
            
            if (existingFeedback.isEmpty()) {
                // No feedback yet - show "Leave feedback" button
                Button feedbackButton = new Button("⭐ Laisser un avis");
                feedbackButton.setStyle(
                    "-fx-background-color: #8b5cf6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-width: 0;"
                );
                feedbackButton.setOnMouseEntered(e -> feedbackButton.setStyle(
                    "-fx-background-color: #7c3aed;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-width: 0;"
                ));
                feedbackButton.setOnMouseExited(e -> feedbackButton.setStyle(
                    "-fx-background-color: #8b5cf6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-width: 0;"
                ));
                feedbackButton.setOnAction(e -> handleLeaveFeedback(rdv));
                actionButtons.getChildren().add(feedbackButton);
            } else {
                // Feedback exists - show modify and delete buttons
                Feedback feedback = existingFeedback.get(0);
                
                // Modify button
                Button modifyButton = new Button("✏️ Modifier l'avis");
                modifyButton.setStyle(
                    "-fx-background-color: #3b82f6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-width: 0;"
                );
                modifyButton.setOnMouseEntered(e -> modifyButton.setStyle(
                    "-fx-background-color: #2563eb;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-width: 0;"
                ));
                modifyButton.setOnMouseExited(e -> modifyButton.setStyle(
                    "-fx-background-color: #3b82f6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-width: 0;"
                ));
                modifyButton.setOnAction(e -> handleLeaveFeedback(rdv));
                actionButtons.getChildren().add(modifyButton);
                
                // Delete button
                Button deleteButton = new Button("🗑️ Supprimer l'avis");
                deleteButton.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #ef4444;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: #ef4444;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 6;"
                );
                deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.1);" +
                    "-fx-text-fill: #ef4444;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: #ef4444;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 6;"
                ));
                deleteButton.setOnMouseExited(e -> deleteButton.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #ef4444;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-padding: 6 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: #ef4444;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 6;"
                ));
                deleteButton.setOnAction(e -> handleDeleteFeedback(feedback, rdv));
                actionButtons.getChildren().add(deleteButton);
            }
        }

        if (!actionButtons.getChildren().isEmpty()) {
            card.getChildren().addAll(header, actionButtons);
        } else {
            card.getChildren().add(header);
        }

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #1a2332;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(34,197,94,0.25), 14, 0, 0, 5);"
        ));

        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3);"
        ));

        return card;
    }

    /**
     * Handle appointment cancellation - DELETE from database
     */
    private void handleCancelAppointment(RendezVous rdv) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer l'annulation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir annuler ce rendez-vous?\nCette action est irréversible.");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                // Get doctor info for email
                Medecin doctor = medecinService.getById(rdv.getMedecinId());
                String doctorName = doctor != null ? "Dr. " + doctor.getFullName() : "Médecin";
                
                // Delete the appointment from database
                rdvService.supprimer(rdv);
                
                // Send cancellation email to patient
                String patientEmail = UserSession.getInstance().getUser().getEmail();
                String patientName = UserSession.getInstance().getUser().getFullName();
                
                EmailService.getInstance().sendAppointmentCancellation(
                    patientEmail,
                    patientName,
                    doctorName,
                    rdv.getDate(),
                    rdv.getHeure(),
                    () -> {
                        // Success callback
                        System.out.println("✅ Cancellation email sent to: " + patientEmail);
                    },
                    (error) -> {
                        // Error callback
                        System.err.println("❌ Failed to send cancellation email: " + error);
                    }
                );
                
                showAlert(Alert.AlertType.INFORMATION, "✓ Succès", 
                    "Rendez-vous annulé et supprimé avec succès.\n📧 Un email de confirmation vous a été envoyé.");
                loadAppointments();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                    "Impossible d'annuler le rendez-vous:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle feedback submission
     */
    private void handleLeaveFeedback(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FeedbackDialogView.fxml"));
            VBox feedbackView = loader.load();
            
            FeedbackDialogController feedbackController = loader.getController();
            feedbackController.setAppointment(rdv);
            
            Stage stage = new Stage();
            stage.setTitle("Laisser un Avis");
            stage.setScene(new javafx.scene.Scene(feedbackView, 500, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Refresh appointments after feedback
            loadAppointments();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                "Impossible d'ouvrir le formulaire d'avis:\n" + e.getMessage());
        }
    }

    /**
     * Handle feedback deletion
     */
    private void handleDeleteFeedback(Feedback feedback, RendezVous rdv) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet avis?\nCette action est irréversible.");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                feedbackService.supprimer(feedback);
                
                // Update doctor's rating after feedback deletion
                medecinService.updateDoctorRating(rdv.getMedecinId());
                
                showAlert(Alert.AlertType.INFORMATION, "✓ Succès", 
                    "Avis supprimé avec succès.");
                loadAppointments();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                    "Impossible de supprimer l'avis:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle refresh button
     */
    @FXML
    public void handleRefresh() {
        loadAppointments();
        showAlert(Alert.AlertType.INFORMATION, "✓ Rafraîchi", 
            "Liste des rendez-vous mise à jour.");
    }

    /**
     * Update appointment count label
     */
    private void updateAppointmentCount() {
        int count = filteredList != null ? filteredList.size() : 0;
        appointmentCountLabel.setText(count + " rendez-vous");
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
