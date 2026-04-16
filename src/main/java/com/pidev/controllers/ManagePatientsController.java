package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.services.PatientService;
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

public class ManagePatientsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> insuranceFilterCombo;
    @FXML private ComboBox<String> activeFilterCombo;
    @FXML private ListView<Patient> patientsListView;

    private final PatientService patientService = new PatientService();
    private ObservableList<Patient> masterList = FXCollections.observableArrayList();
    private FilteredList<Patient> filteredList;

    @FXML
    private void initialize() {
        insuranceFilterCombo.setItems(FXCollections.observableArrayList("All", "Has Insurance", "No Insurance"));
        activeFilterCombo.setItems(FXCollections.observableArrayList("All Status", "Active", "Inactive"));
        insuranceFilterCombo.getSelectionModel().selectFirst();
        activeFilterCombo.getSelectionModel().selectFirst();

        filteredList = new FilteredList<>(masterList, p -> true);
        patientsListView.setItems(filteredList);
        patientsListView.setCellFactory(lv -> new PatientCardCell());

        searchField.textProperty().addListener((obs, old, val) -> filterPatients());
        insuranceFilterCombo.valueProperty().addListener((obs, old, val) -> filterPatients());
        activeFilterCombo.valueProperty().addListener((obs, old, val) -> filterPatients());

        loadPatients();
    }

    private void loadPatients() {
        try {
            masterList.setAll(patientService.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshList() {
        loadPatients();
        filterPatients();
    }

    private void filterPatients() {
        String search = searchField.getText().trim().toLowerCase();
        String insurance = insuranceFilterCombo.getValue();
        String active = activeFilterCombo.getValue();

        Predicate<Patient> predicate = patient -> {
            boolean nameMatch = search.isEmpty() ||
                    patient.getFullName().toLowerCase().contains(search) ||
                    patient.getEmail().toLowerCase().contains(search);
            boolean insuranceMatch = insurance == null || insurance.equals("All") ||
                    (insurance.equals("Has Insurance") && patient.isHasInsurance()) ||
                    (insurance.equals("No Insurance") && !patient.isHasInsurance());
            boolean activeMatch = active == null || active.equals("All Status") ||
                    (active.equals("Active") && patient.isActive()) ||
                    (active.equals("Inactive") && !patient.isActive());
            return nameMatch && insuranceMatch && activeMatch;
        };
        filteredList.setPredicate(predicate);
    }

    private class PatientCardCell extends ListCell<Patient> {
        private final VBox card;
        private final Label nameLabel;
        private final Label detailsLabel;
        private final Label emailLabel;
        private final Label activeBadge;
        private final Button editBtn;
        private final Button toggleActiveBtn;
        private final Button deleteBtn;
        private final HBox actionsBox;

        public PatientCardCell() {
            card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 2, 0);");

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

            detailsLabel = new Label();
            detailsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

            emailLabel = new Label();
            emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            activeBadge = new Label();
            activeBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; " +
                    "-fx-padding: 3 8; -fx-background-radius: 20; -fx-font-weight: bold;");

            editBtn = new Button("✏️ Edit");
            editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");

            toggleActiveBtn = new Button();
            toggleActiveBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");

            deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");

            actionsBox = new HBox(8, editBtn, toggleActiveBtn, deleteBtn);
            actionsBox.setAlignment(Pos.CENTER_RIGHT);

            HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            topRow.getChildren().addAll(nameLabel, activeBadge);

            card.getChildren().addAll(topRow, detailsLabel, emailLabel, actionsBox);
        }

        @Override
        protected void updateItem(Patient patient, boolean empty) {
            super.updateItem(patient, empty);
            if (empty || patient == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(patient.getFullName());
                detailsLabel.setText("🕒 " + patient.getAge() + " years  •  " +
                        (patient.getGender() != null ? patient.getGender() : "N/A") + "  •  " +
                        (patient.isHasInsurance() ? "🛡️ Insured" : "⚠️ No Insurance"));
                emailLabel.setText("📧 " + patient.getEmail());

                activeBadge.setText(patient.isActive() ? "Active" : "Inactive");
                activeBadge.setStyle(patient.isActive() ?
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 20;" :
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 20;");

                editBtn.setOnAction(e -> openEditDialog(patient));

                toggleActiveBtn.setText(patient.isActive() ? "Deactivate" : "Activate");
                toggleActiveBtn.setStyle(patient.isActive() ?
                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;" :
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6;");
                toggleActiveBtn.setOnAction(e -> {
                    patient.setActive(!patient.isActive());
                    try {
                        patientService.update(patient);
                        refreshList();
                        showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                                patient.getFullName() + " is now " + (patient.isActive() ? "active" : "inactive"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Patient");
                    confirm.setHeaderText("Delete " + patient.getFullName() + "?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                patientService.delete(patient.getId());
                                refreshList();
                                showAlert(Alert.AlertType.INFORMATION, "Patient Deleted",
                                        patient.getFullName() + " has been removed.");
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

    private void openEditDialog(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditPatientDialog.fxml"));
            Parent root = loader.load();
            EditPatientDialogController controller = loader.getController();
            controller.setPatient(patient);
            controller.setOnSaved(() -> {
                refreshList();
                showAlert(Alert.AlertType.INFORMATION, "Patient Updated", patient.getFullName() + " has been updated.");
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Patient");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToCSV() {
        String[] headers = {"ID", "First Name", "Last Name", "Email", "Age", "Gender", "Insurance", "Active", "Phone", "Address"};
        ExportUtils.exportToCSV(patientsListView.getScene().getWindow(), "patients_export",
                filteredList, headers, p -> new String[]{
                        String.valueOf(p.getId()), p.getFirstName(), p.getLastName(), p.getEmail(),
                        String.valueOf(p.getAge()), p.getGender() != null ? p.getGender() : "",
                        p.isHasInsurance() ? "Yes" : "No", p.isActive() ? "Yes" : "No",
                        p.getPhoneNumber() != null ? p.getPhoneNumber() : "",
                        p.getAddress() != null ? p.getAddress() : ""
                });
        showAlert(Alert.AlertType.INFORMATION, "Export Complete", "Patients exported to CSV successfully.");
    }

    @FXML
    private void exportToPDF() {
        String[] headers = {"ID", "First Name", "Last Name", "Email", "Age", "Gender", "Insurance", "Active", "Phone", "Address"};
        ExportUtils.exportToPDF(patientsListView.getScene().getWindow(), "Patients Directory",
                filteredList, headers, p -> new String[]{
                        String.valueOf(p.getId()), p.getFirstName(), p.getLastName(), p.getEmail(),
                        String.valueOf(p.getAge()), p.getGender() != null ? p.getGender() : "",
                        p.isHasInsurance() ? "Yes" : "No", p.isActive() ? "Yes" : "No",
                        p.getPhoneNumber() != null ? p.getPhoneNumber() : "",
                        p.getAddress() != null ? p.getAddress() : ""
                });
        showAlert(Alert.AlertType.INFORMATION, "Export Complete", "Patients exported to PDF successfully.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}