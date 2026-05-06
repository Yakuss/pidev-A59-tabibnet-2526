package com.pidev.controllers;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;
import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import com.pidev.services.ExportService;
import com.pidev.services.DoctorAPIService;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
import javafx.stage.Stage;

/**
 * Controller for Medecin CRUD operations with elegant card-based ListView.
 */
public class MedecinController {

    @FXML private ListView<Medecin> listMedecins;
    @FXML private TextField tfFirstName, tfLastName, tfEmail, tfPhone, tfCin,
            tfAddress, tfAge, tfEducation, tfExperience, searchField;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbGender;
    @FXML private ComboBox<Specialty> cbSpecialty;
    @FXML private ComboBox<Governorate> cbGovernorate;
    @FXML private CheckBox cbVerified;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnCancel;
    @FXML private Label formTitle;
    @FXML private VBox passwordSection;
    
    // Search filter fields
    @FXML private TextField searchName;
    @FXML private ComboBox<Specialty> searchSpecialty;
    @FXML private ComboBox<Governorate> searchGovernorate;

    private final MedecinService medecinService = new MedecinService();
    private final ExportService exportService = new ExportService();
    private ObservableList<Medecin> medecinList = FXCollections.observableArrayList();
    private Medecin selectedMedecin = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Populate enum ComboBoxes
        cbSpecialty.setItems(FXCollections.observableArrayList(Specialty.values()));
        cbGovernorate.setItems(FXCollections.observableArrayList(Governorate.values()));
        
        // Populate search filter ComboBoxes
        setupSearchFilters();

        setupMedecinCards();
        loadMedecins();

