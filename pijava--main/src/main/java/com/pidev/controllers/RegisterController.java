package com.pidev.controllers;

import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.pidev.utils.BCrypt;

import java.io.IOException;

/**
 * Controller for the registration view.
 */
public class RegisterController {

    @FXML private ToggleButton btnPatient, btnMedecin;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, specialtyField, cinField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private VBox specialtyBox;
    @FXML private Label errorMessage;

    private final PatientService patientService = new PatientService();
    private final MedecinService medecinService = new MedecinService();

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        btnPatient.setToggleGroup(group);
        btnMedecin.setToggleGroup(group);
        btnPatient.setSelected(true);
    }

    @FXML
    public void handleRoleChanged() {
        boolean isMedecin = btnMedecin.isSelected();
        specialtyBox.setManaged(isMedecin);
        specialtyBox.setVisible(isMedecin);
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
                m.setSpecialty(specialtyField.getText().trim());
                m.setCin(cinField.getText().trim());
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
        if (firstNameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showError("Veuillez remplir les champs obligatoires.");
            return false;
        }
        if (btnMedecin.isSelected() && cinField.getText().trim().isEmpty()) {
            showError("Le CIN est obligatoire pour les médecins.");
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            return false;
        }
        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit comporter au moins 6 caractères.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorMessage.setText("❌ " + message);
        errorMessage.setVisible(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
