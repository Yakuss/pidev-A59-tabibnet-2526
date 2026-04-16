package com.pidev.controllers;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.AuthService;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import com.pidev.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField ageField;
    @FXML private VBox ageContainer;
    @FXML private ComboBox<String> genderCombo;

    @FXML private ToggleButton patientToggle;
    @FXML private ToggleButton doctorToggle;
    @FXML private ToggleGroup roleGroup;

    @FXML private VBox doctorFields;
    @FXML private VBox patientFields;

    @FXML private TextField phoneField;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private TextField cinField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> governorateCombo;
    @FXML private TextArea educationArea;
    @FXML private TextArea experienceArea;

    @FXML private DatePicker dobPicker;
    @FXML private CheckBox insuranceCheck;
    @FXML private TextField insuranceNumberField;

    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    private final AuthService authService = new AuthService();
    private final MedecinService medecinService = new MedecinService();
    private final PatientService patientService = new PatientService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[2459]\\d{7}$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern INSURANCE_PATTERN = Pattern.compile("^[A-Z0-9]{5,20}$");

    @FXML
    private void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        specialtyCombo.setItems(FXCollections.observableArrayList(Specialty.getChoices().keySet()));
        governorateCombo.setItems(FXCollections.observableArrayList(Governorate.getChoices().keySet()));

        roleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateUIBasedOnRole(newToggle == doctorToggle);
            validateForm();
        });

        patientToggle.setSelected(true);
        updateUIBasedOnRole(false);

        setupValidationListeners();

        errorLabel.setVisible(false);
        registerButton.setDisable(true);
    }

    private void setupValidationListeners() {
        firstNameField.textProperty().addListener((obs, old, val) -> validateForm());
        lastNameField.textProperty().addListener((obs, old, val) -> validateForm());
        emailField.textProperty().addListener((obs, old, val) -> validateForm());
        passwordField.textProperty().addListener((obs, old, val) -> validateForm());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> validateForm());
        ageField.textProperty().addListener((obs, old, val) -> validateForm());
        genderCombo.valueProperty().addListener((obs, old, val) -> validateForm());

        phoneField.textProperty().addListener((obs, old, val) -> validateForm());
        specialtyCombo.valueProperty().addListener((obs, old, val) -> validateForm());
        cinField.textProperty().addListener((obs, old, val) -> validateForm());
        addressField.textProperty().addListener((obs, old, val) -> validateForm());
        governorateCombo.valueProperty().addListener((obs, old, val) -> validateForm());

        dobPicker.valueProperty().addListener((obs, old, val) -> validateForm());
        insuranceCheck.selectedProperty().addListener((obs, old, val) -> {
            insuranceNumberField.setDisable(!val);
            if (!val) insuranceNumberField.clear();
            validateForm();
        });
        insuranceNumberField.textProperty().addListener((obs, old, val) -> validateForm());
        insuranceNumberField.setDisable(true);
    }

    private void updateUIBasedOnRole(boolean isDoctor) {
        doctorFields.setVisible(isDoctor);
        doctorFields.setManaged(isDoctor);
        patientFields.setVisible(!isDoctor);
        patientFields.setManaged(!isDoctor);
        ageContainer.setVisible(isDoctor);
        ageContainer.setManaged(isDoctor);
    }

    private void validateForm() {
        boolean isValid = true;
        clearAllErrors();

        if (firstNameField.getText().trim().isEmpty()) {
            markError(firstNameField, "First name is required");
            isValid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            markError(lastNameField, "Last name is required");
            isValid = false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            markError(emailField, "Email is required");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            markError(emailField, "Invalid email format");
            isValid = false;
        }
        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            markError(passwordField, "Password is required");
            isValid = false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            markError(passwordField, "Minimum 6 characters required");
            isValid = false;
        }
        String confirm = confirmPasswordField.getText().trim();
        if (confirm.isEmpty()) {
            markError(confirmPasswordField, "Confirm password");
            isValid = false;
        } else if (!password.equals(confirm)) {
            markError(confirmPasswordField, "Passwords do not match");
            isValid = false;
        }
        if (genderCombo.getValue() == null) {
            markError(genderCombo, "Select gender");
            isValid = false;
        }

        boolean isDoctor = doctorToggle.isSelected();

        if (isDoctor) {
            String ageText = ageField.getText().trim();
            if (ageText.isEmpty()) {
                markError(ageField, "Age is required");
                isValid = false;
            } else {
                try {
                    int age = Integer.parseInt(ageText);
                    if (age < 1 || age > 120) {
                        markError(ageField, "Age must be 1-120");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    markError(ageField, "Invalid number");
                    isValid = false;
                }
            }

            String phone = phoneField.getText().trim();
            if (phone.isEmpty()) {
                markError(phoneField, "Phone is required");
                isValid = false;
            } else if (!PHONE_PATTERN.matcher(phone).matches()) {
                markError(phoneField, "8 digits, start with 2,5,9,4");
                isValid = false;
            }
            if (specialtyCombo.getValue() == null) {
                markError(specialtyCombo, "Select specialty");
                isValid = false;
            }
            String cin = cinField.getText().trim();
            if (cin.isEmpty()) {
                markError(cinField, "CIN is required");
                isValid = false;
            } else if (!CIN_PATTERN.matcher(cin).matches()) {
                markError(cinField, "8 digits");
                isValid = false;
            }
            if (addressField.getText().trim().isEmpty()) {
                markError(addressField, "Address is required");
                isValid = false;
            }
            if (governorateCombo.getValue() == null) {
                markError(governorateCombo, "Select governorate");
                isValid = false;
            }
        } else {
            LocalDate dob = dobPicker.getValue();
            if (dob == null) {
                markError(dobPicker, "Date of birth is required");
                isValid = false;
            } else {
                int age = Period.between(dob, LocalDate.now()).getYears();
                if (age < 0) {
                    markError(dobPicker, "Date cannot be in future");
                    isValid = false;
                } else if (age > 120) {
                    markError(dobPicker, "Age exceeds 120 years");
                    isValid = false;
                }
            }

            if (insuranceCheck.isSelected()) {
                String insNum = insuranceNumberField.getText().trim();
                if (insNum.isEmpty()) {
                    markError(insuranceNumberField, "Insurance number required");
                    isValid = false;
                } else if (!INSURANCE_PATTERN.matcher(insNum).matches()) {
                    markError(insuranceNumberField, "5-20 alphanumeric (A-Z,0-9)");
                    isValid = false;
                }
            }
        }

        registerButton.setDisable(!isValid);
        if (isValid) {
            errorLabel.setVisible(false);
        }
    }

    private void markError(Control field, String message) {
        field.getStyleClass().add("error");
        Tooltip tooltip = new Tooltip(message);
        tooltip.getStyleClass().add("validation-tooltip");
        tooltip.setShowDelay(Duration.millis(100));
        field.setTooltip(tooltip);
    }

    private void clearAllErrors() {
        firstNameField.getStyleClass().remove("error");
        lastNameField.getStyleClass().remove("error");
        emailField.getStyleClass().remove("error");
        passwordField.getStyleClass().remove("error");
        confirmPasswordField.getStyleClass().remove("error");
        ageField.getStyleClass().remove("error");
        genderCombo.getStyleClass().remove("error");
        phoneField.getStyleClass().remove("error");
        specialtyCombo.getStyleClass().remove("error");
        cinField.getStyleClass().remove("error");
        addressField.getStyleClass().remove("error");
        governorateCombo.getStyleClass().remove("error");
        dobPicker.getStyleClass().remove("error");
        insuranceNumberField.getStyleClass().remove("error");

        firstNameField.setTooltip(null);
        lastNameField.setTooltip(null);
        emailField.setTooltip(null);
        passwordField.setTooltip(null);
        confirmPasswordField.setTooltip(null);
        ageField.setTooltip(null);
        genderCombo.setTooltip(null);
        phoneField.setTooltip(null);
        specialtyCombo.setTooltip(null);
        cinField.setTooltip(null);
        addressField.setTooltip(null);
        governorateCombo.setTooltip(null);
        dobPicker.setTooltip(null);
        insuranceNumberField.setTooltip(null);
    }

    @FXML
    private void handleRegister() {
        validateForm();
        if (registerButton.isDisable()) {
            showError("Please correct all errors before submitting.");
            return;
        }

        try {
            String email = emailField.getText().trim();
            if (authService.emailExists(email)) {
                markError(emailField, "Email already registered");
                registerButton.setDisable(true);
                return;
            }

            if (doctorToggle.isSelected()) {
                registerMedecin();
            } else {
                registerPatient();
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Your account has been created. Please login.");
            alert.showAndWait();

            goToLogin();

        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerMedecin() throws Exception {
        int age = Integer.parseInt(ageField.getText().trim());

        Medecin m = new Medecin();
        fillBaseUserFields(m, age);
        m.setPhoneNumber(phoneField.getText().trim());
        m.setSpecialty(Specialty.fromDisplayName(specialtyCombo.getValue()));
        m.setCin(cinField.getText().trim());
        m.setAddress(addressField.getText().trim());
        m.setGovernorate(Governorate.fromDisplayName(governorateCombo.getValue()));

        String education = educationArea.getText().trim();
        m.setEducation(education.isEmpty() ? null : education);
        String experience = experienceArea.getText().trim();
        m.setExperience(experience.isEmpty() ? null : experience);

        m.setVerified(false);

        medecinService.add(m);
    }

    private void registerPatient() throws Exception {
        LocalDate dob = dobPicker.getValue();
        int age = Period.between(dob, LocalDate.now()).getYears();

        Patient p = new Patient();
        fillBaseUserFields(p, age);
        p.setPhoneNumber(null);
        p.setAddress(null);
        p.setDateOfBirth(dob.atStartOfDay());
        p.setHasInsurance(insuranceCheck.isSelected());
        p.setInsuranceNumber(insuranceCheck.isSelected() ? insuranceNumberField.getText().trim() : null);

        patientService.add(p);
    }

    private void fillBaseUserFields(com.pidev.models.BaseUser user, int age) {
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim());

        // ✅ Store the PLAIN password – the service will hash it exactly once.
        String plainPassword = passwordField.getText().trim();
        user.setPassword(plainPassword);   // <-- was previously hashed here

        user.setAge(age);
        user.setGender(genderCombo.getValue());
        user.setActive(true);
    }
    // Add this helper method to RegisterController
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Could not load login page.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}