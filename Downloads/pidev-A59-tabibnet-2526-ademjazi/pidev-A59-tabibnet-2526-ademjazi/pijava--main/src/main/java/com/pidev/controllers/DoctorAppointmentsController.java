package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.models.RendezVous;
import com.pidev.services.PatientService;
import com.pidev.services.RendezVousService;
import com.pidev.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for displaying doctor's appointments
 * Shows all appointments for the logged-in doctor with ability to change status
 */
public class DoctorAppointmentsController {

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label appointmentCountLabel;
    @FXML private VBox appointmentsContainer;
    @FXML private VBox emptyStateBox;

    private RendezVousService rdvService = new RendezVousService();
    private PatientService patientService = new PatientService();
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
     * Load appointments for current doctor
     */
    private void loadAppointments() {
        try {
            int doctorId = UserSession.getInstance().getUser().getId();
            appointmentsList.setAll(rdvService.getByMedecinId(doctorId));
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

        // Header with patient info
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);

        // Patient avatar
        Label avatar = new Label();
        try {
            Patient patient = patientService.getById(rdv.getPatientId());
            if (patient != null) {
                String initials = "";
                if (patient.getFirstName() != null && !patient.getFirstName().isEmpty()) {
                    initials += patient.getFirstName().charAt(0);
                }
                if (patient.getLastName() != null && !patient.getLastName().isEmpty()) {
                    initials += patient.getLastName().charAt(0);
                }
                avatar.setText(initials.toUpperCase());
            }
        } catch (Exception e) {
            avatar.setText("PT");
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

        // Patient info
        VBox patientInfo = new VBox();
        patientInfo.setSpacing(4);
        HBox.setHgrow(patientInfo, Priority.ALWAYS);

        String patientName = "Patient #" + rdv.getPatientId();
        try {
            Patient patient = patientService.getById(rdv.getPatientId());
            if (patient != null) {
                patientName = patient.getFirstName() + " " + patient.getLastName();
            }
        } catch (Exception e) {
            // Use default name
        }

        Label nameLabel = new Label(patientName);
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label dateTimeLabel = new Label("📅 " + rdv.getDate() + "  🕐 " + rdv.getHeure());
        dateTimeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        patientInfo.getChildren().addAll(nameLabel, dateTimeLabel);

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

        header.getChildren().addAll(avatar, patientInfo, statusBadge);

        // Action buttons - Status change
        HBox actionButtons = new HBox();
        actionButtons.setSpacing(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        // Mark as Completed button (only for pending)
        if ("En attente".equals(status)) {
            Button completeButton = new Button("✓ Marquer comme Terminé");
            completeButton.setStyle(
                "-fx-background-color: #22c55e;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;"
            );
            completeButton.setOnMouseEntered(e -> completeButton.setStyle(
                "-fx-background-color: #16a34a;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;"
            ));
            completeButton.setOnMouseExited(e -> completeButton.setStyle(
                "-fx-background-color: #22c55e;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;"
            ));
            completeButton.setOnAction(e -> handleChangeStatus(rdv, "Terminé"));
            actionButtons.getChildren().add(completeButton);

            // Cancel button
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
            cancelButton.setOnAction(e -> handleChangeStatus(rdv, "Annulé"));
            actionButtons.getChildren().add(cancelButton);
        }

        // Add Dossier Medical button for all (or only completed/pending)
        Button dossierButton = new Button("📂 Dossier Médical");
        dossierButton.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        dossierButton.setOnAction(e -> {
            UserSession.getInstance().setSelectedPatientId(rdv.getPatientId());
            UserSession.getInstance().setSelectedAppointmentId(rdv.getId());
            UserSession.getInstance().setSelectedMedecinId(rdv.getMedecinId());
            
            if (MainUserController.getInstance() != null) {
                MainUserController.getInstance().showOrdonnances();
            }
        });
        actionButtons.getChildren().add(dossierButton);

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
     * Handle status change
     */
    private void handleChangeStatus(RendezVous rdv, String newStatus) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer le changement de statut");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir marquer ce rendez-vous comme \"" + newStatus + "\"?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                rdvService.modifier(rdv, rdv.getDate(), rdv.getHeure(), newStatus);
                showAlert(Alert.AlertType.INFORMATION, "✓ Succès", 
                    "Statut du rendez-vous mis à jour avec succès.");
                loadAppointments();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                    "Impossible de mettre à jour le rendez-vous:\n" + e.getMessage());
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
