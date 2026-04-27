package edu.connexion3a77.controllers.ArticleController;

import edu.connexion3a77.entities.Article;
import edu.connexion3a77.services.ServiceArticle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class articlePatient {

    // --- Toolbar ---
    @FXML private TextField tfSearch;
    @FXML private ToggleButton btnTous, btnEnfants, btnJeunes, btnAdultes;
    @FXML private ComboBox<String> cbTri;

    // --- Liste + pagination ---
    @FXML private ListView<Article> listArticles;
    @FXML private Button btnPrev, btnNext;
    @FXML private Label lblPage;

    // --- Détail ---
    @FXML private VBox detailPane;
    @FXML private Label lblTitre, lblAuteur, lblDatePub, lblViews, lblStatut, lblPublicCible;
    @FXML private TextArea taSummary, taResume;
    @FXML private Button btnOuvrirPdf;

    private final ServiceArticle serviceArticle = new ServiceArticle();
    private final ObservableList<Article> obsArticles = FXCollections.observableArrayList();

    private int pageCourante = 1;
    private int totalPages   = 1;
    private String pdfCourant = null;
    private Integer magazineIdFiltre = null; // filtre optionnel par magazine

    @FXML
    private void initialize() {
        try {
            if (cbTri != null) {
                cbTri.getItems().addAll("Plus récents", "Plus populaires");
                cbTri.setValue("Plus récents");
            }
            if (listArticles != null) {
                listArticles.setItems(obsArticles);
                listArticles.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(Article a, boolean empty) {
                        super.updateItem(a, empty);
                        if (empty || a == null) { setText(null); }
                        else {
                            setText("📄 " + a.getTitre() + "\n👁 " + a.getViews() + " vues  |  " +
                                    (a.getPublicCible() != null ? a.getPublicCible() : "Tous"));
                        }
                    }
                });
                listArticles.getSelectionModel().selectedItemProperty()
                        .addListener((obs, old, article) -> { if (article != null) afficherDetails(article); });
            }
            charger();
        } catch (Exception e) {
            System.err.println("[articlePatient] Erreur initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Appelé depuis PatientController pour pré-filtrer par magazine */
    public void filtrerParMagazine(int magazineId) {
        this.magazineIdFiltre = magazineId;
        pageCourante = 1;
        charger();
    }

    @FXML private void onSearchChanged() { pageCourante = 1; charger(); }
    @FXML private void onFiltreChanged() { pageCourante = 1; charger(); }
    @FXML private void onTriChanged()    { pageCourante = 1; charger(); }
    @FXML private void pagePrecedente()  { if (pageCourante > 1)          { pageCourante--; charger(); } }
    @FXML private void pageSuivante()    { if (pageCourante < totalPages) { pageCourante++; charger(); } }

    private void charger() {
        String recherche   = (tfSearch != null) ? tfSearch.getText() : null;
        String publicCible = getPublicCibleSelectionne();
        String tri = (cbTri != null && "Plus populaires".equals(cbTri.getValue())) ? "populaire" : "recent";

        try {
            int total = serviceArticle.compterAvecFiltres(recherche, publicCible, magazineIdFiltre);
            totalPages = Math.max(1, (int) Math.ceil((double) total / ServiceArticle.PAGE_SIZE));
            if (pageCourante > totalPages) pageCourante = totalPages;

            if (lblPage != null) lblPage.setText("Page " + pageCourante + " / " + totalPages);
            if (btnPrev != null) btnPrev.setDisable(pageCourante <= 1);
            if (btnNext != null) btnNext.setDisable(pageCourante >= totalPages);

            obsArticles.clear();
            obsArticles.addAll(serviceArticle.rechercherAvecFiltres(recherche, publicCible, tri, pageCourante, magazineIdFiltre));
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
        try {
            serviceArticle.incrementerVue(article.getId());
            article.setViews(article.getViews() + 1);
            if (listArticles != null) listArticles.refresh();
        } catch (SQLException ignored) {}

        if (lblTitre       != null) lblTitre.setText(article.getTitre());
        if (lblAuteur      != null) lblAuteur.setText("✍ " + (article.getAuteur() != null ? article.getAuteur() : ""));
        if (lblDatePub     != null) lblDatePub.setText("📅 " + (article.getDatePub() != null ? article.getDatePub().toLocalDate() : "N/A"));
        if (lblViews       != null) lblViews.setText("👁 " + article.getViews() + " vues");
        if (lblStatut      != null) {
            lblStatut.setText(article.getStatut() != null ? article.getStatut() : "");
            lblStatut.getStyleClass().removeAll("published", "draft", "archived");
            if (article.getStatut() != null) lblStatut.getStyleClass().add(article.getStatut());
        }
        if (lblPublicCible != null) {
            String pc = article.getPublicCible();
            lblPublicCible.setText(pc != null ? pc : "Tous");
            lblPublicCible.setStyle(badgeStyle(pc));
        }
        if (taSummary != null) taSummary.setText(article.getSummary() != null ? article.getSummary() : "");
        if (taResume  != null) taResume.setText(article.getResume()   != null ? article.getResume()   : "");

        pdfCourant = article.getPdfFile();
        boolean hasPdf = pdfCourant != null && !pdfCourant.isBlank();
        if (btnOuvrirPdf != null) { btnOuvrirPdf.setVisible(hasPdf); btnOuvrirPdf.setManaged(hasPdf); }
    }

    @FXML
    private void ouvrirPdf() {
        if (pdfCourant == null) return;
        try {
            File f = new File(pdfCourant);
            if (f.exists() && Desktop.isDesktopSupported()) Desktop.getDesktop().open(f);
            else new Alert(Alert.AlertType.ERROR, "PDF introuvable : " + pdfCourant).showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le PDF : " + e.getMessage()).showAndWait();
        }
    }

    private String badgeStyle(String pc) {
        if ("Enfants".equals(pc))  return "-fx-background-color:#bbf7d0;-fx-text-fill:#166534;-fx-background-radius:999;-fx-padding:3 12;-fx-font-size:12px;-fx-font-weight:bold;";
        if ("Jeunes".equals(pc))   return "-fx-background-color:#bfdbfe;-fx-text-fill:#1e40af;-fx-background-radius:999;-fx-padding:3 12;-fx-font-size:12px;-fx-font-weight:bold;";
        if ("Adultes".equals(pc))  return "-fx-background-color:#fde68a;-fx-text-fill:#92400e;-fx-background-radius:999;-fx-padding:3 12;-fx-font-size:12px;-fx-font-weight:bold;";
        return "-fx-background-color:#e2e8f0;-fx-text-fill:#475569;-fx-background-radius:999;-fx-padding:3 12;-fx-font-size:12px;";
    }
}
