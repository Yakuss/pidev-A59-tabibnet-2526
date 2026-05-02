package com.pidev.controllers;

import com.pidev.models.Specialite;
import com.pidev.services.SpecialiteService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Controller for Specialite CRUD operations — card-based ListView style.
 */
public class SpecialiteController {

    @FXML private ListView<Specialite> listSpecialites;
    @FXML private TextField tfNom, searchField;
    @FXML private TextArea tfDescription;
    @FXML private Label lblStatus, lblCount, formTitle;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnClear;

    private final SpecialiteService specialiteService = new SpecialiteService();
    private ObservableList<Specialite> specialiteList = FXCollections.observableArrayList();
    private Specialite selectedSpecialite = null;

    // Color palette for specialty cards
    private static final String[] COLORS = {
        "#5b6ef5", "#22c55e", "#f59e0b", "#f43f5e", "#06b6d4",
        "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#a78bfa"
    };

    @FXML
    public void initialize() {
        setupListView();
        loadSpecialites();

        // Selection listener
        listSpecialites.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSpecialite = newVal;
                fillForm(newVal);
                formTitle.setText("📝 Modifier : " + newVal.getNom());
                formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
                showStatus("📝 Sélectionné : " + newVal.getNom(), "#a78bfa");
            }
        });

        // Live search
        searchField.textProperty().addListener((obs, o, n) -> onSearch());

        showStatus("✅ Module Spécialités chargé", "#22c55e");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LISTVIEW SETUP
    // ═══════════════════════════════════════════════════════════════════════

    private void setupListView() {
        listSpecialites.setCellFactory(lv -> new ListCell<Specialite>() {
            @Override
            protected void updateItem(Specialite s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createCard(s, getIndex()));
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 4 0;");
                }
            }
        });
    }

    private VBox createCard(Specialite s, int index) {
        String color = COLORS[index % COLORS.length];
        boolean isSelected = selectedSpecialite != null && selectedSpecialite.getId() == s.getId();

        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
            "-fx-background-color: " + (isSelected ? "#141826" : "#0e1220") + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + (isSelected ? color : "#252d42") + ";" +
            "-fx-border-width: " + (isSelected ? "1.5" : "1") + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + color + "44, 10, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: " + (isSelected ? "#141826" : "#0e1220") + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + (isSelected ? color : "#252d42") + ";" +
            "-fx-border-width: " + (isSelected ? "1.5" : "1") + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        ));

        // ── Header row ────────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle with initials
        StackPane avatar = new StackPane();
        avatar.setMinSize(42, 42);
        avatar.setMaxSize(42, 42);
        Region avatarBg = new Region();
        avatarBg.setStyle(
            "-fx-background-color: " + color + "22;" +
            "-fx-background-radius: 21;" +
            "-fx-border-color: " + color + "55;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 21;"
        );
        String initials = s.getNom() != null && s.getNom().length() >= 2
            ? s.getNom().substring(0, 2).toUpperCase() : "SP";
        Label avatarLbl = new Label(initials);
        avatarLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: 800;");
        avatar.getChildren().addAll(avatarBg, avatarLbl);

        // Name + ID
        VBox nameBox = new VBox(3);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameLabel = new Label(s.getNom() != null ? s.getNom() : "Sans nom");
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 15px; -fx-font-weight: 700;");
        nameLabel.setWrapText(true);

        Label idLabel = new Label("ID: " + s.getId());
        idLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");
        nameBox.getChildren().addAll(nameLabel, idLabel);

        // Specialty badge
        Label badge = new Label("🏥 Spécialité");
        badge.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-size: 10px; -fx-font-weight: 700;" +
            "-fx-padding: 3 10; -fx-background-radius: 20;" +
            "-fx-border-color: " + color + "33; -fx-border-width: 1; -fx-border-radius: 20;"
        );

        header.getChildren().addAll(avatar, nameBox, badge);

        // ── Description ───────────────────────────────────────────────────
        String desc = s.getDescription() != null && !s.getDescription().isEmpty()
            ? s.getDescription() : "Aucune description disponible.";
        if (desc.length() > 120) desc = desc.substring(0, 120) + "…";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-line-spacing: 2;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        // ── Divider ───────────────────────────────────────────────────────
        Region divider = new Region();
        divider.setMinHeight(1); divider.setMaxHeight(1);
        divider.setStyle("-fx-background-color: #1c2133;");

        // ── Footer ────────────────────────────────────────────────────────
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Char count chip
        int charCount = s.getDescription() != null ? s.getDescription().length() : 0;
        HBox charChip = makeChip("📝 " + charCount + " caractères", "#475569");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Quick delete button
        Button quickDelete = new Button("🗑");
        quickDelete.setStyle(
            "-fx-background-color: rgba(244,63,94,0.1); -fx-text-fill: #f43f5e;" +
            "-fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6;" +
            "-fx-border-color: rgba(244,63,94,0.3); -fx-border-width: 1; -fx-border-radius: 6;" +
            "-fx-cursor: hand;"
        );
        quickDelete.setOnMouseEntered(e -> quickDelete.setStyle(
            "-fx-background-color: #f43f5e; -fx-text-fill: white;" +
            "-fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6;" +
            "-fx-border-color: #f43f5e; -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;"
        ));
        quickDelete.setOnMouseExited(e -> quickDelete.setStyle(
            "-fx-background-color: rgba(244,63,94,0.1); -fx-text-fill: #f43f5e;" +
            "-fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6;" +
            "-fx-border-color: rgba(244,63,94,0.3); -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;"
        ));
        quickDelete.setOnAction(e -> {
            selectedSpecialite = s;
            deleteSpecialite();
        });

        // Quick edit button
        Button quickEdit = new Button("✏️ Modifier");
        quickEdit.setStyle(
            "-fx-background-color: rgba(91,110,245,0.1); -fx-text-fill: #818cf8;" +
            "-fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 12; -fx-background-radius: 6;" +
            "-fx-border-color: rgba(91,110,245,0.3); -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;"
        );
        quickEdit.setOnMouseEntered(e -> quickEdit.setStyle(
            "-fx-background-color: rgba(91,110,245,0.25); -fx-text-fill: #818cf8;" +
            "-fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 12; -fx-background-radius: 6;" +
            "-fx-border-color: #818cf8; -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;"
        ));
        quickEdit.setOnMouseExited(e -> quickEdit.setStyle(
            "-fx-background-color: rgba(91,110,245,0.1); -fx-text-fill: #818cf8;" +
            "-fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 4 12; -fx-background-radius: 6;" +
            "-fx-border-color: rgba(91,110,245,0.3); -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;"
        ));
        quickEdit.setOnAction(e -> {
            listSpecialites.getSelectionModel().select(s);
        });

        footer.getChildren().addAll(charChip, footerSpacer, quickEdit, quickDelete);

        card.getChildren().addAll(header, descLabel, divider, footer);
        return card;
    }

    private HBox makeChip(String text, String color) {
        HBox chip = new HBox();
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
            "-fx-background-color: " + color + "18; -fx-background-radius: 20;" +
            "-fx-padding: 3 10; -fx-border-color: " + color + "33;" +
            "-fx-border-width: 1; -fx-border-radius: 20;"
        );
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: 600;");
        chip.getChildren().add(lbl);
        return chip;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DATA
    // ═══════════════════════════════════════════════════════════════════════

    private void loadSpecialites() {
        try {
            specialiteList.setAll(specialiteService.getAll());
            listSpecialites.setItems(specialiteList);
            updateCount();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les spécialités: " + e.getMessage());
        }
    }

    private void updateCount() {
        if (lblCount != null)
            lblCount.setText(specialiteList.size() + " spécialité(s)");
    }

    private void fillForm(Specialite s) {
        tfNom.setText(s.getNom());
        tfDescription.setText(s.getDescription() != null ? s.getDescription() : "");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CRUD ACTIONS
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    public void addSpecialite() {
        if (!validateForm()) return;
        try {
            Specialite s = buildFromForm();
            specialiteService.add(s);
            loadSpecialites();
            clearForm();
            showStatus("✅ Spécialité ajoutée avec succès !", "#22c55e");
            animateButton(btnAdd);
        } catch (Exception e) {
            showStatus("❌ Erreur lors de l'ajout", "#ef4444");
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void updateSpecialite() {
        if (selectedSpecialite == null) {
            showStatus("⚠️ Sélectionnez une spécialité", "#f59e0b");
            showAlert(Alert.AlertType.WARNING, "Attention", "Cliquez sur une spécialité dans la liste.");
            return;
        }
        if (!validateForm()) return;
        try {
            Specialite s = buildFromForm();
            s.setId(selectedSpecialite.getId());
            specialiteService.update(s);
            loadSpecialites();
            clearForm();
            showStatus("✅ Spécialité modifiée avec succès !", "#22c55e");
            animateButton(btnUpdate);
        } catch (Exception e) {
            showStatus("❌ Erreur lors de la modification", "#ef4444");
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void deleteSpecialite() {
        if (selectedSpecialite == null) {
            showStatus("⚠️ Sélectionnez une spécialité", "#f59e0b");
            showAlert(Alert.AlertType.WARNING, "Attention", "Cliquez sur une spécialité dans la liste.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer « " + selectedSpecialite.getNom() + " » ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette spécialité ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    specialiteService.delete(selectedSpecialite.getId());
                    loadSpecialites();
                    clearForm();
                    showStatus("🗑️ Spécialité supprimée", "#ef4444");
                } catch (Exception e) {
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
        listSpecialites.getSelectionModel().clearSelection();
        if (formTitle != null) {
            formTitle.setText("➕ Ajouter une Spécialité");
            formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
        }
        showStatus("🔄 Formulaire réinitialisé", "#6b7280");
        // Refresh cards to remove selection highlight
        listSpecialites.refresh();
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            listSpecialites.setItems(specialiteList);
        } else {
            listSpecialites.setItems(specialiteList.filtered(s ->
                (s.getNom() != null && s.getNom().toLowerCase().contains(query)) ||
                (s.getDescription() != null && s.getDescription().toLowerCase().contains(query))
            ));
        }
        updateCount();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

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
        if (lblStatus == null) return;
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), lblStatus);
        ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
    }

    private void animateButton(Button btn) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.08); st.setToY(1.08);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
