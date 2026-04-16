package com.pidev.controllers;

import com.pidev.models.BaseUser;
import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import com.pidev.utils.BCrypt;
import com.pidev.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Profile Controller — displays and edits the currently logged-in user's profile.
 * Supports both Patient and Medecin roles with role-specific fields.
 */
public class ProfileController {

    // ── Left panel ──────────────────────────────────────────────
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblFullName;
    @FXML private Label lblRoleBadge;

    // ── Nav buttons ──────────────────────────────────────────────
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavSecurity;
    @FXML private Button btnNavActivity;

    // ── Profile section ──────────────────────────────────────────
    @FXML private VBox sectionProfile;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfAge;
    @FXML private TextField tfGender;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private Button btnEditProfile;
    @FXML private Button btnSave;
    @FXML private Label lblStatus;

    // ── Patient extra ────────────────────────────────────────────
    @FXML private VBox sectionPatientExtra;
    @FXML private TextField tfAddress;
    @FXML private Label lblInsurance;

    // ── Medecin extra ────────────────────────────────────────────
    @FXML private VBox sectionMedecinExtra;
    @FXML private TextField tfSpecialty;
    @FXML private TextField tfGovernorate;
    @FXML private TextField tfExperience;

    // ── Security section ─────────────────────────────────────────
    @FXML private VBox sectionSecurity;
    @FXML private PasswordField tfCurrentPassword;
    @FXML private PasswordField tfNewPassword;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private Label lblSecurityStatus;

    // ── Activity section ─────────────────────────────────────────
    @FXML private VBox sectionActivity;
    @FXML private Label lblAccountStatus;
    @FXML private Label lblAccountRole;
    @FXML private Label lblAccountId;

    private boolean editMode = false;
    private BaseUser currentUser;

    private static final String STYLE_NAV_ACTIVE =
            "-fx-background-color: rgba(91,110,245,0.15);" +
            "-fx-text-fill: #818cf8; -fx-font-weight: 600;" +
            "-fx-font-size: 13px; -fx-padding: 11 16;" +
            "-fx-alignment: CENTER_LEFT; -fx-cursor: hand;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: -pd-primary transparent transparent transparent;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-radius: 0 8 8 0;";

    private static final String STYLE_NAV_INACTIVE =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #94a3b8; -fx-font-size: 13px;" +
            "-fx-padding: 11 16; -fx-alignment: CENTER_LEFT;" +
            "-fx-cursor: hand; -fx-background-radius: 8;" +
            "-fx-border-width: 0;";

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getUser();
        if (currentUser == null) return;

