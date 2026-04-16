package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PatientDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button homeNavBtn;
    @FXML private Button profileNavBtn;
    @FXML private Button findDoctorsNavBtn;


    private Patient currentPatient;

    @FXML
    private void initialize() {
        currentPatient = (Patient) SessionManager.getInstance().getCurrentUser();
        showHomePage();
        setActiveNavButton(homeNavBtn);
    }

    @FXML
    private void showHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/patient/HomePage.fxml"));
            Parent homePage = loader.load();
            HomePageController controller = loader.getController();
            controller.setPatient(currentPatient);
            contentArea.getChildren().setAll(homePage);
            setActiveNavButton(homeNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/patient/ProfilePage.fxml"));
            Parent profilePage = loader.load();
            ProfilePageController controller = loader.getController();
            controller.setPatient(currentPatient);
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

    @FXML
    private void showFindDoctorsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/patient/DoctorSearchPage.fxml"));
            Parent searchPage = loader.load();
            contentArea.getChildren().setAll(searchPage);
            setActiveNavButton(findDoctorsNavBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}