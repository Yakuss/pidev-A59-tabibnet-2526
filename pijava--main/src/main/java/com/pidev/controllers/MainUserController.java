package com.pidev.controllers;

import com.pidev.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the User Space (Espace Supérieur).
 */
public class MainUserController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnForum, btnProfile, btnHome, btnMedecins, btnAnnuaire, btnAppointments;
    @FXML
    private MenuButton menuDossierMedical;

    private static MainUserController instance;
    private Button activeButton;

    public static MainUserController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        // Load Home page by default
        showHome();

        // Role-based menu visibility
        com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
        if (menuDossierMedical != null) {
            boolean isDoctor = user != null && user.getRoles() != null && user.getRoles().contains("ROLE_MEDECIN");
            menuDossierMedical.setVisible(isDoctor);
            menuDossierMedical.setManaged(isDoctor);
        }
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
    public void showDocuments() {
        setActiveButton(null);
        com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
        if (user != null) {
            int patientId = com.pidev.utils.UserSession.getInstance().getSelectedPatientId() != null
                    ? com.pidev.utils.UserSession.getInstance().getSelectedPatientId()
                    : user.getId();
            int medecinId = user.getRoles().contains("ROLE_MEDECIN") ? user.getId() : 1;
            int rendezVousId = com.pidev.utils.UserSession.getInstance().getSelectedAppointmentId() != null
                    ? com.pidev.utils.UserSession.getInstance().getSelectedAppointmentId()
                    : 0;

            loadDossierMedicalView("/views/DossierMedicalView.fxml", patientId, medecinId, rendezVousId);
        }
    }

    @FXML
    public void showOrdonnances() {
        setActiveButton(null);
        com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
        if (user != null) {
            if (user.getRoles().contains("ROLE_MEDECIN")) {
                com.pidev.utils.UserSession.getInstance().setSelectedMedecinId(user.getId());
            } else {
                com.pidev.utils.UserSession.getInstance().setSelectedPatientId(user.getId());
            }
        }
        loadView("/views/DashboardView.fxml");
    }

    @FXML
    public void showDossierMedical() {
        showDocuments();
    }

    @FXML
    public void showOrdonnanceRapport() {
        showOrdonnances();
    }

    @FXML
    public void showAssistanceAI() {
        setActiveButton(null);
        loadView("/views/AiAssistantView.fxml");
    }

    public void showDocumentDetails(com.pidev.models.Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DocumentDetailsView.fxml"));
            Node view = loader.load();
            DocumentDetailsController controller = loader.getController();
            controller.setDocument(doc);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRapportDetails(com.pidev.models.Rapport r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RapportDetailsView.fxml"));
            Node view = loader.load();
            RapportDetailsController controller = loader.getController();
            controller.setRapport(r);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRapportForm() {
        loadView("/views/RapportFormView.fxml");
    }

    public void showRapportForm(com.pidev.models.Rapport r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RapportFormView.fxml"));
            Node view = loader.load();
            RapportFormController controller = loader.getController();
            controller.setRapport(r); // Enabled editing

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showOrdonnanceDetails(com.pidev.models.Ordonnance o) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OrdonnanceDetailsView.fxml"));
            Node view = loader.load();
            OrdonnanceDetailsController controller = loader.getController();
            controller.setOrdonnance(o);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showOrdonnanceForm() {
        loadView("/views/OrdonnanceFormView.fxml");
    }

    public void showOrdonnanceForm(com.pidev.models.Ordonnance o) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OrdonnanceFormView.fxml"));
            Node view = loader.load();
            OrdonnanceFormController controller = loader.getController();
            controller.setOrdonnance(o); // Enabled editing

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void loadDossierMedicalView(String fxmlPath, int patientId, int medecinId, int rendezVousId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            DossierMedicalController controller = loader.getController();
            controller.initData(patientId, medecinId, rendezVousId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            loadView(fxmlPath); // Fallback
        }
    }

    private void loadView(String fxmlPath) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("❌ Error loading user view: " + fxmlPath);
            e.printStackTrace();

            // Show alert for debugging
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Erreur de chargement");
                alert.setHeaderText("Erreur critique de chargement");
                alert.setContentText("Chemin: " + fxmlPath + "\n\nDétails: " + e.toString());
                alert.showAndWait();
            });
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
