package com.pidev.controllers;

import com.pidev.models.Patient;
import com.pidev.services.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class PatientController {

    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colFirstName;
    @FXML private TableColumn<Patient, String> colLastName;
    @FXML private TableColumn<Patient, String> colEmail;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, Boolean> colInsurance;

    @FXML private TextField tfFirstName, tfLastName, tfEmail, tfPhone, tfAddress, tfAge, tfInsuranceNumber, searchField;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbGender;
    @FXML private CheckBox cbInsurance;

    private final PatientService patientService = new PatientService();
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Patient selectedPatient = null;

    @FXML
    public void initialize() {
        setupTableColumns();

        // UI Fix: Stretches columns to fill the card width (no empty space on right)
        tablePatients.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadPatients();

        tablePatients.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                fillForm(newVal);
            }
        });
    }

    private void setupTableColumns() {
        // Properties match the getters (e.g., "firstName" calls "getFirstName()")
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colInsurance.setCellValueFactory(new PropertyValueFactory<>("hasInsurance"));

        // Add ✅ or ❌ icons
        colInsurance.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✅" : "❌");
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientService.getAll();
            patientList.setAll(patients);
            tablePatients.setItems(patientList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillForm(Patient p) {
        tfFirstName.setText(p.getFirstName());
        tfLastName.setText(p.getLastName());
        tfEmail.setText(p.getEmail());
        tfPhone.setText(p.getPhoneNumber());
        tfAddress.setText(p.getAddress());
        tfAge.setText(String.valueOf(p.getAge()));
        cbGender.setValue(p.getGender());
        cbInsurance.setSelected(p.isHasInsurance());
        tfInsuranceNumber.setText(p.getInsuranceNumber());
        tfPassword.clear();
    }

    @FXML
    public void addPatient() {
        if (!validateForm()) return;
        try {
            Patient p = buildPatientFromForm();
            patientService.add(p);
            loadPatients();
            clearForm();
            showAlert("Succès", "Patient ajouté !");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    public void updatePatient() {
        if (selectedPatient == null || !validateForm()) return;
        try {
            Patient p = buildPatientFromForm();
            p.setId(selectedPatient.getId());
            patientService.update(p);
            loadPatients();
            clearForm();
            showAlert("Succès", "Patient modifié !");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    public void deletePatient() {
        if (selectedPatient == null) return;
        try {
            patientService.delete(selectedPatient.getId());
            loadPatients();
            clearForm();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    public void onSearch() {
        String q = searchField.getText().toLowerCase().trim();
        if (q.isEmpty()) {
            tablePatients.setItems(patientList);
        } else {
            tablePatients.setItems(patientList.filtered(p ->
                    p.getFirstName().toLowerCase().contains(q) || p.getLastName().toLowerCase().contains(q)));
        }
    }

    @FXML
    public void clearForm() {
        tfFirstName.clear(); tfLastName.clear(); tfEmail.clear(); tfPassword.clear();
        tfPhone.clear(); tfAddress.clear(); tfAge.clear();
        cbGender.setValue(null); cbInsurance.setSelected(false); tfInsuranceNumber.clear();
        selectedPatient = null;
        tablePatients.getSelectionModel().clearSelection();
    }

    private Patient buildPatientFromForm() {
        Patient p = new Patient();
        p.setFirstName(tfFirstName.getText().trim());
        p.setLastName(tfLastName.getText().trim());
        p.setEmail(tfEmail.getText().trim());
        p.setPassword(tfPassword.getText());
        p.setPhoneNumber(tfPhone.getText().trim());
        p.setAddress(tfAddress.getText().trim());
        p.setAge(tfAge.getText().isEmpty() ? 0 : Integer.parseInt(tfAge.getText().trim()));
        p.setGender(cbGender.getValue());
        p.setHasInsurance(cbInsurance.isSelected());
        p.setInsuranceNumber(tfInsuranceNumber.getText().trim());
        p.setActive(true);
        p.setRoles("[\"ROLE_PATIENT\"]");
        return p;
    }

    private boolean validateForm() {
        return !tfFirstName.getText().isEmpty() && !tfLastName.getText().isEmpty() && tfEmail.getText().contains("@");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}