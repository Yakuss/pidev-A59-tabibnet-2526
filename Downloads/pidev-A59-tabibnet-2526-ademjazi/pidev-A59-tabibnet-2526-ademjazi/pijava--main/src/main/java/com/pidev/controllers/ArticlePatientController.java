package com.pidev.controllers;

import com.pidev.models.Article;
import com.pidev.services.ServiceArticle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;

/**
 * Contrôleur patient pour la liste et le détail des articles.
 * Peut être filtré par magazine via filtrerParMagazine().
 */
public class ArticlePatientController {

    // --- Toolbar ---
    @FXML private TextField tfSearch;
    @FXML private ToggleButton btnTous, btnEnfants, btnJeunes, btnAdultes;
    @FXML private ComboBox<String> cbTri;
    @FXML private Label lblMagazineFiltre;

    // --- Liste + pagination ---
    @FXML private ListView<Article> listArticles;
    @FXML private Button btnPrev, btnNext;
    @FXML private Label lblPage;

    // --- Détail ---
    @FXML private VBox detailPane;
    @FXML private Label lblTitre, lblAuteur, lblDatePub, lblViews, lblStatut, lblPublicCible;
    @FXML private TextArea taResume;
    @FXML private Button btnOuvrirPdf;

    // --- Résumé IA ---
    @FXML private Button resumeButton;
    @FXML private TextArea resumeArea;
    @FXML private VBox resumeBox;

    // --- Traduction ---
    @FXML private HBox tradRow;
    @FXML private Button btnTradAr, btnTradFr, btnTradEn;

    // --- Retour ---
    @FXML private Button btnRetourMagazines;
    // --- Recommandation IA ---
    @FXML private Button btnRecommandLinks;

    private StackPane contentArea;

    private final ServiceArticle serviceArticle = new ServiceArticle();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();

    private int pageCourante = 1;
    private int totalPages   = 1;
    private String pdfCourant = null;
    private Integer magazineIdFiltre = null;

