package com.pidev.controllers;

import com.pidev.models.Medecin;
import com.pidev.services.MedecinService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for Medecin CRUD operations.
 */
public class MedecinController {

    @FXML private TableView<Medecin> tableMedecins;
    @FXML private TextField tfFirstName, tfLastName, tfEmail, tfPhone, tfSpecialty, tfCin,
            tfAddress, tfGovernorate, tfAge, tfEducation, tfExperience, searchField;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbGender;
    @FXML private CheckBox cbVerified;

    private final MedecinService medecinService = new MedecinService();
    private ObservableList<Medecin> medecinList = FXCollections.observableArrayList();
    private Medecin selectedMedecin = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMedecins();

        tableMedecins.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMedecin = newVal;
                fillForm(newVal);
            }
        });
    }

    private void setupTableColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, Integer> idCol = (TableColumn<Medecin, Integer>) tableMedecins.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, String> fnCol = (TableColumn<Medecin, String>) tableMedecins.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, String> lnCol = (TableColumn<Medecin, String>) tableMedecins.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, String> emailCol = (TableColumn<Medecin, String>) tableMedecins.getColumns().get(3);
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, String> phoneCol = (TableColumn<Medecin, String>) tableMedecins.getColumns().get(4);
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, String> specCol = (TableColumn<Medecin, String>) tableMedecins.getColumns().get(5);
        @SuppressWarnings("unchecked")
        TableColumn<Medecin, Integer> ageCol = (TableColumn<Medecin, Integer>) tableMedecins.getColumns().get(6);

        idCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getId()));
        fnCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getFirstName()));
        lnCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getLastName()));
        emailCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getEmail()));
        phoneCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getPhoneNumber()));
        specCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleStringProperty(cdf.getValue().getSpecialty()));
        ageCol.setCellValueFactory(cdf -> new javafx.beans.property.SimpleObjectProperty<>(cdf.getValue().getAge()));
    }

    private void loadMedecins() {
        try {
            medecinList.setAll(medecinService.getAll());
            tableMedecins.setItems(medecinList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les médecins: " + e.getMessage());
        }
    }

    private void fillForm(Medecin m) {
        tfFirstName.setText(m.getFirstName());
        tfLastName.setText(m.getLastName());
        tfEmail.setText(m.getEmail());
        tfPhone.setText(m.getPhoneNumber());
        tfSpecialty.setText(m.getSpecialty());
        tfCin.setText(m.getCin());
        tfAddress.setText(m.getAddress());
        tfGovernorate.setText(m.getGovernorate());
        tfAge.setText(String.valueOf(m.getAge()));
        cbGender.setValue(m.getGender());
        tfEducation.setText(m.getEducation());
        tfExperience.setText(m.getExperience());
        cbVerified.setSelected(m.isVerified());
        tfPassword.clear();
    }

    @FXML
    public void addMedecin() {
        if (!validateForm()) return;

        try {
            Medecin m = buildMedecinFromForm();
            if (m.getPassword() == null || m.getPassword().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Le mot de passe est obligatoire.");
                return;
            }
            medecinService.add(m);
            loadMedecins();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin ajouté avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void updateMedecin() {
        if (selectedMedecin == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un médecin.");
            return;
        }
        if (!validateForm()) return;

        try {
            Medecin m = buildMedecinFromForm();
            m.setId(selectedMedecin.getId());
            medecinService.update(m);
            loadMedecins();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin modifié avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void deleteMedecin() {
        if (selectedMedecin == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un médecin.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer Dr. " + selectedMedecin.getFullName() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    medecinService.delete(selectedMedecin.getId());
                    loadMedecins();
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin supprimé !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void clearForm() {
        tfFirstName.clear(); tfLastName.clear(); tfEmail.clear(); tfPassword.clear();
        tfPhone.clear(); tfSpecialty.clear(); tfCin.clear(); tfAddress.clear();
        tfGovernorate.clear(); tfAge.clear(); tfEducation.clear(); tfExperience.clear();
        cbGender.setValue(null); cbVerified.setSelected(false);
        selectedMedecin = null;
        tableMedecins.getSelectionModel().clearSelection();
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            tableMedecins.setItems(medecinList);
        } else {
            FilteredList<Medecin> filtered = medecinList.filtered(m ->
                    (m.getFirstName() != null && m.getFirstName().toLowerCase().contains(query)) ||
                    (m.getLastName() != null && m.getLastName().toLowerCase().contains(query)) ||
                    (m.getSpecialty() != null && m.getSpecialty().toLowerCase().contains(query)) ||
                    (m.getEmail() != null && m.getEmail().toLowerCase().contains(query))
            );
            tableMedecins.setItems(filtered);
        }
    }

    private Medecin buildMedecinFromForm() {
        Medecin m = new Medecin();
        m.setFirstName(tfFirstName.getText().trim());
        m.setLastName(tfLastName.getText().trim());
        m.setEmail(tfEmail.getText().trim());
        m.setPassword(tfPassword.getText());
        m.setPhoneNumber(tfPhone.getText().trim());
        m.setSpecialty(tfSpecialty.getText().trim());
        m.setCin(tfCin.getText().trim());
        m.setAddress(tfAddress.getText().trim());
        m.setGovernorate(tfGovernorate.getText().trim());
        m.setAge(tfAge.getText().isEmpty() ? 0 : Integer.parseInt(tfAge.getText().trim()));
        m.setGender(cbGender.getValue());
        m.setEducation(tfEducation.getText().trim());
        m.setExperience(tfExperience.getText().trim());
        m.setActive(true);
        m.setVerified(cbVerified.isSelected());
        return m;
    }

    private boolean validateForm() {
        if (tfFirstName.getText().trim().isEmpty() || tfLastName.getText().trim().isEmpty()
                || tfEmail.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Prénom, Nom et Email sont obligatoires.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
