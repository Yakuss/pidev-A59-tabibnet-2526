package com.pidev.controllers;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditDoctorDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> governorateCombo;
    @FXML private ComboBox<String> specialtyCombo;
    @FXML private TextField feeField;
    @FXML private TextArea educationArea;
    @FXML private TextArea experienceArea;
    @FXML private Label errorLabel;

    private Medecin doctor;
    private final MedecinService medecinService = new MedecinService();
    private Runnable onSaved;

    @FXML
    private void initialize() {
        governorateCombo.setItems(FXCollections.observableArrayList(Governorate.getChoices().keySet()));
        specialtyCombo.setItems(FXCollections.observableArrayList(Specialty.getChoices().keySet()));
    }

    public void setDoctor(Medecin doctor) {
        this.doctor = doctor;
        firstNameField.setText(doctor.getFirstName());
        lastNameField.setText(doctor.getLastName());
        emailField.setText(doctor.getEmail());
        phoneField.setText(doctor.getPhoneNumber() != null ? doctor.getPhoneNumber() : "");
        cinField.setText(doctor.getCin() != null ? doctor.getCin() : "");
        addressField.setText(doctor.getAddress() != null ? doctor.getAddress() : "");
        governorateCombo.setValue(doctor.getGovernorate() != null ? doctor.getGovernorate().getDisplayName() : null);
        specialtyCombo.setValue(doctor.getSpecialty() != null ? doctor.getSpecialty().getDisplayName() : null);
        feeField.setText(doctor.getConsultationFee() != null ? doctor.getConsultationFee().toString() : "");
        educationArea.setText(doctor.getEducation() != null ? doctor.getEducation() : "");
        experienceArea.setText(doctor.getExperience() != null ? doctor.getExperience() : "");
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

        doctor.setFirstName(firstNameField.getText().trim());
        doctor.setLastName(lastNameField.getText().trim());
        doctor.setEmail(emailField.getText().trim());
        doctor.setPhoneNumber(phoneField.getText().trim());
        doctor.setCin(cinField.getText().trim());
        doctor.setAddress(addressField.getText().trim());
        doctor.setGovernorate(Governorate.fromDisplayName(governorateCombo.getValue()));
        doctor.setSpecialty(Specialty.fromDisplayName(specialtyCombo.getValue()));

        String feeText = feeField.getText().trim();
        if (!feeText.isEmpty()) {
            try {
                doctor.setConsultationFee(Double.parseDouble(feeText));
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid consultation fee.");
                errorLabel.setVisible(true);
                return;
            }
        } else {
            doctor.setConsultationFee(null);
        }

        doctor.setEducation(educationArea.getText().trim().isEmpty() ? null : educationArea.getText().trim());
        doctor.setExperience(experienceArea.getText().trim().isEmpty() ? null : experienceArea.getText().trim());

        try {
            medecinService.update(doctor);
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