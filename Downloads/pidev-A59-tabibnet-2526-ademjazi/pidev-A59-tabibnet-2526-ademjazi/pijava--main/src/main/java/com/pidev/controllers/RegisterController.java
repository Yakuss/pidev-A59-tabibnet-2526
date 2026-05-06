package com.pidev.controllers;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.pidev.utils.BCrypt;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Controller for the registration view.
 */
public class RegisterController {

    @FXML private ToggleButton btnPatient, btnMedecin;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, cinField, ageField, insuranceNumberField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<Specialty> specialtyCombo;
    @FXML private ComboBox<Governorate> governorateCombo;
    @FXML private ComboBox<String> genderCombo;
    @FXML private DatePicker dobPicker;
    @FXML private CheckBox hasInsuranceCheckBox;
    @FXML private VBox specialtyBox;
    @FXML private Label errorMessage;

    private final PatientService patientService = new PatientService();
    private final MedecinService medecinService = new MedecinService();

    private static final String STYLE_ACTIVE = 
            "-fx-background-color: rgba(91,110,245,0.2);" +
            "-fx-text-fill: #818cf8; -fx-font-weight: 600;" +
            "-fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-border-color: rgba(91,110,245,0.5);" +
            "-fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-cursor: hand;";

    private static final String STYLE_INACTIVE = 
            "-fx-background-color: #1c2133;" +
            "-fx-text-fill: #94a3b8; -fx-font-size: 13px;" +
            "-fx-background-radius: 8; -fx-border-color: #252d42;" +
            "-fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-cursor: hand;";

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        btnPatient.setToggleGroup(group);
        btnMedecin.setToggleGroup(group);
        btnPatient.setSelected(true);
        
        // Apply initial styles
        updateToggleStyles();
        
        specialtyCombo.setItems(FXCollections.observableArrayList(Specialty.values()));
        governorateCombo.setItems(FXCollections.observableArrayList(Governorate.values()));
        
