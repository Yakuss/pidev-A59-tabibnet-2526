package com.pidev.controllers;

import com.pidev.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the User Space (Espace Supérieur).
 */
public class MainUserController {

    @FXML private StackPane contentArea;
    @FXML private Button btnForum, btnProfile, btnHome, btnMedecins, btnAnnuaire, btnAppointments;

    private Button activeButton;

    @FXML
    public void initialize() {
        // Load Home page by default
        showHome();
    }

    @FXML
    public void showHome() {
        setActiveButton(btnHome);
        loadView("/views/HomeView.fxml");
    }

    @FXML
    public void showForum() {
        setActiveButton(btnForum);
        loadView("/views/ForumView.fxml");
    }

    @FXML
    public void showProfile() {
        setActiveButton(btnProfile);
        loadView("/views/ProfileView.fxml");
    }

    @FXML
    public void showMedecins() {
        setActiveButton(btnMedecins);
        loadView("/views/MedecinDirectoryView.fxml");
    }

    @FXML
    public void showAnnuaire() {
        setActiveButton(btnAnnuaire);
        loadView("/views/AnnuaireView.fxml");
    }

    @FXML
    public void showAppointments() {
        setActiveButton(btnAppointments);
        
        // Check if user is a doctor or patient
        com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
        String viewPath = "/views/PatientAppointmentsView.fxml";
        
        if (user != null && user.getRoles() != null && user.getRoles().contains("ROLE_MEDECIN")) {
            // Doctor view
            viewPath = "/views/DoctorAppointmentsView.fxml";
        }
        
        loadView(viewPath);
    }

    @FXML
    public void handleLogout() {
        UserSession.getInstance().cleanUserSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("❌ Error loading user view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-user-active");
        }
        activeButton = button;
        if (button != null && !button.getStyleClass().contains("nav-button-user-active")) {
            button.getStyleClass().add("nav-button-user-active");
        }
    }
}
