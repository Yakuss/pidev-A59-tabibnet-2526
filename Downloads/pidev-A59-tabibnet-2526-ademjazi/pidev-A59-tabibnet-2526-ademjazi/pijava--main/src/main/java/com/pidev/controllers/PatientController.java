package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.services.PatientService;
import com.pidev.services.ExportService;

import java.util.ArrayList;
import java.util.List;
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

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Controller for Patient CRUD operations with elegant card-based ListView.
 */
public class PatientController {

    @FXML private ListView<Patient> listPatients;
    @FXML private TextField tfFirstName, tfLastName, tfEmail, tfPhone, tfAddress, tfInsuranceNumber;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbGender;
    @FXML private CheckBox cbInsurance;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnCancel;
    @FXML private Label formTitle;
    @FXML private VBox passwordSection;
    
    // Search filter fields
    @FXML private TextField searchName;
    @FXML private ComboBox<String> searchGender;
    @FXML private ComboBox<String> searchInsurance;

    private final PatientService patientService = new PatientService();
    private final ExportService exportService = new ExportService();
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Patient selectedPatient = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Populate gender ComboBox
        cbGender.setItems(FXCollections.observableArrayList("Homme", "Femme"));
        
        // Setup search filters
        setupSearchFilters();

        setupPatientCards();
        loadPatients();

