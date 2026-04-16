package com.pidev.controllers;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.regex.Pattern;

public class DoctorProfilePageController {

    @FXML private Label fullNameLabel;
    @FXML private Label specialtyLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label cinLabel;
    @FXML private Label addressLabel;
    @FXML private Label governorateLabel;
    @FXML private Label feeLabel;
    @FXML private Label educationLabel;
    @FXML private Label experienceLabel;
    @FXML private Label verifiedLabel;

    @FXML private VBox viewModeContainer;
    @FXML private VBox editModeContainer;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> governorateCombo;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private TextField feeField;
    @FXML private TextArea educationArea;
    @FXML private TextArea experienceArea;

    @FXML private Label editErrorLabel;

    private Medecin medecin;
    private final MedecinService medecinService = new MedecinService();

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[2459]\\d{7}$");

    @FXML
    private void initialize() {
        governorateCombo.setItems(FXCollections.observableArrayList(Governorate.getChoices().keySet()));
        specialtyCombo.setItems(FXCollections.observableArrayList(Specialty.getChoices().keySet()));
        setupValidationListeners();
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
        refreshView();
    }

    private void setupValidationListeners() {
        firstNameField.textProperty().addListener((obs, old, val) -> validateEditForm());
        lastNameField.textProperty().addListener((obs, old, val) -> validateEditForm());
        phoneField.textProperty().addListener((obs, old, val) -> validateEditForm());
        addressField.textProperty().addListener((obs, old, val) -> validateEditForm());
        governorateCombo.valueProperty().addListener((obs, old, val) -> validateEditForm());
        specialtyCombo.valueProperty().addListener((obs, old, val) -> validateEditForm());
        // fee, education, experience are optional – no validation
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
        if (phone.isEmpty()) {
            markError(phoneField, "Phone is required");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            markError(phoneField, "8 digits, start with 2,5,9,4");
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
        if (specialtyCombo.getValue() == null) {
            markError(specialtyCombo, "Select specialty");
            isValid = false;
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
        addressField.getStyleClass().remove("error");
        governorateCombo.getStyleClass().remove("error");
        specialtyCombo.getStyleClass().remove("error");

        firstNameField.setTooltip(null);
        lastNameField.setTooltip(null);
        phoneField.setTooltip(null);
        addressField.setTooltip(null);
        governorateCombo.setTooltip(null);
        specialtyCombo.setTooltip(null);
    }

    private void refreshView() {
        fullNameLabel.setText("Dr. " + medecin.getFullName());
        specialtyLabel.setText(medecin.getSpecialty() != null ? medecin.getSpecialty().getDisplayName() : "Not specified");
        emailLabel.setText(medecin.getEmail());
        phoneLabel.setText(medecin.getPhoneNumber() != null ? medecin.getPhoneNumber() : "Not provided");
        cinLabel.setText(medecin.getCin() != null ? medecin.getCin() : "Not provided");
        addressLabel.setText(medecin.getAddress() != null ? medecin.getAddress() : "Not provided");
        governorateLabel.setText(medecin.getGovernorate() != null ? medecin.getGovernorate().getDisplayName() : "Not specified");
        feeLabel.setText(medecin.getConsultationFee() != null ? String.format("%.2f TND", medecin.getConsultationFee()) : "Not set");
        educationLabel.setText(medecin.getEducation() != null ? medecin.getEducation() : "Not provided");
        experienceLabel.setText(medecin.getExperience() != null ? medecin.getExperience() : "Not provided");
        verifiedLabel.setText(medecin.isVerified() ? "✅ Verified" : "⏳ Pending Verification");

        // Populate edit fields
        firstNameField.setText(medecin.getFirstName());
        lastNameField.setText(medecin.getLastName());
        phoneField.setText(medecin.getPhoneNumber() != null ? medecin.getPhoneNumber() : "");
        addressField.setText(medecin.getAddress() != null ? medecin.getAddress() : "");
        governorateCombo.setValue(medecin.getGovernorate() != null ? medecin.getGovernorate().getDisplayName() : null);
        specialtyCombo.setValue(medecin.getSpecialty() != null ? medecin.getSpecialty().getDisplayName() : null);
        feeField.setText(medecin.getConsultationFee() != null ? medecin.getConsultationFee().toString() : "");
        educationArea.setText(medecin.getEducation() != null ? medecin.getEducation() : "");
        experienceArea.setText(medecin.getExperience() != null ? medecin.getExperience() : "");

        clearFieldErrors();
        editErrorLabel.setVisible(false);
    }

    @FXML
    private void toggleEditMode() {
        boolean isEditMode = editModeContainer.isVisible();
        if (!isEditMode) {
            refreshView(); // reset fields to current data
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

        medecin.setFirstName(firstNameField.getText().trim());
        medecin.setLastName(lastNameField.getText().trim());
        medecin.setPhoneNumber(phoneField.getText().trim());
        medecin.setAddress(addressField.getText().trim());
        medecin.setGovernorate(Governorate.fromDisplayName(governorateCombo.getValue()));
        medecin.setSpecialty(Specialty.fromDisplayName(specialtyCombo.getValue()));

        String feeText = feeField.getText().trim();
        if (!feeText.isEmpty()) {
            try {
                medecin.setConsultationFee(Double.parseDouble(feeText));
            } catch (NumberFormatException e) {
                markError(feeField, "Invalid number");
                return;
            }
        } else {
            medecin.setConsultationFee(null);
        }

        String education = educationArea.getText().trim();
        medecin.setEducation(education.isEmpty() ? null : education);
        String experience = experienceArea.getText().trim();
        medecin.setExperience(experience.isEmpty() ? null : experience);

        try {
            medecinService.update(medecin);
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