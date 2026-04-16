package com.pidev.controllers;

import com.pidev.models.Admin;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminHomePageController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label pendingDoctorsLabel;

    private Admin admin;
    private final MedecinService medecinService = new MedecinService();
    private final PatientService patientService = new PatientService();

    public void setAdmin(Admin admin) {
        this.admin = admin;
        welcomeLabel.setText("Welcome, " + admin.getFullName() + "!");
        loadStats();
    }

    private void loadStats() {
        try {
            int totalDoctors = medecinService.findAll().size();
            int pendingDoctors = (int) medecinService.findAll().stream().filter(d -> !d.isVerified()).count();
            int totalPatients = patientService.findAll().size();

            totalDoctorsLabel.setText(String.valueOf(totalDoctors));
            pendingDoctorsLabel.setText(String.valueOf(pendingDoctors));
            totalPatientsLabel.setText(String.valueOf(totalPatients));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToManageDoctors() {
        // Navigate to manage doctors (handled by parent dashboard)
        ((AdminDashboardController) welcomeLabel.getScene().getWindow().getUserData()).showManageDoctorsPage();
    }

    @FXML
    private void goToManagePatients() {
        ((AdminDashboardController) welcomeLabel.getScene().getWindow().getUserData()).showManagePatientsPage();
    }
}