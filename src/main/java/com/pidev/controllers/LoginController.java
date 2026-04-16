package com.pidev.controllers;

import com.pidev.models.BaseUser;
import com.pidev.services.AuthService;
import com.pidev.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim(); // ✅ Trim to remove accidental spaces

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }

        try {
            BaseUser user = authService.login(email, password);
            if (user != null) {
                if (!user.isActive()) {
                    showError("Your account is inactive.");
                    return;
                }
                SessionManager.getInstance().setCurrentUser(user);
                navigateToDashboard(user);
            } else {
                showError("Invalid email or password.");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Create Account");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Could not load registration page.");
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(BaseUser user) {
        try {
            String fxmlFile;
            String title;
            switch (user.getDiscriminator()) {
                case "admin":
                    fxmlFile = "/views/AdminDashboard.fxml";
                    title = "Admin Dashboard";
                    break;
                case "medecin":
                    fxmlFile = "/views/MedecinDashboard.fxml";
                    title = "Doctor Dashboard";
                    break;
                case "patient":
                    fxmlFile = "/views/PatientDashboard.fxml";
                    title = "Patient Dashboard";
                    break;
                default:
                    throw new IllegalStateException("Unknown user type: " + user.getDiscriminator());
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Could not load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}