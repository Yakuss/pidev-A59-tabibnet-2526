package com.pidev.controllers;

import com.pidev.models.BaseUser;
import com.pidev.services.AuthService;
import com.pidev.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Login view.
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorMessage;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Hide error message initially
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
        }
        
        // Clear error message when user starts typing
        if (emailField != null) {
            emailField.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        }
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            BaseUser user = authService.login(email, password);
            if (user != null) {
                UserSession.getInstance().setUser(user);
                System.out.println("✅ Login successful: " + user.getFullName());
                navigateAfterLogin();
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            // Check if it's an inactive account error
            if (e.getMessage() != null && e.getMessage().startsWith("ACCOUNT_INACTIVE:")) {
                // Extract the user-friendly message after the prefix
                String userMessage = e.getMessage().substring("ACCOUNT_INACTIVE:".length()).trim();
                showError(userMessage);
            } else {
                // Generic error for other exceptions
                e.printStackTrace();
                showError("Erreur de connexion. Veuillez réessayer.");
            }
        }
    }

    @FXML
    public void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ForgotPasswordView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateAfterLogin() {
        try {
            com.pidev.models.BaseUser user = UserSession.getInstance().getUser();
            String fxmlPath = "/views/MainUserView.fxml"; // Default for Users

            // Role check: If Admin, go to Dashboard
            if (user != null && user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN")) {
                fxmlPath = "/views/Dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(scene);

            // Set a proper window size and maximize for the dashboard
            javafx.geometry.Rectangle2D screenBounds =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setWidth(Math.min(1400, screenBounds.getWidth()));
            stage.setHeight(Math.min(900, screenBounds.getHeight()));
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText("❌ " + message);
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
        }
    }

    private void hideError() {
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
        }
    }
}