        // Enable/disable insurance number field based on checkbox
        if (hasInsuranceCheckBox != null && insuranceNumberField != null) {
            insuranceNumberField.setDisable(true);
            hasInsuranceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                insuranceNumberField.setDisable(!newVal);
                if (!newVal) insuranceNumberField.clear();
            });
        }
        
        // Hide error message initially
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
        }
        
        // Add listeners to clear error when user starts typing
        addClearErrorListeners();
        
        // Add real-time validation styling
        addValidationListeners();
    }
    
    /**
     * Add listeners to clear error message when user types
     */
    private void addClearErrorListeners() {
        if (firstNameField != null) firstNameField.textProperty().addListener((obs, old, val) -> hideError());
        if (lastNameField != null) lastNameField.textProperty().addListener((obs, old, val) -> hideError());
        if (emailField != null) emailField.textProperty().addListener((obs, old, val) -> hideError());
        if (passwordField != null) passwordField.textProperty().addListener((obs, old, val) -> hideError());
        if (confirmPasswordField != null) confirmPasswordField.textProperty().addListener((obs, old, val) -> hideError());
        if (phoneField != null) phoneField.textProperty().addListener((obs, old, val) -> hideError());
        if (cinField != null) cinField.textProperty().addListener((obs, old, val) -> hideError());
        if (ageField != null) ageField.textProperty().addListener((obs, old, val) -> hideError());
    }
    
    /**
     * Add real-time validation styling to fields
     */
    private void addValidationListeners() {
        // Email validation
        if (emailField != null) {
            emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && !emailField.getText().isEmpty()) {
                    if (!isValidEmail(emailField.getText())) {
                        emailField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                    } else {
                        emailField.setStyle("");
                    }
                }
            });
        }
        
        // Phone validation
        if (phoneField != null) {
            phoneField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && !phoneField.getText().isEmpty()) {
                    if (!isValidPhone(phoneField.getText())) {
                        phoneField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                    } else {
                        phoneField.setStyle("");
                    }
                }
            });
        }
        
        // Age validation (numbers only)
        if (ageField != null) {
            ageField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.isEmpty() && !newVal.matches("\\d*")) {
                    ageField.setText(oldVal);
                }
            });
        }
        
        // Password match validation
        if (confirmPasswordField != null) {
            confirmPasswordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && !confirmPasswordField.getText().isEmpty()) {
                    if (!confirmPasswordField.getText().equals(passwordField.getText())) {
                        confirmPasswordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                    } else {
                        confirmPasswordField.setStyle("");
                    }
                }
            });
        }
    }

    @FXML
    public void handleRoleChanged() {
        boolean isMedecin = btnMedecin.isSelected();
        specialtyBox.setManaged(isMedecin);
        specialtyBox.setVisible(isMedecin);
        
        // Update button styles
        updateToggleStyles();
    }

    private void updateToggleStyles() {
        if (btnPatient.isSelected()) {
            btnPatient.setStyle(STYLE_ACTIVE);
            btnMedecin.setStyle(STYLE_INACTIVE);
        } else {
            btnPatient.setStyle(STYLE_INACTIVE);
            btnMedecin.setStyle(STYLE_ACTIVE);
        }
    }

    @FXML
    public void handleRegister() {
        if (!validateForm()) return;

        try {
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());

            if (btnPatient.isSelected()) {
                Patient p = new Patient();
                p.setFirstName(firstNameField.getText().trim());
                p.setLastName(lastNameField.getText().trim());
                p.setEmail(emailField.getText().trim());
                p.setPassword(hashedPassword);
                p.setPhoneNumber(phoneField.getText().trim());
                
                // Handle date of birth and calculate age
                if (dobPicker.getValue() != null) {
                    LocalDate dob = dobPicker.getValue();
                    p.setDateOfBirth(dob.atStartOfDay());
                    p.setAge(calculateAge(dob));
                }
                
                // Handle insurance
                p.setHasInsurance(hasInsuranceCheckBox.isSelected());
                if (hasInsuranceCheckBox.isSelected()) {
                    p.setInsuranceNumber(insuranceNumberField.getText().trim());
                }
                
                p.setActive(true);
                p.setRoles("[\"ROLE_PATIENT\"]");
                patientService.add(p);
            } else {
                Medecin m = new Medecin();
                m.setFirstName(firstNameField.getText().trim());
                m.setLastName(lastNameField.getText().trim());
                m.setEmail(emailField.getText().trim());
                m.setPassword(hashedPassword);
                m.setPhoneNumber(phoneField.getText().trim());
                m.setSpecialty(specialtyCombo.getValue());
                m.setGovernorate(governorateCombo.getValue());
                m.setCin(cinField.getText().trim());
                m.setGender(genderCombo.getValue());
                String ageText = ageField.getText().trim();
                if (!ageText.isEmpty()) {
                    try { m.setAge(Integer.parseInt(ageText)); } catch (NumberFormatException ignored) {}
                }
                m.setActive(true);
                m.setRoles("[\"ROLE_MEDECIN\"]");
                medecinService.add(m);
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            showLogin();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    @FXML
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Clear all field styles first
        clearFieldStyles();
        
        // Check required fields
        if (firstNameField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire.");
            firstNameField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            firstNameField.requestFocus();
            return false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire.");
            lastNameField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            lastNameField.requestFocus();
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("L'adresse email est obligatoire.");
            emailField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            emailField.requestFocus();
            return false;
        }
        
        // Validate email format
        if (!isValidEmail(emailField.getText().trim())) {
            showError("Format d'email invalide. Exemple: nom@exemple.com");
            emailField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            emailField.requestFocus();
            return false;
        }
        
        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est obligatoire.");
            passwordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            passwordField.requestFocus();
            return false;
        }
        
        // Validate password length
        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit comporter au moins 6 caractères.");
            passwordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            passwordField.requestFocus();
            return false;
        }
        
        // Validate password match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            confirmPasswordField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // Validate phone format if provided
        if (!phoneField.getText().trim().isEmpty() && !isValidPhone(phoneField.getText().trim())) {
            showError("Format de téléphone invalide. Exemple: +216 12 345 678 ou 12345678");
            phoneField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
            phoneField.requestFocus();
            return false;
        }
        
        // Medecin-specific validation
        if (btnMedecin.isSelected()) {
            if (cinField.getText().trim().isEmpty()) {
                showError("Le CIN est obligatoire pour les médecins.");
                cinField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                cinField.requestFocus();
                return false;
            }
            
            if (cinField.getText().trim().length() != 8) {
                showError("Le CIN doit comporter exactement 8 chiffres.");
                cinField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                cinField.requestFocus();
                return false;
            }
            
            if (specialtyCombo.getValue() == null) {
                showError("Veuillez sélectionner une spécialité.");
                specialtyCombo.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                specialtyCombo.requestFocus();
                return false;
            }
            
            if (governorateCombo.getValue() == null) {
                showError("Veuillez sélectionner un gouvernorat.");
                governorateCombo.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                governorateCombo.requestFocus();
                return false;
            }
        }
        
        // Patient-specific validation
        if (btnPatient.isSelected()) {
            if (dobPicker.getValue() == null) {
                showError("La date de naissance est obligatoire pour les patients.");
                dobPicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                dobPicker.requestFocus();
                return false;
            }
            
            // Check if date of birth is in the future
            if (dobPicker.getValue().isAfter(LocalDate.now())) {
                showError("La date de naissance ne peut pas être dans le futur.");
                dobPicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                dobPicker.requestFocus();
                return false;
            }
            
            // Check minimum age (e.g., 1 year old)
            int age = calculateAge(dobPicker.getValue());
            if (age < 1) {
                showError("L'âge minimum est de 1 an.");
                dobPicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1;");
                dobPicker.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        // Simple email regex pattern
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validate phone format (Tunisian format)
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // Optional field
        // Remove spaces and special characters
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        // Check if it's 8 digits (Tunisian format) or starts with 216 (country code)
        return cleanPhone.matches("^\\d{8}$") || cleanPhone.matches("^216\\d{8}$");
    }
    
    /**
     * Clear all field error styles
     */
    private void clearFieldStyles() {
        if (firstNameField != null) firstNameField.setStyle("");
        if (lastNameField != null) lastNameField.setStyle("");
        if (emailField != null) emailField.setStyle("");
        if (passwordField != null) passwordField.setStyle("");
        if (confirmPasswordField != null) confirmPasswordField.setStyle("");
        if (phoneField != null) phoneField.setStyle("");
        if (cinField != null) cinField.setStyle("");
        if (ageField != null) ageField.setStyle("");
        if (specialtyCombo != null) specialtyCombo.setStyle("");
        if (governorateCombo != null) governorateCombo.setStyle("");
        if (dobPicker != null) dobPicker.setStyle("");
    }

    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText("❌ " + message);
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
        }
    }
    
    private void hideError() {
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
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
}