        listPatients.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                fillForm(newVal);
                setEditMode(true);
            } else {
                selectedPatient = null;
                setEditMode(false);
            }
        });
        
        // Initialize in add mode
        setEditMode(false);
        
        // Enable/disable insurance number field based on checkbox
        tfInsuranceNumber.setDisable(true);
        cbInsurance.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tfInsuranceNumber.setDisable(!newVal);
            if (!newVal) tfInsuranceNumber.clear();
        });
    }

    private void setupSearchFilters() {
        // Setup gender filter
        ObservableList<String> genderOptions = FXCollections.observableArrayList();
        genderOptions.add(null); // "Tous les genres"
        genderOptions.addAll("Homme", "Femme");
        searchGender.setItems(genderOptions);
        
        // Setup insurance filter
        ObservableList<String> insuranceOptions = FXCollections.observableArrayList();
        insuranceOptions.add(null); // "Tous"
        insuranceOptions.addAll("Avec assurance", "Sans assurance");
        searchInsurance.setItems(insuranceOptions);
        
        // Custom cell factories for search filters
        searchGender.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous les genres");
                } else {
                    setText(item);
                }
            }
        });
        
        searchGender.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous les genres");
                } else {
                    setText(item);
                }
            }
        });
        
        searchInsurance.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous");
                } else {
                    setText(item);
                }
            }
        });
        
        searchInsurance.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous");
                } else {
                    setText(item);
                }
            }
        });
        
        // Set default values
        searchGender.setValue(null);
        searchInsurance.setValue(null);
        
        // Add listeners for real-time filtering
        searchName.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchGender.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchInsurance.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupPatientCards() {
        listPatients.setCellFactory(listView -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                
                if (empty || patient == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createPatientCard(patient));
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        });
    }

    private VBox createPatientCard(Patient patient) {
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

        // Header with name and insurance status
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        // Avatar circle with initials
        Label avatar = new Label();
        String initials = "";
        if (patient.getFirstName() != null && !patient.getFirstName().isEmpty()) {
            initials += patient.getFirstName().charAt(0);
        }
        if (patient.getLastName() != null && !patient.getLastName().isEmpty()) {
            initials += patient.getLastName().charAt(0);
        }
        avatar.setText(initials.toUpperCase());
        avatar.setStyle(
            "-fx-background-color: #22c55e;" +
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

        // Name and info
        VBox nameBox = new VBox();
        nameBox.setSpacing(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label nameLabel = new Label(patient.getFullName());
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label ageGenderLabel = new Label(patient.getAge() + " ans" + 
            (patient.getGender() != null ? " • " + patient.getGender() : ""));
        ageGenderLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 13px;");

        nameBox.getChildren().addAll(nameLabel, ageGenderLabel);

        // Insurance badge
        Label insuranceBadge = new Label();
        if (patient.isHasInsurance()) {
            insuranceBadge.setText("✓ Assuré");
            insuranceBadge.setStyle(
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
            insuranceBadge.setText("Non assuré");
            insuranceBadge.setStyle(
                "-fx-background-color: rgba(156,163,175,0.15);" +
                "-fx-text-fill: #9ca3af;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 4 8;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(156,163,175,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
            );
        }

        // Active status badge
        Label statusBadge = new Label();
        if (patient.isActive()) {
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

        header.getChildren().addAll(avatar, nameBox, insuranceBadge, statusBadge);

        // Contact info
        HBox contactInfo = new HBox();
        contactInfo.setSpacing(20);
        contactInfo.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label("📧 " + (patient.getEmail() != null ? patient.getEmail() : "Non renseigné"));
        emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label phoneLabel = new Label("📞 " + (patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "Non renseigné"));
        phoneLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        contactInfo.getChildren().addAll(emailLabel, phoneLabel);

        // Additional info
        HBox additionalInfo = new HBox();
        additionalInfo.setSpacing(20);
        additionalInfo.setAlignment(Pos.CENTER_LEFT);

        String dobText = "📅 ";
        if (patient.getDateOfBirth() != null) {
            dobText += patient.getDateOfBirth().toLocalDate().toString();
        } else {
            dobText += "Non renseigné";
        }
        Label dobLabel = new Label(dobText);
        dobLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label addressLabel = new Label("📍 " + (patient.getAddress() != null && !patient.getAddress().isEmpty() ? patient.getAddress() : "Non renseigné"));
        addressLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        additionalInfo.getChildren().addAll(dobLabel, addressLabel);

        card.getChildren().addAll(header, contactInfo, additionalInfo);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(34,197,94,0.3), 12, 0, 0, 4);" +
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
            formTitle.setText("✏️ Modifier le Patient");
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
            formTitle.setText("➕ Ajouter un Patient");
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

    private void loadPatients() {
        try {
            List<Patient> patients = patientService.getAll();
            patientList.setAll(patients);
            listPatients.setItems(patientList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les patients: " + e.getMessage());
        }
    }

    private void fillForm(Patient p) {
        tfFirstName.setText(p.getFirstName() != null ? p.getFirstName() : "");
        tfLastName.setText(p.getLastName() != null ? p.getLastName() : "");
        tfEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        tfPhone.setText(p.getPhoneNumber() != null ? p.getPhoneNumber() : "");
        tfAddress.setText(p.getAddress() != null ? p.getAddress() : "");
        
        // Set date of birth in DatePicker
        if (p.getDateOfBirth() != null) {
            dpDateOfBirth.setValue(p.getDateOfBirth().toLocalDate());
        } else {
            dpDateOfBirth.setValue(null);
        }
        
        cbGender.setValue(p.getGender());
        cbInsurance.setSelected(p.isHasInsurance());
        tfInsuranceNumber.setText(p.getInsuranceNumber() != null ? p.getInsuranceNumber() : "");
        tfInsuranceNumber.setDisable(!p.isHasInsurance());
        tfPassword.clear();
    }

    @FXML
    public void addPatient() {
        if (!validateForm()) return;
        try {
            Patient p = buildPatientFromForm();
            if (p.getPassword() == null || p.getPassword().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Le mot de passe est obligatoire.");
                return;
            }
            patientService.add(p);
            loadPatients();
            clearForm();
            setEditMode(false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient ajouté avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void updatePatient() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un patient.");
            return;
        }
        if (!validateForm()) return;
        
        try {
            Patient p = buildPatientFromForm();
            p.setId(selectedPatient.getId());
            
            // Keep existing password for updates
            p.setPassword(selectedPatient.getPassword());
            
            patientService.update(p);
            loadPatients();
            clearForm();
            setEditMode(false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient modifié avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void deletePatient() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un patient.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + selectedPatient.getFullName() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    patientService.delete(selectedPatient.getId());
                    loadPatients();
                    clearForm();
                    setEditMode(false);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient supprimé !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void clearForm() {
        tfFirstName.clear(); tfLastName.clear(); tfEmail.clear(); tfPassword.clear();
        tfPhone.clear(); tfAddress.clear(); dpDateOfBirth.setValue(null);
        cbGender.setValue(null); cbInsurance.setSelected(false); tfInsuranceNumber.clear();
        tfInsuranceNumber.setDisable(true);
        selectedPatient = null;
        listPatients.getSelectionModel().clearSelection();
        setEditMode(false);
    }

    @FXML
    public void cancelEdit() {
        listPatients.getSelectionModel().clearSelection();
        clearForm();
        setEditMode(false);
    }

    @FXML
    public void onSearch() {
        applyFilters();
    }
    
    private void applyFilters() {
        String nameQuery = searchName.getText() != null ? searchName.getText().toLowerCase().trim() : "";
        String selectedGender = searchGender.getValue();
        String selectedInsurance = searchInsurance.getValue();
        
        FilteredList<Patient> filtered = patientList.filtered(patient -> {
            // Name filter (searches in first name, last name, and full name)
            boolean nameMatch = nameQuery.isEmpty() || 
                (patient.getFirstName() != null && patient.getFirstName().toLowerCase().contains(nameQuery)) ||
                (patient.getLastName() != null && patient.getLastName().toLowerCase().contains(nameQuery)) ||
                (patient.getFullName() != null && patient.getFullName().toLowerCase().contains(nameQuery));
            
            // Gender filter
            boolean genderMatch = selectedGender == null || 
                (patient.getGender() != null && patient.getGender().equals(selectedGender));
            
            // Insurance filter
            boolean insuranceMatch = selectedInsurance == null ||
                (selectedInsurance.equals("Avec assurance") && patient.isHasInsurance()) ||
                (selectedInsurance.equals("Sans assurance") && !patient.isHasInsurance());
            
            return nameMatch && genderMatch && insuranceMatch;
        });
        
        listPatients.setItems(filtered);
    }
    
    @FXML
    public void clearFilters() {
        searchName.clear();
        searchGender.setValue(null);
        searchInsurance.setValue(null);
        listPatients.setItems(patientList);
    }

    private Patient buildPatientFromForm() {
        Patient p = new Patient();
        p.setFirstName(tfFirstName.getText() != null ? tfFirstName.getText().trim() : "");
        p.setLastName(tfLastName.getText() != null ? tfLastName.getText().trim() : "");
        p.setEmail(tfEmail.getText() != null ? tfEmail.getText().trim() : "");
        p.setPassword(tfPassword.getText() != null ? tfPassword.getText() : "");
        p.setPhoneNumber(tfPhone.getText() != null ? tfPhone.getText().trim() : "");
        p.setAddress(tfAddress.getText() != null ? tfAddress.getText().trim() : "");
        
        // Set date of birth and calculate age
        if (dpDateOfBirth.getValue() != null) {
            LocalDate dob = dpDateOfBirth.getValue();
            p.setDateOfBirth(dob.atStartOfDay());
            p.setAge(calculateAge(dob));
        } else {
            p.setAge(0);
        }
        
        p.setGender(cbGender.getValue());
        p.setHasInsurance(cbInsurance.isSelected());
        if (cbInsurance.isSelected()) {
            p.setInsuranceNumber(tfInsuranceNumber.getText() != null ? tfInsuranceNumber.getText().trim() : "");
        } else {
            p.setInsuranceNumber(null);
        }
        p.setActive(true);
        p.setRoles("[\"ROLE_PATIENT\"]");
        return p;
    }

    private boolean validateForm() {
        String firstName = tfFirstName.getText();
        String lastName = tfLastName.getText();
        String email = tfEmail.getText();
        
        if (firstName == null || firstName.trim().isEmpty() || 
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() || !email.contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Prénom, Nom et Email valide sont obligatoires.");
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

    /**
     * Calculate age from date of birth
     */
    private int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // ==================== ADMIN FUNCTIONALITY ====================

    /**
     * Toggle the active status of the selected patient (Admin only)
     */
    @FXML
    public void togglePatientStatus() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un patient.");
            return;
        }

        String action = selectedPatient.isActive() ? "désactiver" : "activer";
        String statusText = selectedPatient.isActive() ? "désactivé" : "activé";
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous " + action + " le compte de " + selectedPatient.getFullName() + " ?\n\n" +
                "Un compte désactivé ne pourra plus se connecter à l'application.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation - " + action.substring(0, 1).toUpperCase() + action.substring(1) + " compte");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    patientService.toggleActiveStatus(selectedPatient.getId());
                    loadPatients(); // Refresh the list
                    clearForm();
                    setEditMode(false);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Le compte de " + selectedPatient.getFullName() + " a été " + statusText + " avec succès !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Erreur lors de la modification du statut: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Export patients list to CSV format
     */
    @FXML
    public void exportPatientsToCSV() {
        try {
            List<Patient> currentList = listPatients.getItems().isEmpty() ? 
                patientList : new ArrayList<>(listPatients.getItems());
            
            if (currentList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucun patient à exporter.");
                return;
            }

            Stage stage = (Stage) listPatients.getScene().getWindow();
            exportService.exportPatientsToCSV(currentList, stage);
            
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", 
                exportService.generateExportSummary(currentList, "patients"));
                
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", 
                "Erreur lors de l'export CSV: " + e.getMessage());
        }
    }

    /**
     * Export patients list to PDF format
     */
    @FXML
    public void exportPatientsToPDF() {
        try {
            List<Patient> currentList = listPatients.getItems().isEmpty() ? 
                patientList : new ArrayList<>(listPatients.getItems());
            
            if (currentList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucun patient à exporter.");
                return;
            }

            Stage stage = (Stage) listPatients.getScene().getWindow();
            exportService.exportPatientsToPDF(currentList, stage);
            
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", 
                exportService.generateExportSummary(currentList, "patients"));
                
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", 
                "Erreur lors de l'export PDF: " + e.getMessage());
        }
    }

    /**
     * Show statistics about active/inactive patients
     */
    @FXML
    public void showPatientStatistics() {
        try {
            int[] counts = patientService.getActiveInactiveCounts();
            int activeCount = counts[0];
            int inactiveCount = counts[1];
            int totalCount = activeCount + inactiveCount;
            
            String message = String.format(
                "📊 Statistiques des Patients\n\n" +
                "👥 Total: %d patient(s)\n" +
                "✅ Actifs: %d patient(s) (%.1f%%)\n" +
                "❌ Inactifs: %d patient(s) (%.1f%%)\n\n" +
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
}