        listMedecins.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMedecin = newVal;
                fillForm(newVal);
                setEditMode(true);
            } else {
                selectedMedecin = null;
                setEditMode(false);
            }
        });
        
        // Initialize in add mode
        setEditMode(false);
    }

    private void setupSearchFilters() {
        // Add "Toutes" option for specialty filter
        ObservableList<Specialty> specialtyOptions = FXCollections.observableArrayList();
        specialtyOptions.add(null); // This will represent "Toutes les spécialités"
        specialtyOptions.addAll(Specialty.values());
        searchSpecialty.setItems(specialtyOptions);
        
        // Add "Tous" option for governorate filter
        ObservableList<Governorate> governorateOptions = FXCollections.observableArrayList();
        governorateOptions.add(null); // This will represent "Tous les gouvernorats"
        governorateOptions.addAll(Governorate.values());
        searchGovernorate.setItems(governorateOptions);
        
        // Custom cell factory to show "Toutes les spécialités" for null value
        searchSpecialty.setCellFactory(listView -> new ListCell<Specialty>() {
            @Override
            protected void updateItem(Specialty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Toutes les spécialités");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        searchSpecialty.setButtonCell(new ListCell<Specialty>() {
            @Override
            protected void updateItem(Specialty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Toutes les spécialités");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        // Custom cell factory to show "Tous les gouvernorats" for null value
        searchGovernorate.setCellFactory(listView -> new ListCell<Governorate>() {
            @Override
            protected void updateItem(Governorate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous les gouvernorats");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        searchGovernorate.setButtonCell(new ListCell<Governorate>() {
            @Override
            protected void updateItem(Governorate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous les gouvernorats");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        // Set default values
        searchSpecialty.setValue(null);
        searchGovernorate.setValue(null);
        
        // Add listeners for real-time filtering
        searchName.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchSpecialty.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchGovernorate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupMedecinCards() {
        listMedecins.setCellFactory(listView -> new ListCell<Medecin>() {
            @Override
            protected void updateItem(Medecin medecin, boolean empty) {
                super.updateItem(medecin, empty);
                
                if (empty || medecin == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createMedecinCard(medecin));
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        });
    }

    private VBox createMedecinCard(Medecin medecin) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: #0e1220;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        // Header with name and verification status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        // Avatar circle with initials
        Label avatar = new Label();
        String initials = "";
        if (medecin.getFirstName() != null && !medecin.getFirstName().isEmpty()) {
            initials += medecin.getFirstName().charAt(0);
        }
        if (medecin.getLastName() != null && !medecin.getLastName().isEmpty()) {
            initials += medecin.getLastName().charAt(0);
        }
        avatar.setText(initials.toUpperCase());
        avatar.setStyle(
            "-fx-background-color: #5b6ef5;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 40px;" +
            "-fx-min-height: 40px;" +
            "-fx-max-width: 40px;" +
            "-fx-max-height: 40px;" +
            "-fx-background-radius: 20;" +
            "-fx-alignment: center;"
        );

        // Name and title
        VBox nameBox = new VBox();
        nameBox.setSpacing(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label nameLabel = new Label("Dr. " + medecin.getFullName());
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label specialtyLabel = new Label(medecin.getSpecialty() != null ? medecin.getSpecialty().getDisplayName() : "Non spécifié");
        specialtyLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 13px;");

        nameBox.getChildren().addAll(nameLabel, specialtyLabel);

        // Verification badge
        Label verifiedBadge = new Label();
        if (medecin.isVerified()) {
            verifiedBadge.setText("✓ Vérifié");
            verifiedBadge.setStyle(
                "-fx-background-color: rgba(34,197,94,0.15);" +
                "-fx-text-fill: #22c55e;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 4 8;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(34,197,94,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
            );
        } else {
            verifiedBadge.setText("En attente");
            verifiedBadge.setStyle(
                "-fx-background-color: rgba(245,158,11,0.15);" +
                "-fx-text-fill: #f59e0b;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 4 8;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(245,158,11,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
            );
        }

        // Active status badge
        Label statusBadge = new Label();
        if (medecin.isActive()) {
            statusBadge.setText("✓ Actif");
            statusBadge.setStyle(
                "-fx-background-color: rgba(34,197,94,0.15);" +
                "-fx-text-fill: #22c55e;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 4 8;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(34,197,94,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
            );
        } else {
            statusBadge.setText("❌ Inactif");
            statusBadge.setStyle(
                "-fx-background-color: rgba(239,68,68,0.15);" +
                "-fx-text-fill: #ef4444;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 4 8;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(239,68,68,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
            );
        }

        header.getChildren().addAll(avatar, nameBox, verifiedBadge, statusBadge);

        // Contact info
        HBox contactInfo = new HBox();
        contactInfo.setSpacing(20);
        contactInfo.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label("📧 " + (medecin.getEmail() != null ? medecin.getEmail() : "Non renseigné"));
        emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label phoneLabel = new Label("📞 " + (medecin.getPhoneNumber() != null ? medecin.getPhoneNumber() : "Non renseigné"));
        phoneLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        contactInfo.getChildren().addAll(emailLabel, phoneLabel);

        // Additional info
        HBox additionalInfo = new HBox();
        additionalInfo.setSpacing(20);
        additionalInfo.setAlignment(Pos.CENTER_LEFT);

        Label ageLabel = new Label("👤 " + medecin.getAge() + " ans");
        ageLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label governorateLabel = new Label("📍 " + (medecin.getGovernorate() != null ? medecin.getGovernorate().getDisplayName() : "Non spécifié"));
        governorateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label experienceLabel = new Label("🎓 " + (medecin.getExperience() != null && !medecin.getExperience().isEmpty() ? medecin.getExperience() : "Non renseigné"));
        experienceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        additionalInfo.getChildren().addAll(ageLabel, governorateLabel, experienceLabel);

        card.getChildren().addAll(header, contactInfo, additionalInfo);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #5b6ef5;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(91,110,245,0.3), 12, 0, 0, 4);" +
            "-fx-cursor: hand;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #0e1220;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        ));

        return card;
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        
        if (editMode) {
            // Edit mode - show update/delete buttons, hide add button
            formTitle.setText("✏️ Modifier le Médecin");
            btnAdd.setVisible(false);
            btnAdd.setManaged(false);
            btnUpdate.setVisible(true);
            btnUpdate.setManaged(true);
            btnDelete.setVisible(true);
            btnDelete.setManaged(true);
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
            
            // Hide password section in edit mode
            passwordSection.setVisible(false);
            passwordSection.setManaged(false);
        } else {
            // Add mode - show add button, hide update/delete buttons
            formTitle.setText("➕ Ajouter un Médecin");
            btnAdd.setVisible(true);
            btnAdd.setManaged(true);
            btnUpdate.setVisible(false);
            btnUpdate.setManaged(false);
            btnDelete.setVisible(false);
            btnDelete.setManaged(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            
            // Show password section in add mode
            passwordSection.setVisible(true);
            passwordSection.setManaged(true);
            tfPassword.setPromptText("Mot de passe (obligatoire)");
        }
    }

    private void loadMedecins() {
        try {
            medecinList.setAll(medecinService.getAll());
            listMedecins.setItems(medecinList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les médecins: " + e.getMessage());
        }
    }

    private void fillForm(Medecin m) {
        tfFirstName.setText(m.getFirstName() != null ? m.getFirstName() : "");
        tfLastName.setText(m.getLastName() != null ? m.getLastName() : "");
        tfEmail.setText(m.getEmail() != null ? m.getEmail() : "");
        tfPhone.setText(m.getPhoneNumber() != null ? m.getPhoneNumber() : "");
        cbSpecialty.setValue(m.getSpecialty());
        tfCin.setText(m.getCin() != null ? m.getCin() : "");
        tfAddress.setText(m.getAddress() != null ? m.getAddress() : "");
        cbGovernorate.setValue(m.getGovernorate());
        tfAge.setText(String.valueOf(m.getAge()));
        cbGender.setValue(m.getGender());
        tfEducation.setText(m.getEducation() != null ? m.getEducation() : "");
        tfExperience.setText(m.getExperience() != null ? m.getExperience() : "");
        cbVerified.setSelected(m.isVerified());
        tfPassword.clear();
    }

    @FXML
    public void addMedecin() {
        if (!validateForm()) return;

        try {
            Medecin m = buildMedecinFromForm();
            if (m.getPassword() == null || m.getPassword().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Le mot de passe est obligatoire.");
                return;
            }
            medecinService.add(m);
            loadMedecins();
            clearForm();
            setEditMode(false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin ajouté avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void updateMedecin() {
        if (selectedMedecin == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un médecin.");
            return;
        }
        if (!validateForm()) return;

        try {
            Medecin m = buildMedecinFromForm();
            m.setId(selectedMedecin.getId());
            
            // If password is empty, keep the existing password
            if (m.getPassword() == null || m.getPassword().trim().isEmpty()) {
                m.setPassword(selectedMedecin.getPassword());
            }
            
            medecinService.update(m);
            loadMedecins();
            clearForm();
            setEditMode(false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin modifié avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void deleteMedecin() {
        if (selectedMedecin == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un médecin.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer Dr. " + selectedMedecin.getFullName() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    medecinService.delete(selectedMedecin.getId());
                    loadMedecins();
                    clearForm();
                    setEditMode(false);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin supprimé !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void clearForm() {
        tfFirstName.clear(); tfLastName.clear(); tfEmail.clear(); tfPassword.clear();
        tfPhone.clear(); tfCin.clear(); tfAddress.clear();
        tfAge.clear(); tfEducation.clear(); tfExperience.clear();
        cbGender.setValue(null);
        cbSpecialty.setValue(null);
        cbGovernorate.setValue(null);
        cbVerified.setSelected(false);
        selectedMedecin = null;
        listMedecins.getSelectionModel().clearSelection();
        setEditMode(false);
    }

    @FXML
    public void cancelEdit() {
        listMedecins.getSelectionModel().clearSelection();
        clearForm();
        setEditMode(false);
    }

    @FXML
    public void onSearch() {
        applyFilters();
    }
    
    private void applyFilters() {
        String nameQuery = searchName.getText() != null ? searchName.getText().toLowerCase().trim() : "";
        Specialty selectedSpecialty = searchSpecialty.getValue();
        Governorate selectedGovernorate = searchGovernorate.getValue();
        
        FilteredList<Medecin> filtered = medecinList.filtered(medecin -> {
            // Name filter (searches in first name, last name, and full name)
            boolean nameMatch = nameQuery.isEmpty() || 
                (medecin.getFirstName() != null && medecin.getFirstName().toLowerCase().contains(nameQuery)) ||
                (medecin.getLastName() != null && medecin.getLastName().toLowerCase().contains(nameQuery)) ||
                (medecin.getFullName() != null && medecin.getFullName().toLowerCase().contains(nameQuery));
            
            // Specialty filter
            boolean specialtyMatch = selectedSpecialty == null || 
                (medecin.getSpecialty() != null && medecin.getSpecialty().equals(selectedSpecialty));
            
            // Governorate filter
            boolean governorateMatch = selectedGovernorate == null || 
                (medecin.getGovernorate() != null && medecin.getGovernorate().equals(selectedGovernorate));
            
            return nameMatch && specialtyMatch && governorateMatch;
        });
        
        listMedecins.setItems(filtered);
    }
    
    @FXML
    public void clearFilters() {
        searchName.clear();
        searchSpecialty.setValue(null);
        searchGovernorate.setValue(null);
        listMedecins.setItems(medecinList);
    }

    private Medecin buildMedecinFromForm() {
        Medecin m = new Medecin();
        m.setFirstName(tfFirstName.getText() != null ? tfFirstName.getText().trim() : "");
        m.setLastName(tfLastName.getText() != null ? tfLastName.getText().trim() : "");
        m.setEmail(tfEmail.getText() != null ? tfEmail.getText().trim() : "");
        m.setPassword(tfPassword.getText() != null ? tfPassword.getText() : "");
        m.setPhoneNumber(tfPhone.getText() != null ? tfPhone.getText().trim() : "");
        m.setSpecialty(cbSpecialty.getValue());
        m.setCin(tfCin.getText() != null ? tfCin.getText().trim() : "");
        m.setAddress(tfAddress.getText() != null ? tfAddress.getText().trim() : "");
        m.setGovernorate(cbGovernorate.getValue());
        
        // Handle age field safely
        String ageText = tfAge.getText();
        if (ageText != null && !ageText.trim().isEmpty()) {
            try {
                m.setAge(Integer.parseInt(ageText.trim()));
            } catch (NumberFormatException e) {
                m.setAge(0);
            }
        } else {
            m.setAge(0);
        }
        
        m.setGender(cbGender.getValue());
        m.setEducation(tfEducation.getText() != null ? tfEducation.getText().trim() : "");
        m.setExperience(tfExperience.getText() != null ? tfExperience.getText().trim() : "");
        m.setActive(true);
        m.setVerified(cbVerified.isSelected());
        return m;
    }

    private boolean validateForm() {
        String firstName = tfFirstName.getText();
        String lastName = tfLastName.getText();
        String email = tfEmail.getText();
        
        if (firstName == null || firstName.trim().isEmpty() || 
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Prénom, Nom et Email sont obligatoires.");
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

    // ==================== ADMIN FUNCTIONALITY ====================

    /**
     * Toggle the active status of the selected medecin (Admin only)
     */
    @FXML
    public void toggleMedecinStatus() {
        if (selectedMedecin == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un médecin.");
            return;
        }

        String action = selectedMedecin.isActive() ? "désactiver" : "activer";
        String statusText = selectedMedecin.isActive() ? "désactivé" : "activé";
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous " + action + " le compte de Dr. " + selectedMedecin.getFullName() + " ?\n\n" +
                "Un compte désactivé ne pourra plus se connecter à l'application.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation - " + action.substring(0, 1).toUpperCase() + action.substring(1) + " compte");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    medecinService.toggleActiveStatus(selectedMedecin.getId());
                    loadMedecins(); // Refresh the list
                    clearForm();
                    setEditMode(false);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Le compte de Dr. " + selectedMedecin.getFullName() + " a été " + statusText + " avec succès !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Erreur lors de la modification du statut: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Export medecins list to CSV format
     */
    @FXML
    public void exportMedecinsToCSV() {
        try {
            List<Medecin> currentList = listMedecins.getItems().isEmpty() ? 
                medecinList : new ArrayList<>(listMedecins.getItems());
            
            if (currentList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucun médecin à exporter.");
                return;
            }

            Stage stage = (Stage) listMedecins.getScene().getWindow();
            exportService.exportMedecinsToCSV(currentList, stage);
            
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", 
                exportService.generateExportSummary(currentList, "médecins"));
                
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", 
                "Erreur lors de l'export CSV: " + e.getMessage());
        }
    }

    /**
     * Export medecins list to PDF format
     */
    @FXML
    public void exportMedecinsToPDF() {
        try {
            List<Medecin> currentList = listMedecins.getItems().isEmpty() ? 
                medecinList : new ArrayList<>(listMedecins.getItems());
            
            if (currentList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucun médecin à exporter.");
                return;
            }

            Stage stage = (Stage) listMedecins.getScene().getWindow();
            exportService.exportMedecinsToPDF(currentList, stage);
            
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", 
                exportService.generateExportSummary(currentList, "médecins"));
                
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", 
                "Erreur lors de l'export PDF: " + e.getMessage());
        }
    }

    /**
     * Show statistics about active/inactive medecins
     */
    @FXML
    public void showMedecinStatistics() {
        try {
            int[] counts = medecinService.getActiveInactiveCounts();
            int activeCount = counts[0];
            int inactiveCount = counts[1];
            int totalCount = activeCount + inactiveCount;
            
            String message = String.format(
                "📊 Statistiques des Médecins\n\n" +
                "👥 Total: %d médecin(s)\n" +
                "✅ Actifs: %d médecin(s) (%.1f%%)\n" +
                "❌ Inactifs: %d médecin(s) (%.1f%%)\n\n" +
                "Les comptes inactifs ne peuvent pas se connecter à l'application.",
                totalCount,
                activeCount, totalCount > 0 ? (activeCount * 100.0 / totalCount) : 0,
                inactiveCount, totalCount > 0 ? (inactiveCount * 100.0 / totalCount) : 0
            );
            
            showAlert(Alert.AlertType.INFORMATION, "Statistiques", message);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du calcul des statistiques: " + e.getMessage());
        }
    }

    /**
     * Check doctor authenticity using Flask API
     */
    @FXML
    public void checkMedecinAuthenticity() {
        // Get selected medecin from form
        if (tfFirstName.getText().isEmpty() || tfLastName.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", 
                "Veuillez sélectionner un médecin pour vérifier son authenticité.");
            return;
        }

        String fullName = tfFirstName.getText().trim() + " " + tfLastName.getText().trim();
        String specialty = cbSpecialty.getValue() != null ? cbSpecialty.getValue().getDisplayName() : null;
        String governorate = cbGovernorate.getValue() != null ? cbGovernorate.getValue().getDisplayName() : null;

        // Show loading dialog
        showAlert(Alert.AlertType.INFORMATION, "Vérification", 
            "Vérification de l'authenticité du médecin en cours...");

        // Call API in background thread
        new Thread(() -> {
            try {
                DoctorAPIService apiService = new DoctorAPIService();
                boolean exists = apiService.checkDoctorExists(fullName, specialty, governorate);

                Platform.runLater(() -> {
                    if (exists) {
                        showAlert(Alert.AlertType.INFORMATION, "✅ Médecin Vérifié", 
                            "Le médecin '" + fullName + "' a été trouvé dans l'annuaire national.\n\n" +
                            "Ce médecin est authentifié et enregistré auprès des autorités de santé.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "⚠️ Médecin Non Trouvé", 
                            "Le médecin '" + fullName + "' n'a pas été trouvé dans l'annuaire national.\n\n" +
                            "Veuillez vérifier les informations ou contacter l'administration.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "❌ Erreur de Vérification", 
                        "Impossible de vérifier l'authenticité du médecin.\n\n" +
                        "Erreur: " + e.getMessage() + "\n\n" +
                        "Assurez-vous que l'API Flask est en cours d'exécution sur le port 5000.");
                });
                e.printStackTrace();
            }
        }).start();
    }
}