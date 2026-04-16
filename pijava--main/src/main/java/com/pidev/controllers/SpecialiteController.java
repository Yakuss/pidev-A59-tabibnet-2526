package com.pidev.controllers;

import com.pidev.models.Specialite;
import com.pidev.services.SpecialiteService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

/**
 * Controller for Specialite CRUD operations with professional UX.
 */
public class SpecialiteController {

    @FXML private TableView<Specialite> tableSpecialites;
    @FXML private TextField tfNom, tfDescription, searchField;
    @FXML private Label lblStatus, lblCount;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnClear;

    private final SpecialiteService specialiteService = new SpecialiteService();
    private ObservableList<Specialite> specialiteList = FXCollections.observableArrayList();
    private Specialite selectedSpecialite = null;

    @FXML
    public void initialize() {
        loadSpecialites();

        // Listen for table row selection
        tableSpecialites.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSpecialite = newVal;
                fillForm(newVal);
                showStatus("📝 Spécialité sélectionnée : " + newVal.getNom(), "#a78bfa");
            }
        });

        // Setup search live filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> onSearch());

        // Initial status
        showStatus("✅ Module Spécialités chargé", "#10b981");
    }

    private void loadSpecialites() {
        try {
            specialiteList.setAll(specialiteService.getAll());
            tableSpecialites.setItems(specialiteList);
            updateCount();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les spécialités: " + e.getMessage());
        }
    }

    private void updateCount() {
        lblCount.setText(specialiteList.size() + " spécialité(s)");
    }

    private void fillForm(Specialite s) {
        tfNom.setText(s.getNom());
        tfDescription.setText(s.getDescription() != null ? s.getDescription() : "");

        // Subtle animation on form fill
        FadeTransition ft = new FadeTransition(Duration.millis(200), tfNom.getParent());
        ft.setFromValue(0.7);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    public void addSpecialite() {
        if (!validateForm()) return;

        try {
            Specialite s = buildFromForm();
            specialiteService.add(s);
            loadSpecialites();
            clearForm();
            showStatus("✅ Spécialité ajoutée avec succès !", "#10b981");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Spécialité ajoutée avec succès !");
            animateButton(btnAdd);
        } catch (Exception e) {
            showStatus("❌ Erreur lors de l'ajout", "#ef4444");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void updateSpecialite() {
        if (selectedSpecialite == null) {
            showStatus("⚠️ Sélectionnez une spécialité", "#f59e0b");
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une spécialité dans le tableau.");
            return;
        }
        if (!validateForm()) return;

        try {
            Specialite s = buildFromForm();
            s.setId(selectedSpecialite.getId());
            specialiteService.update(s);
            loadSpecialites();
            clearForm();
            showStatus("✅ Spécialité modifiée avec succès !", "#10b981");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Spécialité modifiée avec succès !");
            animateButton(btnUpdate);
        } catch (Exception e) {
            showStatus("❌ Erreur lors de la modification", "#ef4444");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    public void deleteSpecialite() {
        if (selectedSpecialite == null) {
            showStatus("⚠️ Sélectionnez une spécialité", "#f59e0b");
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une spécialité dans le tableau.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer « " + selectedSpecialite.getNom() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer cette spécialité ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    specialiteService.delete(selectedSpecialite.getId());
                    loadSpecialites();
                    clearForm();
                    showStatus("🗑️ Spécialité supprimée", "#ef4444");
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Spécialité supprimée !");
                } catch (Exception e) {
                    showStatus("❌ Erreur lors de la suppression", "#ef4444");
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void clearForm() {
        tfNom.clear();
        tfDescription.clear();
        selectedSpecialite = null;
        tableSpecialites.getSelectionModel().clearSelection();
        showStatus("🔄 Formulaire réinitialisé", "#6b7280");
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            tableSpecialites.setItems(specialiteList);
        } else {
            FilteredList<Specialite> filtered = specialiteList.filtered(s ->
                    (s.getNom() != null && s.getNom().toLowerCase().contains(query)) ||
                    (s.getDescription() != null && s.getDescription().toLowerCase().contains(query))
            );
            tableSpecialites.setItems(filtered);
        }
        updateCount();
    }

    private Specialite buildFromForm() {
        Specialite s = new Specialite();
        s.setNom(tfNom.getText().trim());
        s.setDescription(tfDescription.getText().trim());
        return s;
    }

    private boolean validateForm() {
        if (tfNom.getText().trim().isEmpty()) {
            showStatus("⚠️ Le nom est obligatoire", "#f59e0b");
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom de la spécialité est obligatoire.");
            tfNom.requestFocus();
            return false;
        }
        return true;
    }

    private void showStatus(String message, String color) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        FadeTransition ft = new FadeTransition(Duration.millis(300), lblStatus);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void animateButton(Button btn) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.1);
        st.setToY(1.1);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
