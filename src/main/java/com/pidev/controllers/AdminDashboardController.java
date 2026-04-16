package com.pidev.controllers;

import com.pidev.models.Admin;
import com.pidev.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button homeNavBtn;
    @FXML private Button profileNavBtn;
    @FXML private Button doctorsNavBtn;
    @FXML private Button patientsNavBtn;

    private Admin currentAdmin;

    @FXML
    private void initialize() {
        currentAdmin = (Admin) SessionManager.getInstance().getCurrentUser();
        showHomePage();
        setActiveNavButton(homeNavBtn);
    }

    @FXML
    private void showHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/AdminHomePage.fxml"));
            Parent page = loader.load();
            AdminHomePageController controller = loader.getController();
            controller.setAdmin(currentAdmin);
            contentArea.getChildren().setAll(page);
            setActiveNavButton(homeNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/AdminProfilePage.fxml"));
            Parent page = loader.load();
            AdminProfilePageController controller = loader.getController();
            controller.setAdmin(currentAdmin);
            contentArea.getChildren().setAll(page);
            setActiveNavButton(profileNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showManageDoctorsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/ManageDoctorsPage.fxml"));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            setActiveNavButton(doctorsNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showManagePatientsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/ManagePatientsPage.fxml"));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            setActiveNavButton(patientsNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveNavButton(Button activeBtn) {
        homeNavBtn.getStyleClass().remove("selected");
        profileNavBtn.getStyleClass().remove("selected");
        doctorsNavBtn.getStyleClass().remove("selected");
        patientsNavBtn.getStyleClass().remove("selected");
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