        populateUserData();
        setFieldsEditable(false);
    }

    // ── Data population ──────────────────────────────────────────

    private void populateUserData() {
        // Avatar initials
        String initials = "";
        if (currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty())
            initials += currentUser.getFirstName().charAt(0);
        if (currentUser.getLastName() != null && !currentUser.getLastName().isEmpty())
            initials += currentUser.getLastName().charAt(0);
        lblAvatarInitials.setText(initials.toUpperCase());

        // Name & role
        lblFullName.setText(currentUser.getFullName());

        // Common fields
        tfFirstName.setText(nvl(currentUser.getFirstName()));
        tfLastName.setText(nvl(currentUser.getLastName()));
        tfAge.setText(currentUser.getAge() > 0 ? String.valueOf(currentUser.getAge()) : "");
        tfGender.setText(nvl(currentUser.getGender()));
        tfEmail.setText(nvl(currentUser.getEmail()));

        // Role-specific
        if (currentUser instanceof Patient p) {
            lblRoleBadge.setText("Patient");
            tfPhone.setText(nvl(p.getPhoneNumber()));
            tfAddress.setText(nvl(p.getAddress()));
            lblInsurance.setText(p.isHasInsurance()
                    ? "Oui — " + nvl(p.getInsuranceNumber())
                    : "Non");
            sectionPatientExtra.setVisible(true);
            sectionPatientExtra.setManaged(true);
            sectionMedecinExtra.setVisible(false);
            sectionMedecinExtra.setManaged(false);

            // Activity
            lblAccountRole.setText("Patient");

        } else if (currentUser instanceof Medecin m) {
            lblRoleBadge.setText("Médecin");
            lblRoleBadge.setStyle(
                    "-fx-background-color: rgba(34,197,94,0.12);" +
                    "-fx-text-fill: #22c55e;" +
                    "-fx-font-size: 11px; -fx-font-weight: 600;" +
                    "-fx-padding: 4 14; -fx-background-radius: 20;" +
                    "-fx-border-color: rgba(34,197,94,0.3);" +
                    "-fx-border-width: 1; -fx-border-radius: 20;");
            tfPhone.setText(nvl(m.getPhoneNumber()));
            tfSpecialty.setText(nvl(m.getSpecialty()));
            tfGovernorate.setText(nvl(m.getGovernorate()));
            tfExperience.setText(nvl(m.getExperience()));
            sectionMedecinExtra.setVisible(true);
            sectionMedecinExtra.setManaged(true);
            sectionPatientExtra.setVisible(false);
            sectionPatientExtra.setManaged(false);

            // Activity
            lblAccountRole.setText("Médecin" + (m.isVerified() ? " ✓ Vérifié" : ""));

        } else {
            lblRoleBadge.setText("Admin");
            sectionPatientExtra.setVisible(false);
            sectionPatientExtra.setManaged(false);
            sectionMedecinExtra.setVisible(false);
            sectionMedecinExtra.setManaged(false);
            lblAccountRole.setText("Administrateur");
        }

        lblAccountStatus.setText(currentUser.isActive() ? "Actif" : "Inactif");
        lblAccountId.setText("#" + currentUser.getId());
    }

    // ── Edit toggle ──────────────────────────────────────────────

    @FXML
    public void toggleEdit() {
        editMode = !editMode;
        setFieldsEditable(editMode);

        if (editMode) {
            btnEditProfile.setText("Annuler");
            btnEditProfile.setStyle(
                    "-fx-background-color: rgba(244,63,94,0.1);" +
                    "-fx-text-fill: #f43f5e; -fx-font-size: 12px;" +
                    "-fx-font-weight: 600; -fx-padding: 6 16;" +
                    "-fx-background-radius: 6; -fx-cursor: hand;" +
                    "-fx-border-color: rgba(244,63,94,0.25);" +
                    "-fx-border-width: 1; -fx-border-radius: 6;");
            btnSave.setVisible(true);
            btnSave.setManaged(true);
        } else {
            btnEditProfile.setText("Modifier");
            btnEditProfile.setStyle(
                    "-fx-background-color: rgba(91,110,245,0.15);" +
                    "-fx-text-fill: #818cf8; -fx-font-size: 12px;" +
                    "-fx-font-weight: 600; -fx-padding: 6 16;" +
                    "-fx-background-radius: 6; -fx-cursor: hand;" +
                    "-fx-border-color: rgba(91,110,245,0.3);" +
                    "-fx-border-width: 1; -fx-border-radius: 6;");
            btnSave.setVisible(false);
            btnSave.setManaged(false);
            // Restore original data on cancel
            populateUserData();
            hideStatus();
        }
    }

    private void setFieldsEditable(boolean editable) {
        String readStyle =
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #f1f5f9; -fx-font-size: 13px;" +
                "-fx-border-width: 0; -fx-padding: 0;" +
                "-fx-background-radius: 0;";

        String editStyle =
                "-fx-background-color: #1c2133;" +
                "-fx-text-fill: #f1f5f9; -fx-font-size: 13px;" +
                "-fx-border-color: #2e3a52; -fx-border-width: 1;" +
                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                "-fx-padding: 6 10;";

        String style = editable ? editStyle : readStyle;

        for (TextField tf : new TextField[]{
                tfFirstName, tfLastName, tfAge, tfGender,
                tfEmail, tfPhone, tfAddress,
                tfSpecialty, tfGovernorate, tfExperience}) {
            if (tf != null) {
                tf.setEditable(editable);
                tf.setStyle(style);
            }
        }
    }

    // ── Save profile ─────────────────────────────────────────────

    @FXML
    public void saveProfile() {
        try {
            currentUser.setFirstName(tfFirstName.getText().trim());
            currentUser.setLastName(tfLastName.getText().trim());
            currentUser.setEmail(tfEmail.getText().trim());
            if (!tfAge.getText().trim().isEmpty()) {
                currentUser.setAge(Integer.parseInt(tfAge.getText().trim()));
            }
            currentUser.setGender(tfGender.getText().trim());

            if (currentUser instanceof Patient p) {
                p.setPhoneNumber(tfPhone.getText().trim());
                p.setAddress(tfAddress.getText().trim());
                new PatientService().update(p);

            } else if (currentUser instanceof Medecin m) {
                m.setPhoneNumber(tfPhone.getText().trim());
                m.setSpecialty(tfSpecialty.getText().trim());
                m.setGovernorate(tfGovernorate.getText().trim());
                m.setExperience(tfExperience.getText().trim());
                new MedecinService().update(m);
            }

            UserSession.getInstance().setUser(currentUser);
            populateUserData();
            toggleEdit(); // exit edit mode
            showStatus("✓ Profil mis à jour avec succès", "#22c55e");

        } catch (NumberFormatException e) {
            showStatus("✗ Âge invalide — entrez un nombre entier", "#f43f5e");
        } catch (Exception e) {
            showStatus("✗ Erreur : " + e.getMessage(), "#f43f5e");
        }
    }

    // ── Change password ──────────────────────────────────────────

    @FXML
    public void changePassword() {
        String current = tfCurrentPassword.getText();
        String newPwd  = tfNewPassword.getText();
        String confirm = tfConfirmPassword.getText();

        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            showSecurityStatus("✗ Veuillez remplir tous les champs", "#f43f5e");
            return;
        }

        if (!BCrypt.checkpw(current, currentUser.getPassword())) {
            showSecurityStatus("✗ Mot de passe actuel incorrect", "#f43f5e");
            return;
        }

        if (!newPwd.equals(confirm)) {
            showSecurityStatus("✗ Les mots de passe ne correspondent pas", "#f43f5e");
            return;
        }

        if (newPwd.length() < 6) {
            showSecurityStatus("✗ Le mot de passe doit contenir au moins 6 caractères", "#f43f5e");
            return;
        }

        try {
            String hashed = BCrypt.hashpw(newPwd, BCrypt.gensalt());
            currentUser.setPassword(hashed);

            if (currentUser instanceof Patient p) {
                new PatientService().update(p);
            } else if (currentUser instanceof Medecin m) {
                new MedecinService().update(m);
            }

            UserSession.getInstance().setUser(currentUser);
            tfCurrentPassword.clear();
            tfNewPassword.clear();
            tfConfirmPassword.clear();
            showSecurityStatus("✓ Mot de passe mis à jour avec succès", "#22c55e");

        } catch (Exception e) {
            showSecurityStatus("✗ Erreur : " + e.getMessage(), "#f43f5e");
        }
    }

    // ── Section navigation ───────────────────────────────────────

    @FXML
    public void showProfileSection() {
        switchSection(sectionProfile, btnNavProfile);
    }

    @FXML
    public void showSecuritySection() {
        switchSection(sectionSecurity, btnNavSecurity);
    }

    @FXML
    public void showActivitySection() {
        switchSection(sectionActivity, btnNavActivity);
    }

    private void switchSection(VBox section, Button activeBtn) {
        // Hide all
        sectionProfile.setVisible(false);  sectionProfile.setManaged(false);
        sectionSecurity.setVisible(false); sectionSecurity.setManaged(false);
        sectionActivity.setVisible(false); sectionActivity.setManaged(false);

        // Show target
        section.setVisible(true);
        section.setManaged(true);

        // Update nav styles
        btnNavProfile.setStyle(STYLE_NAV_INACTIVE);
        btnNavSecurity.setStyle(STYLE_NAV_INACTIVE);
        btnNavActivity.setStyle(STYLE_NAV_INACTIVE);
        activeBtn.setStyle(STYLE_NAV_ACTIVE);
    }

    // ── Logout ───────────────────────────────────────────────────

    @FXML
    public void handleLogout() {
        UserSession.getInstance().cleanUserSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void showStatus(String msg, String color) {
        lblStatus.setText(msg);
        lblStatus.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: " + color + ";");
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

    private void hideStatus() {
        lblStatus.setVisible(false);
        lblStatus.setManaged(false);
    }

    private void showSecurityStatus(String msg, String color) {
        lblSecurityStatus.setText(msg);
        lblSecurityStatus.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: " + color + ";");
        lblSecurityStatus.setVisible(true);
        lblSecurityStatus.setManaged(true);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
