package com.pidev.controllers;

import com.pidev.models.Admin;
import com.pidev.services.AdminService;
import com.pidev.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AdminProfilePageController {

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label genderLabel;
    @FXML private Label ageLabel;
    @FXML private Label displayNameLabel;

    @FXML private VBox viewModeContainer;
    @FXML private VBox editModeContainer;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField displayNameField;
    @FXML private PasswordField passwordField;

    @FXML private Label editErrorLabel;

    private Admin admin;
    private final AdminService adminService = new AdminService();

    @FXML
    private void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
        refreshView();
    }

    private void refreshView() {
        fullNameLabel.setText(admin.getFullName());
        emailLabel.setText(admin.getEmail());
        genderLabel.setText(admin.getGender() != null ? admin.getGender() : "Not set");
        ageLabel.setText(String.valueOf(admin.getAge()));
        displayNameLabel.setText(admin.getName() != null ? admin.getName() : admin.getFullName());

        firstNameField.setText(admin.getFirstName());
        lastNameField.setText(admin.getLastName());
        ageField.setText(String.valueOf(admin.getAge()));
        genderCombo.setValue(admin.getGender());
        displayNameField.setText(admin.getName() != null ? admin.getName() : "");
        passwordField.clear();
        editErrorLabel.setVisible(false);
    }

    @FXML
    private void toggleEditMode() {
        boolean isEditMode = editModeContainer.isVisible();
        if (!isEditMode) {
            refreshView();
        }
        viewModeContainer.setVisible(isEditMode);
        viewModeContainer.setManaged(isEditMode);
        editModeContainer.setVisible(!isEditMode);
        editModeContainer.setManaged(!isEditMode);
    }

    @FXML
    private void saveProfile() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = genderCombo.getValue();
        String displayName = displayNameField.getText().trim();
        String newPassword = passwordField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty() || gender == null || displayName.isEmpty()) {
            editErrorLabel.setText("All fields except password are required.");
            editErrorLabel.setVisible(true);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 1 || age > 120) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            editErrorLabel.setText("Invalid age.");
            editErrorLabel.setVisible(true);
            return;
        }

        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setAge(age);
        admin.setGender(gender);
        admin.setName(displayName);

        if (!newPassword.isEmpty()) {
            admin.setPassword(PasswordUtils.hashPassword(newPassword));
        }

        try {
            adminService.update(admin);
            refreshView();
            toggleEditMode();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile updated successfully!");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            editErrorLabel.setText("Update failed: " + e.getMessage());
            editErrorLabel.setVisible(true);
        }
    }
}