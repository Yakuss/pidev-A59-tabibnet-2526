package com.pidev.controllers;

import com.pidev.models.Medecin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Controller for detailed medecin profile view.
 */
public class MedecinProfileController {

    @FXML private Label doctorName;
    @FXML private Label doctorSpecialty;
    @FXML private Label doctorAvatar;
    @FXML private Label verificationStatus;
    @FXML private Label aiScore;
    @FXML private Label phoneNumber;
    @FXML private Label emailAddress;
    @FXML private Label governorate;
    @FXML private Label address;
    @FXML private Label age;
    @FXML private Label gender;
    @FXML private Label cin;
    @FXML private Label education;
    @FXML private Label experience;
    @FXML private Button btnContact;
    @FXML private Button btnAppointment;
    @FXML private Button btnBack;

    private Medecin currentMedecin;

    public void setMedecin(Medecin medecin) {
        this.currentMedecin = medecin;
        populateProfile();
    }

    private void populateProfile() {
        if (currentMedecin == null) return;

        // Doctor name and avatar
        doctorName.setText("Dr. " + currentMedecin.getFullName());
        
        // Avatar initials
        String initials = "";
        if (currentMedecin.getFirstName() != null && !currentMedecin.getFirstName().isEmpty()) {
            initials += currentMedecin.getFirstName().charAt(0);
        }
        if (currentMedecin.getLastName() != null && !currentMedecin.getLastName().isEmpty()) {
            initials += currentMedecin.getLastName().charAt(0);
        }
        doctorAvatar.setText(initials.toUpperCase());

        // Specialty
        doctorSpecialty.setText(currentMedecin.getSpecialty() != null ? 
            currentMedecin.getSpecialty().getDisplayName() : "Spécialité non spécifiée");

        // Verification status
        if (currentMedecin.isVerified()) {
            verificationStatus.setText("✓ Médecin Vérifié");
            verificationStatus.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: 600;");
        } else {
            verificationStatus.setText("⏳ En cours de vérification");
            verificationStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 600;");
        }

        // AI Score
        if (currentMedecin.getAiAverageScore() != null) {
            aiScore.setText(String.format("⭐ %.1f/5.0", currentMedecin.getAiAverageScore()));
            aiScore.setVisible(true);
        } else {
            aiScore.setVisible(false);
        }

        // Contact information
        phoneNumber.setText(currentMedecin.getPhoneNumber() != null ? 
            currentMedecin.getPhoneNumber() : "Non renseigné");
        emailAddress.setText(currentMedecin.getEmail() != null ? 
            currentMedecin.getEmail() : "Non renseigné");

        // Location
        governorate.setText(currentMedecin.getGovernorate() != null ? 
            currentMedecin.getGovernorate().getDisplayName() : "Non spécifié");
        address.setText(currentMedecin.getAddress() != null && !currentMedecin.getAddress().isEmpty() ? 
            currentMedecin.getAddress() : "Non renseignée");

        // Personal info
        age.setText(currentMedecin.getAge() + " ans");
        gender.setText(currentMedecin.getGender() != null ? currentMedecin.getGender() : "Non spécifié");
        cin.setText(currentMedecin.getCin() != null ? currentMedecin.getCin() : "Non renseigné");

        // Professional info
        education.setText(currentMedecin.getEducation() != null && !currentMedecin.getEducation().isEmpty() ? 
            currentMedecin.getEducation() : "Non renseignée");
        experience.setText(currentMedecin.getExperience() != null && !currentMedecin.getExperience().isEmpty() ? 
            currentMedecin.getExperience() : "Non renseignée");
    }

    @FXML
    public void handleContact() {
        if (currentMedecin == null) return;
        
        showAlert(Alert.AlertType.INFORMATION, "Contact", 
            "Contacter Dr. " + currentMedecin.getFullName() + "\n\n" +
            "📞 " + (currentMedecin.getPhoneNumber() != null ? currentMedecin.getPhoneNumber() : "Non renseigné") + "\n" +
            "📧 " + (currentMedecin.getEmail() != null ? currentMedecin.getEmail() : "Non renseigné"));
    }

    @FXML
    public void handleAppointment() {
        if (currentMedecin == null) return;
        
        showAlert(Alert.AlertType.INFORMATION, "Rendez-vous", 
            "Prendre rendez-vous avec Dr. " + currentMedecin.getFullName() + "\n\n" +
            "Cette fonctionnalité sera bientôt disponible.");
    }

    @FXML
    public void handleBack() {
        try {
            // Load the medecin directory view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MedecinDirectoryView.fxml"));
            VBox directoryView = loader.load();
            
            // Navigate through the main content area
            VBox rootContainer = findRootContentArea();
            if (rootContainer != null) {
                rootContainer.getChildren().clear();
                rootContainer.getChildren().add(directoryView);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la liste.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la liste des médecins.");
        }
    }
    
    private VBox findRootContentArea() {
        try {
            // Start from any button and traverse up to find the main content area
            javafx.scene.Node current = btnBack;
            while (current != null) {
                current = current.getParent();
                if (current instanceof javafx.scene.layout.StackPane) {
                    // This should be the contentArea from MainUserController
                    javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane) current;
                    if (!stackPane.getChildren().isEmpty() && 
                        stackPane.getChildren().get(0) instanceof VBox) {
                        return (VBox) stackPane.getChildren().get(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
