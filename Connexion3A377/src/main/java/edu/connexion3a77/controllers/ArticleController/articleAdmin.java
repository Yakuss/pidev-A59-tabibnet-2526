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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

        // ── Champs ──────────────────────────────────────────────────────────
        TextField tfTitre  = new TextField(); tfTitre.setPromptText("Entrez le titre de l'article");
        tfTitre.setStyle(fieldStyle());

        TextField tfAuteur = new TextField(); tfAuteur.setPromptText("Entrez le nom de l'auteur");
        tfAuteur.setStyle(fieldStyle());

        DatePicker dpDate = new DatePicker();
        dpDate.setStyle(fieldStyle()); dpDate.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("draft","published");
        cbStatut.setPromptText("Choisir..."); cbStatut.setMaxWidth(Double.MAX_VALUE);
        cbStatut.setStyle(fieldStyle());

        ComboBox<String> cbPublic = new ComboBox<>();
        cbPublic.getItems().addAll("Enfants","Jeunes","Adultes");
        cbPublic.setPromptText("Choisir..."); cbPublic.setMaxWidth(Double.MAX_VALUE);
        cbPublic.setStyle(fieldStyle());

        ComboBox<Magazine> cbMag = new ComboBox<>();
        cbMag.setMaxWidth(Double.MAX_VALUE); cbMag.setStyle(fieldStyle());
        try {
            cbMag.getItems().addAll(serviceMagazine.afficherTout());
            cbMag.setConverter(new StringConverter<>() {
                public String toString(Magazine m)   { return m == null ? "" : m.getTitre(); }
                public Magazine fromString(String s) { return null; }
            });
        } catch (SQLException ignored) {}

        TextArea taResume = new TextArea();
        taResume.setPromptText("Entrez le contenu de l'article");
        taResume.setPrefRowCount(6); taResume.setWrapText(true);
        taResume.setStyle(fieldStyle());

        // Boutons traduction contenu
        Button btnTrAr = new Button("🌐 Arabe");
        Button btnTrFr = new Button("🌐 Français");
        Button btnTrEn = new Button("🌐 Anglais");
        for (Button b : new Button[]{btnTrAr, btnTrFr, btnTrEn}) {
            b.setStyle("-fx-background-color:#1f3b5c;-fx-text-fill:white;-fx-background-radius:16;-fx-padding:5 14;-fx-cursor:hand;-fx-font-size:12px;");
        }
        btnTrAr.setOnAction(e -> traduireChamp(taResume, "arabe",   dialog));
        btnTrFr.setOnAction(e -> traduireChamp(taResume, "francais", dialog));
        btnTrEn.setOnAction(e -> traduireChamp(taResume, "englais",  dialog));
        HBox tradRow = new HBox(8, new Label("Traduire :"), btnTrAr, btnTrFr, btnTrEn);
        tradRow.setAlignment(Pos.CENTER_LEFT);

        // Upload image
        Label lblImageVal = new Label("Aucune image sélectionnée");
        lblImageVal.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        Button btnImage = new Button("📷 Parcourir...");
        btnImage.setStyle(secondaryBtnStyle());
        final String[] imgPath = {null};
        btnImage.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.jpg","*.jpeg","*.png"));
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                if (f.length() > MAX_SIZE) { alert(Alert.AlertType.ERROR,"Trop grand","Max 5 Mo."); return; }
                imgPath[0] = copierFichier(f, "images");
                lblImageVal.setText(f.getName());
            }
        });

        // Upload PDF
        Label lblPdfVal = new Label("Aucun PDF sélectionné");
        lblPdfVal.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        Button btnPdf = new Button("📄 Parcourir...");
        btnPdf.setStyle(secondaryBtnStyle());
        final String[] pdfPath = {null};
        btnPdf.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
            File f = fc.showOpenDialog(dialog);
            if (f != null) {
                if (f.length() > MAX_SIZE) { alert(Alert.AlertType.ERROR,"Trop grand","Max 5 Mo."); return; }
                pdfPath[0] = copierFichier(f, "pdfs");
                lblPdfVal.setText(f.getName());
            }
        });

        // Pré-remplissage si modification
        if (existant != null) {
            tfTitre.setText(existant.getTitre());
            tfAuteur.setText(existant.getAuteur());
            if (existant.getDatePub() != null) dpDate.setValue(existant.getDatePub().toLocalDate());
            cbStatut.setValue(existant.getStatut());
            cbPublic.setValue(existant.getPublicCible());
            taResume.setText(existant.getResume());
            cbMag.setValue(existant.getMagazine());
            if (existant.getImage()   != null) { imgPath[0] = existant.getImage();   lblImageVal.setText(new File(existant.getImage()).getName()); }
            if (existant.getPdfFile() != null) { pdfPath[0] = existant.getPdfFile(); lblPdfVal.setText(new File(existant.getPdfFile()).getName()); }
        }

        // ── Boutons action ───────────────────────────────────────────────────
        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle(secondaryBtnStyle());
        btnCancel.setOnAction(e -> dialog.close());

        Button btnSave = new Button(existant == null ? "Créer l'Article" : "Enregistrer");
        btnSave.setStyle("-fx-background-color:#1f3b5c;-fx-text-fill:white;-fx-font-weight:bold;" +
                         "-fx-padding:10 24;-fx-background-radius:6;-fx-cursor:hand;");
        btnSave.setOnAction(e -> {
            if (tfTitre.getText().trim().length() < 2) { alert(Alert.AlertType.ERROR,"Erreur","Titre obligatoire (min 2 caractères)."); return; }
            if (tfAuteur.getText().trim().isEmpty())   { alert(Alert.AlertType.ERROR,"Erreur","Auteur obligatoire."); return; }
            if (cbStatut.getValue() == null)           { alert(Alert.AlertType.ERROR,"Erreur","Statut obligatoire."); return; }
            if (cbPublic.getValue() == null)           { alert(Alert.AlertType.ERROR,"Erreur","Public cible obligatoire."); return; }
            if (taResume.getText().trim().isEmpty())   { alert(Alert.AlertType.ERROR,"Erreur","Le contenu est obligatoire."); return; }
            try {
                if (existant == null) {
                    Article a = new Article(tfTitre.getText().trim(), taResume.getText().trim(),
                            tfAuteur.getText().trim(),
                            dpDate.getValue() != null ? dpDate.getValue().atStartOfDay() : LocalDateTime.now(),
                            "", cbStatut.getValue(), imgPath[0]);
                    a.setPublicCible(cbPublic.getValue());
                    a.setPdfFile(pdfPath[0]);
                    a.setMagazine(cbMag.getValue());
                    serviceArticle.ajouter(a);
                } else {
                    existant.setTitre(tfTitre.getText().trim());
                    existant.setResume(taResume.getText().trim());
                    existant.setAuteur(tfAuteur.getText().trim());
                    existant.setDatePub(dpDate.getValue() != null ? dpDate.getValue().atStartOfDay() : null);
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

        HBox actionRow = new HBox(12, btnCancel, btnSave);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(10, 0, 0, 0));

        // ── Layout ───────────────────────────────────────────────────────────
        Label header = new Label(existant == null ? "Nouvel Article" : "Modifier l'Article");
        header.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1f3b5c;");

        Label subtitle = new Label("Remplissez les informations pour " +
                (existant == null ? "créer un nouvel article" : "modifier cet article"));
        subtitle.setStyle("-fx-text-fill:#64748b;-fx-font-size:13px;");
        subtitle.setWrapText(true);

        VBox content = new VBox(16,
                header, subtitle,
                fieldBlock(true,  "Titre",        tfTitre),
                fieldBlock(true,  "Auteur",        tfAuteur),
                new HBox(14,
                    fieldBlockH(false, "Date",         dpDate),
                    fieldBlockH(true,  "Statut",       cbStatut),
                    fieldBlockH(true,  "Public cible", cbPublic)
                ),
                fieldBlock(false, "Magazine",      cbMag),
                fieldBlock(true,  "Contenu",       taResume),
                tradRow,
                fieldBlock(false, "Image de couverture (JPG/PNG, max 5 Mo)",
                        rowWithBtn(btnImage, lblImageVal)),
                fieldBlock(false, "Fichier PDF joint (max 5 Mo)",
                        rowWithBtn(btnPdf, lblPdfVal)),
                actionRow
        );
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color:white;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:white;");
        dialog.setScene(new Scene(sp, 640, 720));
        dialog.showAndWait();
    }

    // ── Helpers style ────────────────────────────────────────────────────────
    private String fieldStyle() {
        return "-fx-background-color:#f8fafc;-fx-border-color:#cbd5e1;-fx-border-radius:6;" +
               "-fx-background-radius:6;-fx-padding:8 12;-fx-font-size:13px;";
    }

    private String secondaryBtnStyle() {
        return "-fx-background-color:transparent;-fx-text-fill:#64748b;-fx-font-weight:bold;" +
               "-fx-cursor:hand;-fx-border-color:#cbd5e1;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 16;";
    }

    /** Bloc label + champ avec étoile rouge si obligatoire */
    private VBox fieldBlock(boolean required, String labelText, javafx.scene.Node field) {
        HBox labelRow = buildLabelRow(required, labelText);
        VBox box = new VBox(6, labelRow, field);
        return box;
    }

    /** Même chose mais avec HBox.hgrow pour les champs côte à côte */
    private VBox fieldBlockH(boolean required, String labelText, Control field) {
        HBox labelRow = buildLabelRow(required, labelText);
        field.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(6, labelRow, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private HBox buildLabelRow(boolean required, String labelText) {
        Label l = new Label(labelText);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:#1f3b5c;-fx-font-size:13px;");
        HBox row = new HBox(4, l);
        if (required) {
            Label star = new Label("*");
            star.setStyle("-fx-text-fill:#ef4444;-fx-font-weight:bold;");
            row.getChildren().add(star);
        }
        return row;
    }

    private HBox rowWithBtn(Button btn, Label lbl) {
        HBox row = new HBox(10, btn, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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

    private void traduireChamp(TextArea champ, String targetLang, Stage owner) {
        if (champ.getText().trim().isEmpty()) return;
        String body = "{\"text\": " + jsonEscape(champ.getText().trim()) +
                      ", \"target_lang\": \"" + targetLang + "\"}";
        HttpClient.newHttpClient()
            .sendAsync(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8000/translate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
            .thenAccept(response -> javafx.application.Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    String translated = extractJsonValue(response.body(), "translated_text");
                    champ.setText(translated);
                } else {
                    alert(Alert.AlertType.ERROR, "Erreur", "Traduction échouée (" + response.statusCode() + ")");
                }
            }))
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() ->
                    alert(Alert.AlertType.ERROR, "Erreur", "API inaccessible : " + ex.getMessage())
                );
                return null;
            });
    }

    /** Extrait la valeur d'une clé dans un JSON simple. */
    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return json;
        int colon = json.indexOf(":", idx + search.length());
        int start = json.indexOf("\"", colon + 1);
        if (start < 0) return json;
        int end = start + 1;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
            end++;
        }
        return json.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"");
    }

    private String jsonEscape(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"")
                          .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
