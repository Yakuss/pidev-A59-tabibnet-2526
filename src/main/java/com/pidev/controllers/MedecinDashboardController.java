package com.pidev.controllers;

import com.pidev.models.Medecin;
import com.pidev.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MedecinDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button homeNavBtn;
    @FXML private Button profileNavBtn;

    private Medecin currentMedecin;

    @FXML
    private void initialize() {
        currentMedecin = (Medecin) SessionManager.getInstance().getCurrentUser();
        showHomePage();
        setActiveNavButton(homeNavBtn);
    }

    @FXML
    private void showHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/medecin/DoctorHomePage.fxml"));
            Parent homePage = loader.load();
            DoctorHomePageController controller = loader.getController();
            controller.setMedecin(currentMedecin);
            contentArea.getChildren().setAll(homePage);
            setActiveNavButton(homeNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/medecin/DoctorProfilePage.fxml"));
            Parent profilePage = loader.load();
            DoctorProfilePageController controller = loader.getController();
            controller.setMedecin(currentMedecin);
            contentArea.getChildren().setAll(profilePage);
            setActiveNavButton(profileNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveNavButton(Button activeBtn) {
        homeNavBtn.getStyleClass().remove("selected");
        profileNavBtn.getStyleClass().remove("selected");
        activeBtn.getStyleClass().add("selected");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}