package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.services.PatientService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class EditPatientDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private CheckBox insuranceCheck;
    @FXML private TextField insuranceNumberField;
    @FXML private Label errorLabel;

    private Patient patient;
    private final PatientService patientService = new PatientService();
    private Runnable onSaved;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[2459]\\d{7}$");

    @FXML
    private void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        insuranceCheck.selectedProperty().addListener((obs, old, val) -> {
            insuranceNumberField.setDisable(!val);
            if (!val) insuranceNumberField.clear();
        });
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        emailField.setText(patient.getEmail());
        phoneField.setText(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "");
        addressField.setText(patient.getAddress() != null ? patient.getAddress() : "");
        ageField.setText(String.valueOf(patient.getAge()));
        genderCombo.setValue(patient.getGender());
        insuranceCheck.setSelected(patient.isHasInsurance());
        insuranceNumberField.setText(patient.getInsuranceNumber() != null ? patient.getInsuranceNumber() : "");
        insuranceNumberField.setDisable(!patient.isHasInsurance());
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void save() {
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            errorLabel.setText("First and last name are required.");
            errorLabel.setVisible(true);
            return;
        }

        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errorLabel.setText("Invalid phone number (8 digits, starts with 2,5,9,4)");
            errorLabel.setVisible(true);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
            if (age < 1 || age > 120) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid age (1-120)");
            errorLabel.setVisible(true);
            return;
        }

        patient.setFirstName(firstNameField.getText().trim());
        patient.setLastName(lastNameField.getText().trim());
        patient.setEmail(emailField.getText().trim());
        patient.setPhoneNumber(phone.isEmpty() ? null : phone);
        patient.setAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());
        patient.setAge(age);
        patient.setGender(genderCombo.getValue());
        patient.setHasInsurance(insuranceCheck.isSelected());
        patient.setInsuranceNumber(insuranceCheck.isSelected() ? insuranceNumberField.getText().trim() : null);

        try {
            patientService.update(patient);
            if (onSaved != null) onSaved.run();
            close();
        } catch (Exception e) {
            errorLabel.setText("Update failed: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        ((Stage) firstNameField.getScene().getWindow()).close();
    }
}