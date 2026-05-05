package com.pidev.controllers;

import com.pidev.models.Article;
import com.pidev.models.Magazine;
import com.pidev.services.ServiceArticle;
import com.pidev.services.ServiceMagazine;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Contrôleur admin pour la gestion des articles.
 * Inclut upload image + PDF dans le formulaire.
 */
public class ArticleAdminController {

    // ── Upload config ────────────────────────────────────────────────────────
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final long   MAX_SIZE   = 5 * 1024 * 1024; // 5 Mo

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFiltrePublic, cbTri;
    @FXML private Label lblArticleCount, lblMagazineFiltre;

    @FXML private TableView<Article> tableArticles;
    @FXML private TableColumn<Article, Integer>       colId, colViews;
    @FXML private TableColumn<Article, String>        colTitre, colAuteur, colPublic, colStatut, colMagazine;
    @FXML private TableColumn<Article, LocalDateTime> colDatePub;
    @FXML private TableColumn<Article, Void>          colActions;

    @FXML private Button btnPrev, btnNext;
    @FXML private Label  lblPage;

    private final ServiceArticle  serviceArticle  = new ServiceArticle();
    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();

    private int pageCourante = 1;
    private int totalPages   = 1;
    private Magazine magazineFiltre = null;
    private javafx.scene.layout.StackPane contentArea;

