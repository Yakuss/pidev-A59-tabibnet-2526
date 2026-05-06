package com.pidev.controllers;

import com.pidev.models.Magazine;
import com.pidev.services.ServiceMagazine;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur admin Magazine.
 * TableView + popup modal pour Ajouter/Modifier.
 */
public class MagazineAdminController {

    @FXML private TableView<Magazine>          tableMagazines;
    @FXML private TableColumn<Magazine, Integer> colId;
    @FXML private TableColumn<Magazine, String>  colTitre, colDescription, colStatut, colDate, colActions;

    private StackPane contentArea;

    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Magazine> obsMagazines = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurerTable();
        chargerDonnees();
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    // ── Table ────────────────────────────────────────────────────────────────
    private void configurerTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colDescription.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateCreate() != null ? c.getValue().getDateCreate().toLocalDate().toString() : "—"));

        // Colonne Actions : boutons Modifier + Supprimer
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDel  = new Button("🗑");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color:#1f6feb;-fx-text-fill:white;-fx-background-radius:4;-fx-cursor:hand;-fx-padding:4 8;");
                btnDel.setStyle("-fx-background-color:#da3633;-fx-text-fill:white;-fx-background-radius:4;-fx-cursor:hand;-fx-padding:4 8;");
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> ouvrirFormulaireModifier(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e  -> supprimerMagazine(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableMagazines.setItems(obsMagazines);
    }

    private void chargerDonnees() {
        try {
            obsMagazines.setAll(serviceMagazine.afficherTout());
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ── Navbar ───────────────────────────────────────────────────────────────
    @FXML
    private void showMagazines() { /* déjà sur cette vue */ }

    @FXML
    private void showArticles() {
        if (contentArea == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/ArticleAdminView.fxml"));
            Node view = loader.load();
            ArticleAdminController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ── Popup Ajouter ────────────────────────────────────────────────────────
    @FXML
    private void ouvrirFormulaireAjout() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFormulaireModifier(Magazine existant) {
        ouvrirFormulaire(existant);
    }

    private void ouvrirFormulaire(Magazine existant) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existant == null ? "Nouveau Magazine" : "Modifier le Magazine");
        dialog.setResizable(false);

        // ── Champs ──────────────────────────────────────────────────────────
        TextField tfTitre = new TextField(existant != null ? existant.getTitre() : "");
        tfTitre.setPromptText("Entrez le titre...");
        tfTitre.setStyle(fieldStyle());

        TextArea taDesc = new TextArea(existant != null ? existant.getDescription() : "");
        taDesc.setPromptText("Description du magazine...");
        taDesc.setPrefRowCount(4);
        taDesc.setWrapText(true);
        taDesc.setStyle(fieldStyle());

        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("draft", "published", "archived");
        cbStatut.setValue(existant != null ? existant.getStatut() : "draft");
        cbStatut.setMaxWidth(Double.MAX_VALUE);
        cbStatut.setStyle(fieldStyle());

        // ── PDF : bouton FileChooser ─────────────────────────────────────────
        final String[] pdfPath = { existant != null && existant.getPdfFile() != null ? existant.getPdfFile() : null };
        Label lblPdfVal = new Label(pdfPath[0] != null ? new java.io.File(pdfPath[0]).getName() : "Aucun fichier sélectionné");
        lblPdfVal.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");

        Button btnPdf = new Button("📄 Parcourir PDF...");
        btnPdf.setStyle("-fx-background-color:#1e293b;-fx-text-fill:white;-fx-background-radius:6;" +
                "-fx-cursor:hand;-fx-padding:8 16;-fx-font-size:13px;");
        btnPdf.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Choisir un fichier PDF");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            java.io.File f = fc.showOpenDialog(dialog);
            if (f != null) {
                pdfPath[0] = f.getAbsolutePath();
                lblPdfVal.setText(f.getName());
            }
        });
        HBox pdfRow = new HBox(10, btnPdf, lblPdfVal);
        pdfRow.setAlignment(Pos.CENTER_LEFT);

        // ── Boutons ──────────────────────────────────────────────────────────
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color:transparent;-fx-text-fill:#64748b;-fx-border-color:#30363d;" +
                "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 20;-fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> dialog.close());

        Button btnSave = new Button(existant == null ? "Créer" : "Enregistrer");
        btnSave.setStyle("-fx-background-color:#238636;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-padding:8 24;-fx-background-radius:6;-fx-cursor:hand;");
        btnSave.setOnAction(e -> {
            // Validation
            if (tfTitre.getText().trim().length() < 2) {
                alert(Alert.AlertType.ERROR, "Erreur", "Titre obligatoire (min 2 caractères)."); return;
            }
            if (taDesc.getText().trim().length() < 5) {
                alert(Alert.AlertType.ERROR, "Erreur", "Description obligatoire (min 5 caractères)."); return;
            }
            if (cbStatut.getValue() == null) {
                alert(Alert.AlertType.ERROR, "Erreur", "Statut obligatoire."); return;
            }
            try {
                if (existant == null) {
                    Magazine m = new Magazine(tfTitre.getText().trim(), taDesc.getText().trim(),
                            cbStatut.getValue(), pdfPath[0]);
                    serviceMagazine.ajouter(m);
                    obsMagazines.add(m);
                } else {
                    existant.setTitre(tfTitre.getText().trim());
                    existant.setDescription(taDesc.getText().trim());
                    existant.setStatut(cbStatut.getValue());
                    existant.setPdfFile(pdfPath[0]);
                    serviceMagazine.modifier(existant);
                    tableMagazines.refresh();
                }
                dialog.close();
                alert(Alert.AlertType.INFORMATION, "Succès",
                        existant == null ? "Magazine ajouté." : "Magazine modifié.");
            } catch (SQLException ex) {
                alert(Alert.AlertType.ERROR, "Erreur BD", ex.getMessage());
            }
        });

        // ── Layout popup ─────────────────────────────────────────────────────
        Label header = new Label(existant == null ? "Nouveau Magazine" : "Modifier le Magazine");
        header.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#1f3b5c;");

        VBox form = new VBox(14,
                header,
                fieldBlock("Titre *",       tfTitre),
                fieldBlock("Description *", taDesc),
                fieldBlock("Statut *",      cbStatut),
                fieldBlockNode("Fichier PDF", pdfRow),
                new HBox(10, btnAnnuler, btnSave) {{ setAlignment(Pos.CENTER_RIGHT); setPadding(new Insets(8, 0, 0, 0)); }}
        );
        form.setPadding(new Insets(28));
        form.setStyle("-fx-background-color:white;");
        form.setPrefWidth(420);

        dialog.setScene(new Scene(form));
        dialog.showAndWait();
    }

    // ── Supprimer ────────────────────────────────────────────────────────────
    private void supprimerMagazine(Magazine magazine) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + magazine.getTitre() + "\" et tous ses articles ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    serviceMagazine.supprimer(magazine.getId());
                    obsMagazines.remove(magazine);
                } catch (SQLException e) {
                    alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────
    private VBox fieldBlock(String labelText, javafx.scene.control.Control field) {
        Label l = new Label(labelText);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#1f3b5c;-fx-font-size:13px;");
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(6, l, field);
    }

    private VBox fieldBlockNode(String labelText, javafx.scene.Node node) {
        Label l = new Label(labelText);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#1f3b5c;-fx-font-size:13px;");
        return new VBox(6, l, node);
    }

    private String fieldStyle() {
        return "-fx-background-color:#f8fafc;-fx-border-color:#cbd5e1;-fx-border-radius:6;" +
               "-fx-background-radius:6;-fx-padding:8 12;-fx-font-size:13px;";
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
