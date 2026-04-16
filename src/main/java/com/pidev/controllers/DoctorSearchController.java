package com.pidev.controllers;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorSearchController {

    @FXML private TextField nameFilterField;
    @FXML private ComboBox<String> specialtyFilterCombo;
    @FXML private ComboBox<String> governorateFilterCombo;
    @FXML private VBox resultsContainer;
    @FXML private Label resultsCountLabel;

    private final MedecinService medecinService = new MedecinService();
    private List<Medecin> allDoctors;

    @FXML
    private void initialize() {
        specialtyFilterCombo.setItems(FXCollections.observableArrayList(Specialty.getChoices().keySet()));
        governorateFilterCombo.setItems(FXCollections.observableArrayList(Governorate.getChoices().keySet()));

        loadAllDoctors();
    }

    private void loadAllDoctors() {
        try {
            allDoctors = medecinService.findAll();
            displayDoctors(allDoctors);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String nameFilter = nameFilterField.getText().trim().toLowerCase();
        String specialtyFilter = specialtyFilterCombo.getValue();
        String governorateFilter = governorateFilterCombo.getValue();

        List<Medecin> filtered = allDoctors.stream()
                .filter(d -> {
                    boolean nameMatch = nameFilter.isEmpty() ||
                            d.getFullName().toLowerCase().contains(nameFilter);
                    boolean specialtyMatch = specialtyFilter == null ||
                            (d.getSpecialty() != null && d.getSpecialty().getDisplayName().equals(specialtyFilter));
                    boolean govMatch = governorateFilter == null ||
                            (d.getGovernorate() != null && d.getGovernorate().getDisplayName().equals(governorateFilter));
                    return nameMatch && specialtyMatch && govMatch;
                })
                .collect(Collectors.toList());
        displayDoctors(filtered);
    }

    @FXML
    private void handleReset() {
        nameFilterField.clear();
        specialtyFilterCombo.setValue(null);
        governorateFilterCombo.setValue(null);
        displayDoctors(allDoctors);
    }

    private void displayDoctors(List<Medecin> doctors) {
        resultsContainer.getChildren().clear();
        resultsCountLabel.setText(doctors.size() + " doctor" + (doctors.size() != 1 ? "s" : "") + " found");

        for (Medecin doctor : doctors) {
            resultsContainer.getChildren().add(createDoctorCard(doctor));
        }
    }

    private VBox createDoctorCard(Medecin doctor) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 2, 0);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.15), 12, 0, 4, 0); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 2, 0);"));

        // Top row: Name and verification badge
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Dr. " + doctor.getFullName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        topRow.getChildren().add(nameLabel);

        if (doctor.isVerified()) {
            Label verifiedBadge = new Label("✓ Verified");
            verifiedBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; " +
                    "-fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 20;");
            topRow.getChildren().add(verifiedBadge);
        }

        // Specialty with icon
        HBox specialtyBox = new HBox(8);
        specialtyBox.setAlignment(Pos.CENTER_LEFT);
        Label specialtyIcon = new Label("🩺");
        specialtyIcon.setStyle("-fx-font-size: 16px;");
        Label specialtyLabel = new Label(doctor.getSpecialty() != null ?
                doctor.getSpecialty().getDisplayName() : "Not specified");
        specialtyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        specialtyBox.getChildren().addAll(specialtyIcon, specialtyLabel);

        // Governorate with icon
        HBox govBox = new HBox(8);
        govBox.setAlignment(Pos.CENTER_LEFT);
        Label govIcon = new Label("📍");
        govIcon.setStyle("-fx-font-size: 16px;");
        Label govLabel = new Label(doctor.getGovernorate() != null ?
                doctor.getGovernorate().getDisplayName() : "Not specified");
        govLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        govBox.getChildren().addAll(govIcon, govLabel);

        // Bottom row: View Profile button
        Button viewButton = new Button("View Full Profile →");
        viewButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        viewButton.setOnAction(e -> openDoctorProfile(doctor));
        HBox buttonBox = new HBox(viewButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(topRow, specialtyBox, govBox, new Separator(), buttonBox);
        return card;
    }

    private void openDoctorProfile(Medecin doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/patient/DoctorProfileView.fxml"));
            Parent root = loader.load();
            DoctorProfileViewController controller = loader.getController();
            controller.setDoctor(doctor);

            Stage stage = new Stage();
            stage.setTitle("Dr. " + doctor.getFullName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}