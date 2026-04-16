package com.pidev.controllers;

import com.pidev.models.Medecin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DoctorHomePageController {

    @FXML private Label welcomeLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label patientCountLabel;
    @FXML private Label aiScoreLabel; // Remove this if you don't want it

    private Medecin medecin;

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
        welcomeLabel.setText("Welcome back, Dr. " + medecin.getLastName() + "!");

        // Placeholder data – replace with real service calls later
        appointmentCountLabel.setText("5");
        patientCountLabel.setText("42");

        // No AI score available; you can hide or repurpose the label
        if (aiScoreLabel != null) {
            aiScoreLabel.setVisible(false); // or set to empty
        }
    }
}