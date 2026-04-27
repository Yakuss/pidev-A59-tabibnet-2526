package edu.connexion3a77.controllers.ArticleController;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.entities.Magazine;
import edu.connexion3a77.services.ServiceArticle;
import edu.connexion3a77.services.ServiceMagazine;
import javafx.beans.property.*;
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
import java.util.List;

public class articleAdmin {

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFiltrePublic, cbTri;
    @FXML private Label lblArticleCount;

    @FXML private TableView<Article> tableArticles;
    @FXML private TableColumn<Article, Integer>       colId, colViews;
    @FXML private TableColumn<Article, String>        colTitre, colAuteur, colPublic, colStatut;
    @FXML private TableColumn<Article, LocalDateTime> colDatePub;
    @FXML private TableColumn<Article, Void>          colActions;

    @FXML private Button btnPrev, btnNext;
    @FXML private Label  lblPage;

    private final ServiceArticle  serviceArticle  = new ServiceArticle();
    private final ServiceMagazine serviceMagazine = new ServiceMagazine();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();

    private static final String UPLOAD_DIR = "uploads/";
    private static final long   MAX_SIZE   = 5 * 1024 * 1024;

    private int pageCourante = 1;
    private int totalPages   = 1;

    @FXML
    private void initialize() {
        if (cbFiltrePublic != null) { cbFiltrePublic.getItems().addAll("Tous","Enfants","Jeunes","Adultes"); cbFiltrePublic.setValue("Tous"); }
        if (cbTri          != null) { cbTri.getItems().addAll("Plus récents","Plus populaires"); cbTri.setValue("Plus récents"); }
        configurerTable();
        charger();
    }