    @FXML
    private void initialize() {
        if (cbTri != null) { cbTri.getItems().addAll("Plus récents","Plus populaires"); cbTri.setValue("Plus récents"); }
        if (listArticles != null) {
            listArticles.setItems(obsArticles);
            listArticles.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Article a, boolean empty) {
                    super.updateItem(a, empty);
                    if (empty || a == null) setText(null);
                    else setText("📄 " + a.getTitre() + "\n👁 " + a.getViews() + " vues  |  " +
                            (a.getPublicCible() != null ? a.getPublicCible() : "Tous"));
                }
            });
            listArticles.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, article) -> { if (article != null) afficherDetails(article); });
        }
        charger();
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    /** Filtre les articles d'un magazine spécifique */
    public void filtrerParMagazine(int magazineId) {
        this.magazineIdFiltre = magazineId;
        if (lblMagazineFiltre != null) lblMagazineFiltre.setVisible(true);
        pageCourante = 1;
        charger();
    }

    @FXML private void onSearchChanged() { pageCourante = 1; charger(); }
    @FXML private void onFiltreChanged() { pageCourante = 1; charger(); }
    @FXML private void onTriChanged()    { pageCourante = 1; charger(); }
    @FXML private void pagePrecedente()  { if (pageCourante > 1)          { pageCourante--; charger(); } }
    @FXML private void pageSuivante()    { if (pageCourante < totalPages) { pageCourante++; charger(); } }

    /** Retour à la liste des magazines */
    @FXML
    private void retourMagazines() {
        if (contentArea == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/MagazinePatientView.fxml"));
            javafx.scene.Node view = loader.load();
            MagazinePatientController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    private void charger() {
        String recherche   = tfSearch != null ? tfSearch.getText() : null;
        String publicCible = getPublicCibleSelectionne();
        String tri = (cbTri != null && "Plus populaires".equals(cbTri.getValue())) ? "populaire" : "recent";
        try {
            int total = serviceArticle.compterAvecFiltres(recherche, publicCible, magazineIdFiltre);
            totalPages = Math.max(1, (int) Math.ceil((double) total / ServiceArticle.PAGE_SIZE));
            if (pageCourante > totalPages) pageCourante = totalPages;
            if (lblPage != null) lblPage.setText("Page " + pageCourante + " / " + totalPages);
            if (btnPrev != null) btnPrev.setDisable(pageCourante <= 1);
            if (btnNext != null) btnNext.setDisable(pageCourante >= totalPages);
            obsArticles.setAll(serviceArticle.rechercherAvecFiltres(recherche, publicCible, tri, pageCourante, magazineIdFiltre));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    private String getPublicCibleSelectionne() {
        if (btnEnfants != null && btnEnfants.isSelected()) return "Enfants";
        if (btnJeunes  != null && btnJeunes.isSelected())  return "Jeunes";
        if (btnAdultes != null && btnAdultes.isSelected()) return "Adultes";
        return null;
    }

    private void afficherDetails(Article article) {
        try { serviceArticle.incrementerVue(article.getId()); article.setViews(article.getViews() + 1); if (listArticles != null) listArticles.refresh(); }
        catch (SQLException ignored) {}

        if (lblTitre       != null) lblTitre.setText(article.getTitre());
        if (lblAuteur      != null) lblAuteur.setText("✍ " + (article.getAuteur() != null ? article.getAuteur() : ""));
        if (lblDatePub     != null) lblDatePub.setText("📅 " + (article.getDatePub() != null ? article.getDatePub().toLocalDate() : "N/A"));
        if (lblViews       != null) lblViews.setText("👁 " + article.getViews() + " vues");
        if (lblStatut      != null) lblStatut.setText(article.getStatut() != null ? article.getStatut() : "");
        if (lblPublicCible != null) lblPublicCible.setText(article.getPublicCible() != null ? article.getPublicCible() : "Tous");
        if (taResume       != null) { taResume.setText(article.getResume() != null ? article.getResume() : ""); taResume.setVisible(true); taResume.setManaged(true); }

        // Afficher la ligne traduction
        if (tradRow != null) { tradRow.setVisible(true); tradRow.setManaged(true); }

        // Bouton résumé visible
        if (resumeButton != null) { resumeButton.setVisible(true); resumeButton.setManaged(true); }

        // Réinitialiser la zone résumé IA
        if (resumeArea  != null) resumeArea.setText("Cliquez sur 'Résumé automatique' pour générer...");
        if (resumeBox   != null) { resumeBox.setVisible(true); resumeBox.setManaged(true); }

        // Boutons traduction désactivés jusqu'au résumé
        for (Button b : new Button[]{btnTradAr, btnTradFr, btnTradEn}) {
            if (b != null) { b.setDisable(true); }
        }

        if (btnRecommandLinks != null) { btnRecommandLinks.setVisible(true); btnRecommandLinks.setManaged(true); }

        pdfCourant = article.getPdfFile();
        boolean hasPdf = pdfCourant != null && !pdfCourant.isBlank();
        if (btnOuvrirPdf != null) { btnOuvrirPdf.setVisible(hasPdf); btnOuvrirPdf.setManaged(hasPdf); }
    }

    // ====================== RÉSUMÉ IA ======================
    @FXML
    private void handleResume() {
        if (taResume == null || taResume.getText().trim().length() < 50) {
            new Alert(Alert.AlertType.WARNING, "Le texte doit contenir au moins 50 caractères.").showAndWait(); return;
        }
        String texte = taResume.getText().trim();
        if (resumeButton != null) resumeButton.setDisable(true);
        if (resumeArea   != null) resumeArea.setText("Génération en cours...");
        new Thread(() -> {
            try {
                String body = "{\"text\": " + toJsonString(texte) + ", \"nb_phrases\": 3}";
                HttpResponse<String> response = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder().uri(URI.create("http://localhost:8001/summarize"))
                                .header("Content-Type","application/json").timeout(Duration.ofSeconds(15))
                                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String summary = extractJsonValue(response.body(), "summary");
                    Platform.runLater(() -> {
                        if (resumeArea != null) resumeArea.setText(summary);
                        for (Button b : new Button[]{btnTradAr, btnTradFr, btnTradEn}) { if (b != null) b.setDisable(false); }
                    });
                } else {
                    Platform.runLater(() -> { if (resumeArea != null) resumeArea.setText("Erreur API (" + response.statusCode() + ")"); });
                }
            } catch (Exception e) {
                Platform.runLater(() -> { if (resumeArea != null) resumeArea.setText("API inaccessible : " + e.getMessage()); });
            } finally {
                Platform.runLater(() -> { if (resumeButton != null) resumeButton.setDisable(false); });
            }
        }).start();
    }

    // ====================== TRADUCTION ======================
    @FXML private void traduireArabe()    { traduire("ar"); }
    @FXML private void traduireFrancais() { traduire("fr"); }
    @FXML private void traduireAnglais()  { traduire("en"); }

    private void traduire(String targetLang) {
        if (resumeArea == null || resumeArea.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Générez d'abord un résumé.").showAndWait(); return;
        }
        String body = "{\"text\": " + toJsonString(resumeArea.getText().trim()) + ", \"target_lang\": \"" + targetLang + "\"}";
        for (Button b : new Button[]{btnTradAr, btnTradFr, btnTradEn}) { if (b != null) b.setDisable(true); }
        HttpClient.newHttpClient().sendAsync(
                HttpRequest.newBuilder().uri(URI.create("http://localhost:8001/translate"))
                        .header("Content-Type","application/json").timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())
            .thenAccept(r -> Platform.runLater(() -> {
                if (r.statusCode() == 200 && resumeArea != null) {
                    resumeArea.setText(extractJsonValue(r.body(), "translated_text"));
                    resumeArea.setNodeOrientation("ar".equals(targetLang) ?
                            javafx.geometry.NodeOrientation.RIGHT_TO_LEFT : javafx.geometry.NodeOrientation.LEFT_TO_RIGHT);
                }
            }))
            .whenComplete((r, ex) -> Platform.runLater(() -> {
                for (Button b : new Button[]{btnTradAr, btnTradFr, btnTradEn}) { if (b != null) b.setDisable(false); }
            }));
    }

    // ====================== RECOMMANDATION IA ======================
    @FXML
    private void recommanderLiens() {
        String contenu = (taResume != null) ? taResume.getText().trim() : "";
        if (contenu.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un article avec du contenu.").showAndWait();
            return;
        }
        if (btnRecommandLinks != null) btnRecommandLinks.setDisable(true);
        String body = "{\"content\": " + toJsonString(contenu) + "}";
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
            .sendAsync(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8001/recommend-links"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build(),
                HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> Platform.runLater(() -> {
                if (response.statusCode() == 200) afficherLiensDialog(response.body());
                else new Alert(Alert.AlertType.ERROR,
                        "Erreur IA (" + response.statusCode() + ")").showAndWait();
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                        "API inaccessible. Démarrez resume-api sur le port 8001.\nErreur : " + ex.getMessage()).showAndWait());
                return null;
            })
            .whenComplete((r, ex) -> Platform.runLater(() -> {
                if (btnRecommandLinks != null) btnRecommandLinks.setDisable(false);
            }));
    }

    private void afficherLiensDialog(String json) {
        java.util.List<String> links = new java.util.ArrayList<>();
        int start = json.indexOf("["), end = json.lastIndexOf("]");
        if (start >= 0 && end > start) {
            for (String part : json.substring(start + 1, end).split(",")) {
                String url = part.trim().replaceAll("^\"|\"$", "");
                if (!url.isEmpty()) links.add(url);
            }
        }
        if (links.isEmpty()) { new Alert(Alert.AlertType.INFORMATION, "Aucun lien retourné.").showAndWait(); return; }

        javafx.stage.Stage dlg = new javafx.stage.Stage();
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.setTitle("🔗 Liens recommandés par l'IA");

        javafx.scene.control.Label title = new javafx.scene.control.Label("Ressources complémentaires sur ce sujet");
        title.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f172a;");

        VBox linksBox = new VBox(10);
        for (String url : links) {
            javafx.scene.control.Hyperlink hl = new javafx.scene.control.Hyperlink(url);
            hl.setStyle("-fx-text-fill:#0ea5e9;-fx-font-size:12px;");
            hl.setWrapText(true);
            hl.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(URI.create(url));
                } catch (Exception ex) {
                    javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
                    cc.putString(url); cb.setContent(cc);
                    new Alert(Alert.AlertType.INFORMATION, "URL copiée dans le presse-papier.").showAndWait();
                }
            });
            linksBox.getChildren().add(hl);
        }

        Button btnClose = new Button("Fermer");
        btnClose.setStyle("-fx-background-color:#0f172a;-fx-text-fill:white;-fx-padding:8 20;-fx-background-radius:6;-fx-cursor:hand;");
        btnClose.setOnAction(e -> dlg.close());

        VBox root = new VBox(16, title, linksBox, btnClose);
        root.setPadding(new javafx.geometry.Insets(24));
        root.setStyle("-fx-background-color:white;");
        dlg.setScene(new javafx.scene.Scene(root, 540, 240));
        dlg.showAndWait();
    }
    @FXML
    private void ouvrirPdf() {
        if (pdfCourant == null || pdfCourant.isBlank()) return;
        try {
            File f = new File(pdfCourant);
            if (!f.isAbsolute()) f = new File(System.getProperty("user.dir"), pdfCourant);
            if (f.exists()) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Ouverture non supportée sur ce système.").showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "PDF introuvable : " + f.getAbsolutePath()).showAndWait();
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le PDF : " + e.getMessage()).showAndWait();
        }
    }

    // ====================== UTILS ======================
    private String toJsonString(String text) {
        return "\"" + text.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t") + "\"";
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search); if (idx < 0) return json;
        int colon = json.indexOf(":", idx + search.length()); if (colon < 0) return json;
        int start = json.indexOf("\"", colon + 1); if (start < 0) return json;
        int end = start + 1;
        while (end < json.length()) { if (json.charAt(end) == '"' && json.charAt(end-1) != '\\') break; end++; }
        return json.substring(start + 1, end).replace("\\n","\n").replace("\\\"","\"").replace("\\\\","\\");
    }
}
