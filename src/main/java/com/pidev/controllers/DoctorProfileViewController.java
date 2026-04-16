package com.pidev.controllers;

import com.pidev.models.Medecin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DoctorProfileViewController {

    @FXML private Label fullNameLabel;
    @FXML private Label verifiedBadge;
    @FXML private Label specialtyLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label governorateLabel;
    @FXML private Label cinLabel;
    @FXML private Label educationLabel;
    @FXML private Label experienceLabel;
    @FXML private Label feeLabel;

    public void setDoctor(Medecin doctor) {
        fullNameLabel.setText("Dr. " + doctor.getFullName());
        specialtyLabel.setText(doctor.getSpecialty() != null ? doctor.getSpecialty().getDisplayName() : "Specialty not specified");
        emailLabel.setText(doctor.getEmail());
        phoneLabel.setText(doctor.getPhoneNumber() != null ? doctor.getPhoneNumber() : "Not provided");
        addressLabel.setText(doctor.getAddress() != null ? doctor.getAddress() : "Not provided");
        governorateLabel.setText(doctor.getGovernorate() != null ? doctor.getGovernorate().getDisplayName() : "Not specified");
        cinLabel.setText(doctor.getCin() != null ? doctor.getCin() : "Not provided");
        educationLabel.setText(doctor.getEducation() != null ? doctor.getEducation() : "Not provided");
        experienceLabel.setText(doctor.getExperience() != null ? doctor.getExperience() : "Not provided");
        feeLabel.setText(doctor.getConsultationFee() != null ? String.format("%.2f TND", doctor.getConsultationFee()) : "Not specified");

        if (doctor.isVerified()) {
            verifiedBadge.setText("✓ Verified");
            verifiedBadge.setVisible(true);
        } else {
            verifiedBadge.setVisible(false);
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) fullNameLabel.getScene().getWindow();
        stage.close();
    }
}