package com.pidev.controllers;

import com.pidev.services.AppointmentService;
import com.pidev.services.FeedbackService;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Dashboard Controller - manages sidebar navigation and content swapping.
 */
public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard, btnPatients, btnMedecins, btnAppointments, btnFeedback, btnSpecialites, btnSettings, btnLogout;
    @FXML private Label lblTotalPatients, lblTotalMedecins, lblTotalAppointments, lblTotalFeedbacks;
    @FXML private Label userNameLabel;
    @FXML private VBox dashboardHome;

    private Button activeButton;

    @FXML
    public void initialize() {
        activeButton = btnDashboard;
        displayUserInfo();
        loadStats();
        // Show the stats dashboard home by default
        showDashboardHome();
    }

    private void displayUserInfo() {
        com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
        if (user != null) {
            userNameLabel.setText("👤 " + user.getFirstName() + " " + user.getLastName());
        }
    }

    private void loadStats() {
        try {
            lblTotalPatients.setText(String.valueOf(new PatientService().getAll().size()));
        } catch (Exception e) { lblTotalPatients.setText("—"); }
        try {
            lblTotalMedecins.setText(String.valueOf(new MedecinService().getAll().size()));
        } catch (Exception e) { lblTotalMedecins.setText("—"); }
        try {
            lblTotalAppointments.setText(String.valueOf(new AppointmentService().getAll().size()));
        } catch (Exception e) { lblTotalAppointments.setText("—"); }
        try {
            lblTotalFeedbacks.setText(String.valueOf(new FeedbackService().getAll().size()));
        } catch (Exception e) { lblTotalFeedbacks.setText("—"); }
    }

    @FXML
    public void showDashboard() {
        setActiveButton(btnDashboard);
        showDashboardHome();
        loadStats();
    }

    private void showDashboardHome() {
        // Restore the FXML-defined dashboard home (stats cards + table)
        contentArea.getChildren().clear();
        if (dashboardHome != null) {
            contentArea.getChildren().add(dashboardHome);
        }
    }

    @FXML
    public void showPatients() {
        setActiveButton(btnPatients);
        loadView("/views/PatientView.fxml");
    }

    @FXML
    public void showMedecins() {
        setActiveButton(btnMedecins);
        loadView("/views/MedecinView.fxml");
    }

    @FXML
    public void showAppointments() {
        setActiveButton(btnAppointments);
        loadView("/views/AppointmentView.fxml");
    }

    @FXML
    public void showFeedback() {
        setActiveButton(btnFeedback);
        loadView("/views/FeedbackView.fxml");
    }

    @FXML
    public void showSpecialites() {
        setActiveButton(btnSpecialites);
        loadView("/views/SpecialiteView.fxml");
    }

    @FXML
    public void showSettings() {
        if (btnSettings != null) setActiveButton(btnSettings);
        contentArea.getChildren().clear();
        Label placeholder = new Label("⚙️ Les paramètres seront bientôt disponibles !");
        placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 18px; -fx-font-weight: bold;");
        contentArea.getChildren().add(placeholder);
    }



    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("❌ Error loading view: " + fxmlPath);
            e.printStackTrace();

            // Show error in content area
            Label errorLabel = new Label("❌ Erreur de chargement: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        activeButton = button;
        if (!button.getStyleClass().contains("nav-button-active")) {
            button.getStyleClass().add("nav-button-active");
        }
    }

    /**
     * Handle admin logout
     */
    @FXML
    public void handleLogout() {
        try {
            // Clear user session
            com.pidev.utils.UserSession.getInstance().cleanUserSession();
            
            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            javafx.scene.Parent loginView = loader.load();
            
            // Get the stage from the current scene
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            
            // Set the new scene
            javafx.scene.Scene scene = new javafx.scene.Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("PiDev Medical - Connexion");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