    private void configurerTable() {
        if (tableArticles == null) return;
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colAuteur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuteur()));
        colPublic.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPublicCible()));
        colViews.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getViews()).asObject());
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut()));
        colDatePub.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDatePub()));

        colStatut.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("status-badge"); }
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                badge.setText(v);
                badge.getStyleClass().removeAll("published","draft","archived");
                badge.getStyleClass().add(v);
                setGraphic(badge);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏ Modifier");
            private final Button btnDel  = new Button("🗑 Supprimer");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().add("action-edit-button");
                btnDel.getStyleClass().add("action-delete-button");
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

    @FXML private void onSearchChanged()  { pageCourante = 1; charger(); }
    @FXML private void onFiltreChanged()  { pageCourante = 1; charger(); }
    @FXML private void onTriChanged()     { pageCourante = 1; charger(); }
    @FXML private void pagePrecedente()   { if (pageCourante > 1)          { pageCourante--; charger(); } }
    @FXML private void pageSuivante()     { if (pageCourante < totalPages) { pageCourante++; charger(); } }
    @FXML private void ouvrirFormulaireAjout() { ouvrirFormulaire(null); }

    @FXML
    private void retourMagazines() {
        if (tableArticles != null && tableArticles.getScene() != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) tableArticles.getScene().getWindow();
            stage.close();
        }
    }

    private void charger() {
        String recherche   = (tfSearch != null) ? tfSearch.getText() : null;
        String publicCible = getPublicCibleFiltre();
        String tri = (cbTri != null && "Plus populaires".equals(cbTri.getValue())) ? "populaire" : "recent";
        try {
            int total = serviceArticle.compterAvecFiltres(recherche, publicCible);
            totalPages = Math.max(1, (int) Math.ceil((double) total / ServiceArticle.PAGE_SIZE));
            if (pageCourante > totalPages) pageCourante = totalPages;
            if (lblPage         != null) lblPage.setText("Page " + pageCourante + " / " + totalPages);
            if (btnPrev         != null) btnPrev.setDisable(pageCourante <= 1);
            if (btnNext         != null) btnNext.setDisable(pageCourante >= totalPages);
            if (lblArticleCount != null) lblArticleCount.setText(total + " article(s)");
            obsArticles.clear();
            obsArticles.addAll(serviceArticle.rechercherAvecFiltres(recherche, publicCible, tri, pageCourante));
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private String getPublicCibleFiltre() {
        if (cbFiltrePublic == null) return null;
        String v = cbFiltrePublic.getValue();
        return (v == null || "Tous".equals(v)) ? null : v;
    }

    private void ouvrirFormulaire(Article existant) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existant == null ? "Nouvel Article" : "Modifier l'Article");

        TextField tfTitre  = new TextField();  tfTitre.setPromptText("Titre");
        TextField tfAuteur = new TextField();  tfAuteur.setPromptText("Auteur");
        DatePicker dpDate  = new DatePicker();
        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("draft","published"); cbStatut.setPromptText("Statut");
        ComboBox<String> cbPublic = new ComboBox<>();
        cbPublic.getItems().addAll("Enfants","Jeunes","Adultes"); cbPublic.setPromptText("Public cible");
        TextArea taSummary = new TextArea(); taSummary.setPromptText("Résumé"); taSummary.setPrefRowCount(3); taSummary.setWrapText(true);
        TextArea taResume  = new TextArea(); taResume.setPromptText("Contenu"); taResume.setPrefRowCount(6); taResume.setWrapText(true);

        ComboBox<Magazine> cbMag = new ComboBox<>();
        try {
            cbMag.getItems().addAll(serviceMagazine.afficherTout());
            cbMag.setConverter(new StringConverter<>() {
                public String toString(Magazine m)   { return m == null ? "" : m.getTitre(); }
                public Magazine fromString(String s) { return null; }
            });
        } catch (SQLException ignored) {}

        // Upload image
        Label lblImage = new Label("Aucune image");
        Button btnImage = new Button("📷 Image (JPG/PNG, max 5 Mo)");
        final String[] imgPath = {null};
        btnImage.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.jpg","*.jpeg","*.png"));
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                if (f.length() > MAX_SIZE) { alert(Alert.AlertType.ERROR,"Trop grand","Max 5 Mo."); return; }
                imgPath[0] = copierFichier(f, "images");
                lblImage.setText(f.getName());
            }
        });

        // Upload PDF
        Label lblPdf = new Label("Aucun PDF");
        Button btnPdf = new Button("📄 PDF joint (max 5 Mo)");
        final String[] pdfPath = {null};
        btnPdf.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                if (f.length() > MAX_SIZE) { alert(Alert.AlertType.ERROR,"Trop grand","Max 5 Mo."); return; }
                pdfPath[0] = copierFichier(f, "pdfs");
                lblPdf.setText(f.getName());
            }
        });

        if (existant != null) {
            tfTitre.setText(existant.getTitre());
            tfAuteur.setText(existant.getAuteur());
            if (existant.getDatePub() != null) dpDate.setValue(existant.getDatePub().toLocalDate());
            cbStatut.setValue(existant.getStatut());
            cbPublic.setValue(existant.getPublicCible());
            taSummary.setText(existant.getSummary());
            taResume.setText(existant.getResume());
            cbMag.setValue(existant.getMagazine());
            if (existant.getImage()   != null) { imgPath[0]  = existant.getImage();   lblImage.setText(new File(existant.getImage()).getName()); }
            if (existant.getPdfFile() != null) { pdfPath[0]  = existant.getPdfFile(); lblPdf.setText(new File(existant.getPdfFile()).getName()); }
        }

        Button btnSave = new Button(existant == null ? "✅ Publier" : "💾 Enregistrer");
        btnSave.setStyle("-fx-background-color:#1d4ed8;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:10 24;");
        btnSave.setOnAction(e -> {
            if (tfTitre.getText().trim().length() < 2) { alert(Alert.AlertType.ERROR,"Erreur","Titre trop court."); return; }
            if (cbStatut.getValue() == null)           { alert(Alert.AlertType.ERROR,"Erreur","Statut obligatoire."); return; }
            if (cbPublic.getValue() == null)           { alert(Alert.AlertType.ERROR,"Erreur","Public cible obligatoire."); return; }
            try {
                if (existant == null) {
                    Article a = new Article(tfTitre.getText().trim(), taResume.getText().trim(),
                            tfAuteur.getText().trim(),
                            dpDate.getValue() != null ? dpDate.getValue().atStartOfDay() : LocalDateTime.now(),
                            taSummary.getText().trim(), cbStatut.getValue(), imgPath[0]);
                    a.setPublicCible(cbPublic.getValue());
                    a.setPdfFile(pdfPath[0]);
                    a.setMagazine(cbMag.getValue());
                    serviceArticle.ajouter(a);
                } else {
                    existant.setTitre(tfTitre.getText().trim());
                    existant.setResume(taResume.getText().trim());
                    existant.setAuteur(tfAuteur.getText().trim());
                    existant.setDatePub(dpDate.getValue() != null ? dpDate.getValue().atStartOfDay() : null);
                    existant.setSummary(taSummary.getText().trim());
                    existant.setStatut(cbStatut.getValue());
                    existant.setPublicCible(cbPublic.getValue());
                    existant.setImage(imgPath[0]);
                    existant.setPdfFile(pdfPath[0]);
                    existant.setMagazine(cbMag.getValue());
                    serviceArticle.modifier(existant);
                }
                charger();
                dialog.close();
            } catch (SQLException ex) {
                alert(Alert.AlertType.ERROR, "Erreur BD", ex.getMessage());
            }
        });

        VBox form = new VBox(14,
                lbl("Titre"),  tfTitre,
                lbl("Auteur"), tfAuteur,
                new HBox(14, vbox("Date", dpDate), vbox("Statut", cbStatut), vbox("Public cible", cbPublic)),
                lbl("Magazine"), cbMag,
                lbl("Résumé"),  taSummary,
                lbl("Contenu"), taResume,
                lbl("Image de couverture"), btnImage, lblImage,
                lbl("Fichier PDF joint"),   btnPdf,   lblPdf,
                btnSave
        );
        form.setPadding(new Insets(28));
        form.setStyle("-fx-background-color:white;");
        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        dialog.setScene(new Scene(sp, 620, 700));
        dialog.showAndWait();
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

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#334155;-fx-font-size:13px;");
        return l;
    }

    private VBox vbox(String labelText, Control ctrl) {
        VBox v = new VBox(6, lbl(labelText), ctrl);
        HBox.setHgrow(v, Priority.ALWAYS);
        ctrl.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
