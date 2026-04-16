package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.services.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ProfilePageController {

    @FXML private Label fullNameLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label addressLabel;
    @FXML private Label insuranceLabel;

    @FXML private VBox viewModeContainer;
    @FXML private VBox editModeContainer;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private CheckBox insuranceCheck;
    @FXML private TextField insuranceNumberField;

    @FXML private Label editErrorLabel;

    private Patient patient;
    private final PatientService patientService = new PatientService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    // Validation pattern (same as registration)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[2459]\\d{7}$");
    private static final Pattern INSURANCE_PATTERN = Pattern.compile("^[A-Z0-9]{5,20}$");

    public void setPatient(Patient patient) {
        this.patient = patient;
        refreshView();
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        firstNameField.textProperty().addListener((obs, old, val) -> validateEditForm());
        lastNameField.textProperty().addListener((obs, old, val) -> validateEditForm());
        phoneField.textProperty().addListener((obs, old, val) -> validateEditForm());
        insuranceCheck.selectedProperty().addListener((obs, old, val) -> {
            insuranceNumberField.setDisable(!val);
            if (!val) insuranceNumberField.clear();
            validateEditForm();
        });
        insuranceNumberField.textProperty().addListener((obs, old, val) -> validateEditForm());
        insuranceNumberField.setDisable(!insuranceCheck.isSelected());
    }

    private void validateEditForm() {
        boolean isValid = true;
        clearFieldErrors();

        if (firstNameField.getText().trim().isEmpty()) {
            markError(firstNameField, "First name is required");
            isValid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            markError(lastNameField, "Last name is required");
            isValid = false;
        }

        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            markError(phoneField, "8 digits, start with 2,5,9,4");
            isValid = false;
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

        editErrorLabel.setVisible(!isValid);
        if (isValid) {
            editErrorLabel.setText("");
        } else {
            editErrorLabel.setText("Please correct the highlighted fields.");
        }
    }

    private void markError(Control field, String message) {
        field.getStyleClass().add("error");
        Tooltip tooltip = new Tooltip(message);
        tooltip.getStyleClass().add("validation-tooltip");
        tooltip.setShowDelay(Duration.millis(100));
        field.setTooltip(tooltip);
    }

    private void clearFieldErrors() {
        firstNameField.getStyleClass().remove("error");
        lastNameField.getStyleClass().remove("error");
        phoneField.getStyleClass().remove("error");
        insuranceNumberField.getStyleClass().remove("error");

        firstNameField.setTooltip(null);
        lastNameField.setTooltip(null);
        phoneField.setTooltip(null);
        insuranceNumberField.setTooltip(null);
    }

    private void refreshView() {
        fullNameLabel.setText(patient.getFullName());
        memberSinceLabel.setText("Patient since recently"); // placeholder

        emailLabel.setText(patient.getEmail());
        phoneLabel.setText(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "Not provided");
        dobLabel.setText(patient.getDateOfBirth() != null ?
                patient.getDateOfBirth().format(DATE_FORMATTER) : "Not provided");
        genderLabel.setText(patient.getGender() != null ? patient.getGender() : "Not provided");
        addressLabel.setText(patient.getAddress() != null ? patient.getAddress() : "Not provided");
        insuranceLabel.setText(patient.isHasInsurance() ?
                "Yes (" + patient.getInsuranceNumber() + ")" : "No insurance");

        // Populate edit fields
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        phoneField.setText(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "");
        addressField.setText(patient.getAddress() != null ? patient.getAddress() : "");
        insuranceCheck.setSelected(patient.isHasInsurance());
        insuranceNumberField.setText(patient.getInsuranceNumber() != null ? patient.getInsuranceNumber() : "");
        insuranceNumberField.setDisable(!patient.isHasInsurance());

        clearFieldErrors();
        editErrorLabel.setVisible(false);
    }

    @FXML
    private void toggleEditMode() {
        boolean isEditMode = editModeContainer.isVisible();
        if (!isEditMode) {
            // entering edit mode, refresh from current patient data
            refreshView();
        }
        viewModeContainer.setVisible(isEditMode);
        viewModeContainer.setManaged(isEditMode);
        editModeContainer.setVisible(!isEditMode);
        editModeContainer.setManaged(!isEditMode);
    }

    @FXML
    private void saveProfile() {
        validateEditForm();
        if (editErrorLabel.isVisible()) {
            return;
        }

        patient.setFirstName(firstNameField.getText().trim());
        patient.setLastName(lastNameField.getText().trim());
        patient.setPhoneNumber(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
        patient.setAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());
        patient.setHasInsurance(insuranceCheck.isSelected());
        patient.setInsuranceNumber(insuranceCheck.isSelected() ?
                insuranceNumberField.getText().trim() : null);

        try {
            patientService.update(patient);
            refreshView();
            toggleEditMode();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile updated successfully!");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update profile: " + e.getMessage());
            alert.showAndWait();
        }
    }
}