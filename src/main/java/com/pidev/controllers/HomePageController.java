package com.pidev.controllers;

import com.pidev.models.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomePageController {

    @FXML private Label welcomeLabel;
    @FXML private Label appointmentCountLabel;

    private Patient patient;

    public void setPatient(Patient patient) {
        this.patient = patient;
        welcomeLabel.setText("Welcome back, " + patient.getFirstName() + "!");
        // In real implementation, fetch actual appointment count
        appointmentCountLabel.setText("3");
    }
}