    public void setContentArea(javafx.scene.layout.StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    private void initialize() {
        if (cbFiltrePublic != null) { cbFiltrePublic.getItems().addAll("Tous","Enfants","Jeunes","Adultes"); cbFiltrePublic.setValue("Tous"); }
        if (cbTri != null) { cbTri.getItems().addAll("Plus récents","Plus populaires"); cbTri.setValue("Plus récents"); }
        configurerTable();
        charger();
        // Recherche réactive : se déclenche à chaque frappe
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                pageCourante = 1;
                charger();
            });
        }
    }

    public void filtrerParMagazine(Magazine magazine) {
        this.magazineFiltre = magazine;
        if (lblMagazineFiltre != null) {
            lblMagazineFiltre.setText("Magazine : " + magazine.getTitre());
            lblMagazineFiltre.setVisible(true);
        }
        pageCourante = 1;
        charger();
    }

    private void configurerTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colAuteur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuteur()));
        colPublic.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPublicCible()));
        colViews.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getViews()).asObject());
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));
        colDatePub.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDatePub()));
        if (colMagazine != null) {
            colMagazine.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getMagazine() != null ? c.getValue().getMagazine().getTitre() : "—"));
        }

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏ Modifier");
            private final Button btnDel  = new Button("🗑 Supprimer");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color:#1f3b5c;-fx-text-fill:white;-fx-background-radius:4;-fx-cursor:hand;");
                btnDel.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:4;-fx-cursor:hand;");
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e  -> supprimerArticle(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
        tableArticles.setItems(obsArticles);
    }

    @FXML public void onSearchChanged() { pageCourante = 1; charger(); }
    @FXML private void onFiltreChanged() { pageCourante = 1; charger(); }
    @FXML private void onTriChanged()    { pageCourante = 1; charger(); }
    @FXML private void pagePrecedente()  { if (pageCourante > 1)          { pageCourante--; charger(); } }
    @FXML private void pageSuivante()    { if (pageCourante < totalPages) { pageCourante++; charger(); } }
    @FXML private void ouvrirFormulaireAjout() { ouvrirFormulaire(null); }

    @FXML
    private void retournerMagazines() {
        if (contentArea == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/MagazineAdminView.fxml"));
            javafx.scene.Node view = loader.load();
            MagazineAdminController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(view);
        } catch (java.io.IOException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void effacerFiltresMagazine() {
        magazineFiltre = null;
        if (lblMagazineFiltre != null) lblMagazineFiltre.setVisible(false);
        pageCourante = 1;
        charger();
    }

    private void charger() {
        String recherche = (tfSearch != null && tfSearch.getText() != null && !tfSearch.getText().isBlank())
                ? tfSearch.getText().trim() : null;
        String publicCible = getPublicCibleFiltre();
        String tri = (cbTri != null && "Plus populaires".equals(cbTri.getValue())) ? "populaire" : "recent";
        Integer magId = magazineFiltre != null ? magazineFiltre.getId() : null;
        try {
            int total = serviceArticle.compterAvecFiltres(recherche, publicCible, magId);
            totalPages = Math.max(1, (int) Math.ceil((double) total / ServiceArticle.PAGE_SIZE));
            if (pageCourante > totalPages) pageCourante = totalPages;
            if (lblPage         != null) lblPage.setText("Page " + pageCourante + " / " + totalPages);
            if (btnPrev         != null) btnPrev.setDisable(pageCourante <= 1);
            if (btnNext         != null) btnNext.setDisable(pageCourante >= totalPages);
            if (lblArticleCount != null) lblArticleCount.setText(total + " article(s)");
            obsArticles.setAll(serviceArticle.rechercherAvecFiltres(recherche, publicCible, tri, pageCourante, magId));
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private String getPublicCibleFiltre() {
        if (cbFiltrePublic == null) return null;
        String v = cbFiltrePublic.getValue();
        return (v == null || "Tous".equals(v)) ? null : v;
    }

    // ── Formulaire avec upload ───────────────────────────────────────────────
    private void ouvrirFormulaire(Article existant) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existant == null ? "Nouvel Article" : "Modifier l'Article");

        // Champs texte
        TextField tfTitre  = new TextField(existant != null ? existant.getTitre()  : ""); tfTitre.setPromptText("Titre *");
        TextField tfAuteur = new TextField(existant != null ? existant.getAuteur() : ""); tfAuteur.setPromptText("Auteur *");
        DatePicker dpDate  = new DatePicker(existant != null && existant.getDatePub() != null ? existant.getDatePub().toLocalDate() : null);
        dpDate.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("draft","published");
        cbStatut.setValue(existant != null ? existant.getStatut() : "draft");
        cbStatut.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbPublic = new ComboBox<>();
        cbPublic.getItems().addAll("Enfants","Jeunes","Adultes");
        cbPublic.setValue(existant != null ? existant.getPublicCible() : null);
        cbPublic.setPromptText("Public cible"); cbPublic.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Magazine> cbMag = new ComboBox<>();
        cbMag.setMaxWidth(Double.MAX_VALUE);
        cbMag.setConverter(new StringConverter<>() {
            public String toString(Magazine m)   { return m == null ? "" : m.getTitre(); }
            public Magazine fromString(String s) { return null; }
        });
        try {
            cbMag.getItems().addAll(serviceMagazine.afficherTout());
            if (existant != null) cbMag.setValue(existant.getMagazine());
            else if (magazineFiltre != null) cbMag.setValue(magazineFiltre);
        } catch (SQLException ignored) {}

        TextArea taResume = new TextArea(existant != null ? existant.getResume() : "");
        taResume.setPromptText("Contenu"); taResume.setPrefRowCount(5); taResume.setWrapText(true);

        TextField tfSummary = new TextField(existant != null && existant.getSummary() != null ? existant.getSummary() : "");
        tfSummary.setPromptText("Résumé court");

        // ── Upload Image ─────────────────────────────────────────────────────
        final String[] imgPath = { existant != null ? existant.getImage() : null };
        Label lblImageVal = new Label(imgPath[0] != null ? new File(imgPath[0]).getName() : "Aucune image");
        lblImageVal.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");

        Button btnImage = new Button("📷 Parcourir image...");
        btnImage.setStyle("-fx-background-color:#1e293b;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;-fx-padding:7 14;");
        btnImage.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une image de couverture");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images (JPG, PNG)", "*.jpg","*.jpeg","*.png"));
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                if (f.length() > MAX_SIZE) { alert(Alert.AlertType.ERROR, "Fichier trop grand", "Max 5 Mo."); return; }
                imgPath[0] = copierFichier(f, "images");
                if (imgPath[0] != null) lblImageVal.setText(f.getName());
            }
        });

        // ── Upload PDF ───────────────────────────────────────────────────────
        final String[] pdfPath = { existant != null ? existant.getPdfFile() : null };
        Label lblPdfVal = new Label(pdfPath[0] != null ? new File(pdfPath[0]).getName() : "Aucun PDF");
        lblPdfVal.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");

        Button btnPdf = new Button("📄 Parcourir PDF...");
        btnPdf.setStyle("-fx-background-color:#1e293b;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;-fx-padding:7 14;");
        btnPdf.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un fichier PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                if (f.length() > MAX_SIZE) { alert(Alert.AlertType.ERROR, "Fichier trop grand", "Max 5 Mo."); return; }
                pdfPath[0] = copierFichier(f, "pdfs");
                if (pdfPath[0] != null) lblPdfVal.setText(f.getName());
            }
        });

        // ── Boutons action ───────────────────────────────────────────────────
        Button btnSave = new Button(existant == null ? "Créer" : "Enregistrer");
        btnSave.setStyle("-fx-background-color:#1f3b5c;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:8 20;-fx-background-radius:6;-fx-cursor:hand;");
        Button btnCancel = new Button("Annuler");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            if (tfTitre.getText().trim().length() < 2) { alert(Alert.AlertType.ERROR,"Erreur","Titre min 2 caractères."); return; }
            if (tfAuteur.getText().trim().isEmpty())   { alert(Alert.AlertType.ERROR,"Erreur","Auteur obligatoire."); return; }
            if (cbStatut.getValue() == null)           { alert(Alert.AlertType.ERROR,"Erreur","Statut obligatoire."); return; }
            if (cbPublic.getValue() == null)           { alert(Alert.AlertType.ERROR,"Erreur","Public cible obligatoire."); return; }
            try {
                if (existant == null) {
                    Article a = new Article(tfTitre.getText().trim(), taResume.getText().trim(),
                            tfAuteur.getText().trim(),
                            dpDate.getValue() != null ? dpDate.getValue().atStartOfDay() : LocalDateTime.now(),
                            tfSummary.getText().trim(), cbStatut.getValue(), imgPath[0]);
                    a.setPublicCible(cbPublic.getValue());
                    a.setMagazine(cbMag.getValue());
                    a.setPdfFile(pdfPath[0]);
                    serviceArticle.ajouter(a);
                } else {
                    existant.setTitre(tfTitre.getText().trim());
                    existant.setResume(taResume.getText().trim());
                    existant.setAuteur(tfAuteur.getText().trim());
                    existant.setDatePub(dpDate.getValue() != null ? dpDate.getValue().atStartOfDay() : null);
                    existant.setStatut(cbStatut.getValue());
                    existant.setPublicCible(cbPublic.getValue());
                    existant.setSummary(tfSummary.getText().trim());
                    existant.setMagazine(cbMag.getValue());
                    existant.setImage(imgPath[0]);
                    existant.setPdfFile(pdfPath[0]);
                    serviceArticle.modifier(existant);
                }
                charger();
                dialog.close();
            } catch (SQLException ex) {
                alert(Alert.AlertType.ERROR, "Erreur BD", ex.getMessage());
            }
        });

        // ── Layout formulaire ────────────────────────────────────────────────
        VBox form = new VBox(12,
                label("Titre *"), tfTitre,
                label("Auteur *"), tfAuteur,
                new HBox(10, vbox("Date", dpDate), vbox("Statut *", cbStatut), vbox("Public *", cbPublic)),
                label("Magazine"), cbMag,
                label("Contenu"), taResume,
                label("Image de couverture (JPG/PNG, max 5 Mo)"),
                new HBox(10, btnImage, lblImageVal),
                label("Fichier PDF joint (max 5 Mo)"),
                new HBox(10, btnPdf, lblPdfVal),
                new HBox(10, btnCancel, btnSave)
        );
        form.setPadding(new Insets(20));
        ScrollPane sp = new ScrollPane(form); sp.setFitToWidth(true);
        dialog.setScene(new Scene(sp, 600, 680));
        dialog.showAndWait();
    }

    // ── Upload helper ────────────────────────────────────────────────────────
    private String copierFichier(File source, String sousRep) {
        try {
            Path dir = Paths.get(UPLOAD_DIR + sousRep);
            Files.createDirectories(dir);
            Path dest = dir.resolve(System.currentTimeMillis() + "_" + source.getName());
            Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toString();
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Upload échoué", e.getMessage());
            return null;
        }
    }

    private void supprimerArticle(Article article) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + article.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try { serviceArticle.supprimer(article.getId()); charger(); }
                catch (SQLException e) { alert(Alert.AlertType.ERROR,"Erreur",e.getMessage()); }
            }
        });
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#1f3b5c;");
        return l;
    }

    private VBox vbox(String labelText, javafx.scene.control.Control field) {
        VBox b = new VBox(4, label(labelText), field);
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
