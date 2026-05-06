package com.pidev.controllers;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;
import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for patient-facing medecin directory - allows patients to search and browse doctors.
 */
public class MedecinDirectoryController {

    @FXML private ListView<Medecin> listMedecins;
    @FXML private TextField searchName;
    @FXML private ComboBox<Specialty> searchSpecialty;
    @FXML private ComboBox<Governorate> searchGovernorate;
    @FXML private CheckBox filterVerifiedOnly;
    @FXML private Label resultsCount;
    @FXML private Button filterToggle;
    @FXML private VBox advancedFiltersPanel;

    private final MedecinService medecinService = new MedecinService();
    private ObservableList<Medecin> medecinList = FXCollections.observableArrayList();
    private FilteredList<Medecin> filteredList;
    private boolean advancedFiltersVisible = false;

    @FXML
    public void initialize() {
        setupSearchFilters();
        setupMedecinCards();
        loadMedecins();
        
        // Set up real-time filtering
        searchName.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchSpecialty.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchGovernorate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterVerifiedOnly.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupSearchFilters() {
        // Setup specialty filter with "Toutes" option
        ObservableList<Specialty> specialtyOptions = FXCollections.observableArrayList();
        specialtyOptions.add(null); // "Toutes les spécialités"
        specialtyOptions.addAll(Specialty.values());
        searchSpecialty.setItems(specialtyOptions);
        
        // Setup governorate filter with "Tous" option
        ObservableList<Governorate> governorateOptions = FXCollections.observableArrayList();
        governorateOptions.add(null); // "Tous les gouvernorats"
        governorateOptions.addAll(Governorate.values());
        searchGovernorate.setItems(governorateOptions);
        
        // Custom cell factories for display
        searchSpecialty.setCellFactory(listView -> new ListCell<Specialty>() {
            @Override
            protected void updateItem(Specialty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Toutes les spécialités");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        searchSpecialty.setButtonCell(new ListCell<Specialty>() {
            @Override
            protected void updateItem(Specialty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Toutes les spécialités");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        searchGovernorate.setCellFactory(listView -> new ListCell<Governorate>() {
            @Override
            protected void updateItem(Governorate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous les gouvernorats");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        searchGovernorate.setButtonCell(new ListCell<Governorate>() {
            @Override
            protected void updateItem(Governorate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Tous les gouvernorats");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        // Set default values
        searchSpecialty.setValue(null);
        searchGovernorate.setValue(null);
        filterVerifiedOnly.setSelected(false);
    }

    private void setupMedecinCards() {
        listMedecins.setCellFactory(listView -> new ListCell<Medecin>() {
            @Override
            protected void updateItem(Medecin medecin, boolean empty) {
                super.updateItem(medecin, empty);
                
                if (empty || medecin == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createMedecinCard(medecin));
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        });
    }

    private VBox createMedecinCard(Medecin medecin) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setPadding(new Insets(18));
        card.setStyle(
            "-fx-background-color: #0e1220;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3);"
        );

        // Header with doctor info and verification
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);

        // Doctor avatar
        Label avatar = new Label();
        String initials = "";
        if (medecin.getFirstName() != null && !medecin.getFirstName().isEmpty()) {
            initials += medecin.getFirstName().charAt(0);
        }
        if (medecin.getLastName() != null && !medecin.getLastName().isEmpty()) {
            initials += medecin.getLastName().charAt(0);
        }
        avatar.setText(initials.toUpperCase());
        avatar.setStyle(
            "-fx-background-color: #22c55e;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 50px;" +
            "-fx-min-height: 50px;" +
            "-fx-max-width: 50px;" +
            "-fx-max-height: 50px;" +
            "-fx-background-radius: 25;" +
            "-fx-alignment: center;"
        );

        // Doctor name and specialty
        VBox doctorInfo = new VBox();
        doctorInfo.setSpacing(4);
        HBox.setHgrow(doctorInfo, Priority.ALWAYS);

        Label nameLabel = new Label("Dr. " + medecin.getFullName());
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label specialtyLabel = new Label(medecin.getSpecialty() != null ? medecin.getSpecialty().getDisplayName() : "Spécialité non spécifiée");
        specialtyLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 14px; -fx-font-weight: 600;");

        // AI Score display with stars
        HBox ratingBox = new HBox();
        ratingBox.setSpacing(6);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        
        if (medecin.getAiAverageScore() != null && medecin.getAiAverageScore() > 0) {
            // Display stars based on AI score
            double aiScore = medecin.getAiAverageScore();
            int fullStars = (int) aiScore;
            boolean hasHalfStar = (aiScore - fullStars) >= 0.5;
            
            // Create stars string
            StringBuilder starsText = new StringBuilder();
            for (int i = 0; i < fullStars; i++) {
                starsText.append("⭐");
            }
            if (hasHalfStar && fullStars < 5) {
                starsText.append("⭐"); // You can use a half-star emoji if available
            }
            
            Label starsLabel = new Label(starsText.toString());
            starsLabel.setStyle("-fx-font-size: 14px;");
            
            Label aiScoreValueLabel = new Label(String.format("%.2f", aiScore));
            aiScoreValueLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 13px; -fx-font-weight: 700;");
            
            Label aiScoreBadge = new Label("🤖 AI Score");
            aiScoreBadge.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-background-color: rgba(167,139,250,0.1); -fx-padding: 2 6; -fx-background-radius: 8;");
            
            Label reviewsCountLabel = new Label("(" + (medecin.getTotalReviews() != null ? medecin.getTotalReviews() : 0) + " avis)");
            reviewsCountLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            
            ratingBox.getChildren().addAll(starsLabel, aiScoreValueLabel, aiScoreBadge, reviewsCountLabel);
        } else {
            Label noRatingLabel = new Label("Aucun avis");
            noRatingLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-style: italic;");
            ratingBox.getChildren().add(noRatingLabel);
        }

        // Experience info
        String experienceText = "Expérience: " + (medecin.getExperience() != null && !medecin.getExperience().isEmpty() ? 
            medecin.getExperience() : "Non renseignée");
        Label experienceLabel = new Label(experienceText);
        experienceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        doctorInfo.getChildren().addAll(nameLabel, specialtyLabel, ratingBox, experienceLabel);

        // Verification status
        VBox statusBox = new VBox();
        statusBox.setAlignment(Pos.TOP_RIGHT);
        statusBox.setSpacing(8);

        Label verifiedBadge = new Label();
        if (medecin.isVerified()) {
            verifiedBadge.setText("✓ Vérifié");
            verifiedBadge.setStyle(
                "-fx-background-color: rgba(34,197,94,0.2);" +
                "-fx-text-fill: #22c55e;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(34,197,94,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;"
            );
        } else {
            verifiedBadge.setText("⏳ En attente");
            verifiedBadge.setStyle(
                "-fx-background-color: rgba(245,158,11,0.2);" +
                "-fx-text-fill: #f59e0b;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(245,158,11,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;"
            );
        }

        // AI Score if available
        if (medecin.getAiAverageScore() != null) {
            Label scoreLabel = new Label(String.format("⭐ %.1f/5", medecin.getAiAverageScore()));
            scoreLabel.setStyle(
                "-fx-background-color: rgba(139,92,246,0.2);" +
                "-fx-text-fill: #8b5cf6;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 4 8;" +
                "-fx-background-radius: 12;"
            );
            statusBox.getChildren().add(scoreLabel);
        }

        statusBox.getChildren().add(verifiedBadge);

        header.getChildren().addAll(avatar, doctorInfo, statusBox);

        // Contact and location info
        HBox contactInfo = new HBox();
        contactInfo.setSpacing(24);
        contactInfo.setAlignment(Pos.CENTER_LEFT);

        if (medecin.getPhoneNumber() != null && !medecin.getPhoneNumber().isEmpty()) {
            Label phoneLabel = new Label("📞 " + medecin.getPhoneNumber());
            phoneLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            contactInfo.getChildren().add(phoneLabel);
        }

        if (medecin.getEmail() != null && !medecin.getEmail().isEmpty()) {
            Label emailLabel = new Label("📧 " + medecin.getEmail());
            emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            contactInfo.getChildren().add(emailLabel);
        }

        if (medecin.getGovernorate() != null) {
            Label locationLabel = new Label("📍 " + medecin.getGovernorate().getDisplayName());
            locationLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            contactInfo.getChildren().add(locationLabel);
        }

        // Education info
        if (medecin.getEducation() != null && !medecin.getEducation().isEmpty()) {
            Label educationLabel = new Label("🎓 " + medecin.getEducation());
            educationLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            educationLabel.setWrapText(true);
            card.getChildren().add(educationLabel);
        }

        // Action buttons
        HBox actionButtons = new HBox();
        actionButtons.setSpacing(12);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        Button contactButton = new Button("📞 Contacter");
        contactButton.setStyle(
            "-fx-background-color: #22c55e;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        );
        contactButton.setOnMouseEntered(e -> contactButton.setStyle(
            "-fx-background-color: #16a34a;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        ));
        contactButton.setOnMouseExited(e -> contactButton.setStyle(
            "-fx-background-color: #22c55e;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        ));

        Button appointmentButton = new Button("📅 Prendre RDV");
        appointmentButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #22c55e;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;"
        );
        appointmentButton.setOnMouseEntered(e -> appointmentButton.setStyle(
            "-fx-background-color: rgba(34,197,94,0.1);" +
            "-fx-text-fill: #22c55e;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;"
        ));
        appointmentButton.setOnMouseExited(e -> appointmentButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #22c55e;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;"
        ));

        // Add click handlers
        contactButton.setOnAction(e -> handleContactDoctor(medecin));
        appointmentButton.setOnAction(e -> handleBookAppointment(medecin));

        actionButtons.getChildren().addAll(contactButton, appointmentButton);

        card.getChildren().addAll(header, contactInfo, actionButtons);

        // Card click handler - navigate to detailed profile
        card.setOnMouseClicked(e -> showMedecinProfile(medecin));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(34,197,94,0.25), 14, 0, 0, 5);" +
            "-fx-cursor: hand;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #0e1220;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 3);"
        ));

        return card;
    }

    private void loadMedecins() {
        try {
            medecinList.setAll(medecinService.getAll());
            filteredList = new FilteredList<>(medecinList);
            listMedecins.setItems(filteredList);
            updateResultsCount();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les médecins: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredList == null) return;
        
        String nameQuery = searchName.getText() != null ? searchName.getText().toLowerCase().trim() : "";
        Specialty selectedSpecialty = searchSpecialty.getValue();
        Governorate selectedGovernorate = searchGovernorate.getValue();
        boolean verifiedOnly = filterVerifiedOnly.isSelected();
        
        filteredList.setPredicate(medecin -> {
            // Name filter
            boolean nameMatch = nameQuery.isEmpty() || 
                (medecin.getFirstName() != null && medecin.getFirstName().toLowerCase().contains(nameQuery)) ||
                (medecin.getLastName() != null && medecin.getLastName().toLowerCase().contains(nameQuery)) ||
                (medecin.getFullName() != null && medecin.getFullName().toLowerCase().contains(nameQuery));
            
            // Specialty filter
            boolean specialtyMatch = selectedSpecialty == null || 
                (medecin.getSpecialty() != null && medecin.getSpecialty().equals(selectedSpecialty));
            
            // Governorate filter
            boolean governorateMatch = selectedGovernorate == null || 
                (medecin.getGovernorate() != null && medecin.getGovernorate().equals(selectedGovernorate));
            
            // Verified filter
            boolean verifiedMatch = !verifiedOnly || medecin.isVerified();
            
            return nameMatch && specialtyMatch && governorateMatch && verifiedMatch;
        });
        
        updateResultsCount();
    }

    private void updateResultsCount() {
        if (filteredList != null) {
            int count = filteredList.size();
            int total = medecinList.size();
            if (count == total) {
                resultsCount.setText(count + " médecin" + (count > 1 ? "s" : "") + " disponible" + (count > 1 ? "s" : ""));
            } else {
                resultsCount.setText(count + "/" + total + " médecin" + (count > 1 ? "s" : "") + " trouvé" + (count > 1 ? "s" : ""));
            }
        }
    }

    @FXML
    public void clearFilters() {
        searchName.clear();
        searchSpecialty.setValue(null);
        searchGovernorate.setValue(null);
        filterVerifiedOnly.setSelected(false);
    }

    @FXML
    public void toggleAdvancedFilters() {
        advancedFiltersVisible = !advancedFiltersVisible;
        advancedFiltersPanel.setVisible(advancedFiltersVisible);
        advancedFiltersPanel.setManaged(advancedFiltersVisible);
        
        // Update button appearance
        if (advancedFiltersVisible) {
            filterToggle.setText("⚙️");
            filterToggle.setStyle(
                "-fx-background-color: #f59e0b;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 8 12;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;"
            );
        } else {
            filterToggle.setText("⚙️");
            filterToggle.setStyle(
                "-fx-background-color: #22c55e;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 8 12;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;"
            );
        }
    }

    private void handleContactDoctor(Medecin medecin) {
        // TODO: Implement contact functionality (email, phone, messaging)
        showAlert(Alert.AlertType.INFORMATION, "Contact", 
            "Contacter Dr. " + medecin.getFullName() + "\n\n" +
            "📞 " + (medecin.getPhoneNumber() != null ? medecin.getPhoneNumber() : "Non renseigné") + "\n" +
            "📧 " + (medecin.getEmail() != null ? medecin.getEmail() : "Non renseigné"));
    }

    private void handleBookAppointment(Medecin medecin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AppointmentBookingView.fxml"));
            VBox bookingView = loader.load();
            
            AppointmentBookingController bookingController = loader.getController();
            bookingController.setDoctor(medecin);
            
            Stage stage = new Stage();
            stage.setTitle("Prendre un Rendez-vous");
            stage.setScene(new javafx.scene.Scene(bookingView, 550, 600));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Erreur", 
                "Impossible d'ouvrir le formulaire de rendez-vous:\n" + e.getMessage());
        }
    }

    private void showMedecinProfile(Medecin medecin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MedecinProfileView.fxml"));
            VBox profileView = loader.load();
            
            // Get the controller and pass the medecin data
            MedecinProfileController profileController = loader.getController();
            profileController.setMedecin(medecin);
            
            // Navigate through the main user controller
            // Find the root content area by traversing up the scene graph
            VBox rootContainer = findRootContentArea();
            if (rootContainer != null) {
                rootContainer.getChildren().clear();
                rootContainer.getChildren().add(profileView);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers le profil.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil du médecin.");
        }
    }
    
    private VBox findRootContentArea() {
        try {
            // Start from the ListView and traverse up to find the main content area
            javafx.scene.Node current = listMedecins;
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