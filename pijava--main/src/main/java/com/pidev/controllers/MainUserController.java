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
    @FXML private Button btnForum, btnProfile;

    private Button activeButton;

    @FXML
    public void initialize() {
        // Load Forum by default
        showForum();
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
