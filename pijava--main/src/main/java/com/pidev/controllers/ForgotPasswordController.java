package com.pidev.controllers;

import com.pidev.utils.PasswordResetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * 4-step forgot-password flow:
 *  Step 1 — Enter email → send OTP
 *  Step 2 — Enter OTP code
 *  Step 3 — Enter new password
 *  Step 4 — Success screen
 */
public class ForgotPasswordController {

    // ── Step panels ──────────────────────────────────────────────
    @FXML private VBox stepEmail;
    @FXML private VBox stepOTP;
    @FXML private VBox stepNewPassword;
    @FXML private VBox stepSuccess;

    // ── Step indicator dots ──────────────────────────────────────
    @FXML private Circle dot1;
    @FXML private Circle dot2;
    @FXML private Circle dot3;

    // ── Header subtitle ──────────────────────────────────────────
    @FXML private Label lblStepTitle;

    // ── Step 1 ───────────────────────────────────────────────────
    @FXML private TextField tfEmail;
    @FXML private Label     lblEmailError;
    @FXML private Button    btnSendCode;

    // ── Step 2 ───────────────────────────────────────────────────
    @FXML private TextField tfOTP;
    @FXML private Label     lblOTPInfo;
    @FXML private Label     lblOTPError;

    // ── Step 3 ───────────────────────────────────────────────────
    @FXML private PasswordField tfNewPassword;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private Label         lblPasswordError;

    private String currentEmail = "";

    private static final String DOT_ACTIVE   = "-fx-fill: #5b6ef5;";
    private static final String DOT_DONE     = "-fx-fill: #22c55e;";
    private static final String DOT_INACTIVE = "-fx-fill: #252d42;";

    // ── Step 1: Send OTP ─────────────────────────────────────────

    @FXML
    public void handleSendCode() {
        String email = tfEmail.getText().trim();

        if (email.isEmpty() || !email.contains("@")) {
            showError(lblEmailError, "Veuillez entrer une adresse email valide.");
            return;
        }

        currentEmail = email;
        hideError(lblEmailError);

        // Disable button and show loading state
        btnSendCode.setText("Envoi en cours…");
        btnSendCode.setDisable(true);

        PasswordResetService.getInstance().generateAndSendOTP(
                email,
                // onSuccess
                () -> {
                    btnSendCode.setText("Envoyer le code");
                    btnSendCode.setDisable(false);
                    lblOTPInfo.setText("Un code a été envoyé à " + email + ". Vérifiez votre boîte mail.");
                    goToStep(2);
                },
                // onError
                err -> {
                    btnSendCode.setText("Envoyer le code");
                    btnSendCode.setDisable(false);
                    showError(lblEmailError, err != null ? err : "Erreur lors de l'envoi. Réessayez.");
                }
        );
    }

    // ── Step 2: Verify OTP ───────────────────────────────────────

    @FXML
    public void handleVerifyOTP() {
        String code = tfOTP.getText().trim();

        if (code.length() != 6) {
            showError(lblOTPError, "Le code doit contenir exactement 6 chiffres.");
            return;
        }

        if (PasswordResetService.getInstance().verifyOTP(currentEmail, code)) {
            hideError(lblOTPError);
            goToStep(3);
        } else {
            showError(lblOTPError, "Code incorrect ou expiré. Demandez un nouveau code.");
        }
    }

    // ── Step 3: Reset password ───────────────────────────────────

    @FXML
    public void handleResetPassword() {
        String pwd     = tfNewPassword.getText();
        String confirm = tfConfirmPassword.getText();

        if (pwd.length() < 6) {
            showError(lblPasswordError, "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!pwd.equals(confirm)) {
            showError(lblPasswordError, "Les mots de passe ne correspondent pas.");
            return;
        }

        boolean ok = PasswordResetService.getInstance().resetPassword(currentEmail, pwd);
        if (ok) {
            goToStep(4);
        } else {
            showError(lblPasswordError, "Erreur lors de la réinitialisation. Réessayez.");
        }
    }

    // ── Navigation ───────────────────────────────────────────────

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) stepEmail.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToStep(int step) {
        // Hide all panels
        setStep(stepEmail,       false);
        setStep(stepOTP,         false);
        setStep(stepNewPassword, false);
        setStep(stepSuccess,     false);

        // Reset dots
        dot1.setStyle(DOT_INACTIVE);
        dot2.setStyle(DOT_INACTIVE);
        dot3.setStyle(DOT_INACTIVE);

        switch (step) {
            case 1 -> {
                setStep(stepEmail, true);
                dot1.setStyle(DOT_ACTIVE);
                lblStepTitle.setText("Mot de passe oublié");
            }
            case 2 -> {
                setStep(stepOTP, true);
                dot1.setStyle(DOT_DONE);
                dot2.setStyle(DOT_ACTIVE);
                lblStepTitle.setText("Vérification du code");
            }
            case 3 -> {
                setStep(stepNewPassword, true);
                dot1.setStyle(DOT_DONE);
                dot2.setStyle(DOT_DONE);
                dot3.setStyle(DOT_ACTIVE);
                lblStepTitle.setText("Nouveau mot de passe");
            }
            case 4 -> {
                setStep(stepSuccess, true);
                dot1.setStyle(DOT_DONE);
                dot2.setStyle(DOT_DONE);
                dot3.setStyle(DOT_DONE);
                lblStepTitle.setText("Terminé");
            }
        }
    }

    private void setStep(VBox panel, boolean visible) {
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    // ── Error helpers ────────────────────────────────────────────

    private void showError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void hideError(Label lbl) {
        lbl.setVisible(false);
        lbl.setManaged(false);
    }
}
