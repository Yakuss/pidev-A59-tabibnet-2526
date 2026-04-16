package com.pidev.controllers;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import com.pidev.utils.ExportUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ManageDoctorsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> specialtyFilterCombo;
    @FXML private ComboBox<String> verifiedFilterCombo;
    @FXML private ListView<Medecin> doctorsListView;

    private final MedecinService medecinService = new MedecinService();
    private ObservableList<Medecin> masterList = FXCollections.observableArrayList();
    private FilteredList<Medecin> filteredList;

    @FXML
    private void initialize() {
        specialtyFilterCombo.setItems(FXCollections.observableArrayList(Specialty.getChoices().keySet()));
        specialtyFilterCombo.getItems().add(0, "All Specialties");
        verifiedFilterCombo.setItems(FXCollections.observableArrayList("All Status", "Verified", "Pending"));
        specialtyFilterCombo.getSelectionModel().selectFirst();
        verifiedFilterCombo.getSelectionModel().selectFirst();

        filteredList = new FilteredList<>(masterList, p -> true);
        doctorsListView.setItems(filteredList);
        doctorsListView.setCellFactory(lv -> new DoctorCardCell());

        searchField.textProperty().addListener((obs, old, val) -> filterDoctors());
        specialtyFilterCombo.valueProperty().addListener((obs, old, val) -> filterDoctors());
        verifiedFilterCombo.valueProperty().addListener((obs, old, val) -> filterDoctors());

        loadDoctors();
    }

    private void loadDoctors() {
        try {
            masterList.setAll(medecinService.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshList() {
        loadDoctors();
        filterDoctors();
    }

    private void filterDoctors() {
        String search = searchField.getText().trim().toLowerCase();
        String specialty = specialtyFilterCombo.getValue();
        String verified = verifiedFilterCombo.getValue();

        Predicate<Medecin> predicate = doctor -> {
            boolean nameMatch = search.isEmpty() ||
                    doctor.getFullName().toLowerCase().contains(search) ||
                    doctor.getEmail().toLowerCase().contains(search);
            boolean specialtyMatch = specialty == null || specialty.equals("All Specialties") ||
                    (doctor.getSpecialty() != null && doctor.getSpecialty().getDisplayName().equals(specialty));
            boolean verifiedMatch = verified == null || verified.equals("All Status") ||
                    (verified.equals("Verified") && doctor.isVerified()) ||
                    (verified.equals("Pending") && !doctor.isVerified());
            return nameMatch && specialtyMatch && verifiedMatch;
        };
        filteredList.setPredicate(predicate);
    }

    private class DoctorCardCell extends ListCell<Medecin> {
        private final VBox card;
        private final Label nameLabel;
        private final Label specialtyLabel;
        private final Label governorateLabel;
        private final Label emailLabel;
        private final Label verifiedBadge;
        private final Button editBtn;
        private final Button verifyBtn;
        private final Button deleteBtn;
        private final HBox actionsBox;

        public DoctorCardCell() {
            card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 2, 0);");

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

            specialtyLabel = new Label();
            specialtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

            governorateLabel = new Label();
            governorateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

            emailLabel = new Label();
            emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            verifiedBadge = new Label();
            verifiedBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; " +
                    "-fx-padding: 3 8; -fx-background-radius: 20; -fx-font-weight: bold;");

            editBtn = new Button("✏️ Edit");
            editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");

            verifyBtn = new Button("✓ Verify");
            verifyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");

            deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");

            actionsBox = new HBox(8, editBtn, verifyBtn, deleteBtn);
            actionsBox.setAlignment(Pos.CENTER_RIGHT);

            HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            topRow.getChildren().addAll(nameLabel, verifiedBadge);

            HBox detailsRow = new HBox(15);
            detailsRow.getChildren().addAll(specialtyLabel, new Label("•"), governorateLabel);

            card.getChildren().addAll(topRow, detailsRow, emailLabel, actionsBox);
        }

        @Override
        protected void updateItem(Medecin doctor, boolean empty) {
            super.updateItem(doctor, empty);
            if (empty || doctor == null) {
                setGraphic(null);
            } else {
                nameLabel.setText("Dr. " + doctor.getFullName());
                specialtyLabel.setText("🩺 " + (doctor.getSpecialty() != null ? doctor.getSpecialty().getDisplayName() : "N/A"));
                governorateLabel.setText("📍 " + (doctor.getGovernorate() != null ? doctor.getGovernorate().getDisplayName() : "N/A"));
                emailLabel.setText("📧 " + doctor.getEmail());
                verifiedBadge.setText(doctor.isVerified() ? "Verified" : "Pending");
                verifiedBadge.setStyle(doctor.isVerified() ?
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 20;" :
                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 20;");

                editBtn.setOnAction(e -> openEditDialog(doctor));

                if (!doctor.isVerified()) {
                    verifyBtn.setDisable(false);
                    verifyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");
                    verifyBtn.setOnAction(e -> {
                        doctor.setVerified(true);
                        try {
                            medecinService.update(doctor);
                            refreshList();
                            showAlert(Alert.AlertType.INFORMATION, "Doctor Verified", "Dr. " + doctor.getFullName() + " is now verified.");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } else {
                    verifyBtn.setDisable(true);
                    verifyBtn.setStyle("-fx-background-color: #9ca3af; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
                }

                deleteBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Doctor");
                    confirm.setHeaderText("Delete Dr. " + doctor.getFullName() + "?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                medecinService.delete(doctor.getId());
                                refreshList();
                                showAlert(Alert.AlertType.INFORMATION, "Doctor Deleted", "Dr. " + doctor.getFullName() + " has been removed.");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });

                setGraphic(card);
            }
        }
    }

    private void openEditDialog(Medecin doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditDoctorDialog.fxml"));
            Parent root = loader.load();
            EditDoctorDialogController controller = loader.getController();
            controller.setDoctor(doctor);
            controller.setOnSaved(() -> {
                refreshList();
                showAlert(Alert.AlertType.INFORMATION, "Doctor Updated", "Dr. " + doctor.getFullName() + " has been updated.");
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToCSV() {
        String[] headers = {"ID", "First Name", "Last Name", "Email", "Specialty", "Governorate", "Verified", "Phone", "CIN"};
        ExportUtils.exportToCSV(doctorsListView.getScene().getWindow(), "doctors_export",
                filteredList, headers, d -> new String[]{
                        String.valueOf(d.getId()), d.getFirstName(), d.getLastName(), d.getEmail(),
                        d.getSpecialty() != null ? d.getSpecialty().getDisplayName() : "",
                        d.getGovernorate() != null ? d.getGovernorate().getDisplayName() : "",
                        d.isVerified() ? "Yes" : "No",
                        d.getPhoneNumber() != null ? d.getPhoneNumber() : "",
                        d.getCin() != null ? d.getCin() : ""
                });
        showAlert(Alert.AlertType.INFORMATION, "Export Complete", "Doctors exported to CSV successfully.");
    }

    @FXML
    private void exportToPDF() {
        String[] headers = {"ID", "First Name", "Last Name", "Email", "Specialty", "Governorate", "Verified", "Phone", "CIN"};
        ExportUtils.exportToPDF(doctorsListView.getScene().getWindow(), "Doctors Directory",
                filteredList, headers, d -> new String[]{
                        String.valueOf(d.getId()), d.getFirstName(), d.getLastName(), d.getEmail(),
                        d.getSpecialty() != null ? d.getSpecialty().getDisplayName() : "",
                        d.getGovernorate() != null ? d.getGovernorate().getDisplayName() : "",
                        d.isVerified() ? "Yes" : "No",
                        d.getPhoneNumber() != null ? d.getPhoneNumber() : "",
                        d.getCin() != null ? d.getCin() : ""
                });
        showAlert(Alert.AlertType.INFORMATION, "Export Complete", "Doctors exported to PDF successfully.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}