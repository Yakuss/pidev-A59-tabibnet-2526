package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Document;
import edu.connexion3a77.services.DocumentService;
import edu.connexion3a77.services.OrdonnanceService;
import edu.connexion3a77.services.RapportService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class DocumentController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label badgeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox actionBox;
    @FXML
    private AnchorPane formCard;
    @FXML
    private TextField nomField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField tailleField;
    @FXML
    private TextArea descriptionArea;

    private Document currentDocument = null;
    private java.util.List<edu.connexion3a77.entities.Rapport> rapportsToLink;
    private java.util.List<edu.connexion3a77.entities.Ordonnance> ordonnancesToLink;

    private final DocumentService documentService = new DocumentService();
    private final RapportService rapportService = new RapportService();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();

    @FXML
    public void initialize() {
    }

    public void setItemsToLink(java.util.List<edu.connexion3a77.entities.Rapport> rapports,
            java.util.List<edu.connexion3a77.entities.Ordonnance> ords) {
        this.rapportsToLink = new java.util.ArrayList<>(rapports);
        this.ordonnancesToLink = new java.util.ArrayList<>(ords);
    }

    public void initData(Document d, boolean readOnly) {
        this.currentDocument = d;
        if (d != null) {
            nomField.setText(d.getNomFichier());
            typeField.setText(d.getType());
            tailleField.setText(d.getTaille());
            descriptionArea.setText(d.getDescription());

            if (readOnly) {
                titleLabel.setText("Détails du document");
                badgeLabel.setText("CONSULTATION DOCUMENT");
                statusLabel.setText("Mode lecture seule");
                actionBox.setVisible(false);
                actionBox.setManaged(false);
                formCard.setMouseTransparent(true); // Bloque TOUTE interaction souris sur la carte
                formCard.setFocusTraversable(false);
            } else {
                titleLabel.setText("Modifier document");
                badgeLabel.setText("MODIFICATION DOCUMENT");
                statusLabel.setText("Modification en cours...");
                actionBox.setVisible(true);
                actionBox.setManaged(true);
                formCard.setMouseTransparent(false);
                formCard.setFocusTraversable(true);
            }
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        try {
            if (nomField.getText().isEmpty()) {
                showAlert("Erreur de saisie", "Le nom du document est obligatoire.");
                return;
            }

            Document d = (currentDocument != null) ? currentDocument : new Document();
            d.setNomFichier(nomField.getText());
            d.setType(typeField.getText());
            d.setTaille(tailleField.getText());
            d.setDescription(descriptionArea.getText());

            if (currentDocument == null) {
                // Enregistrer d'abord le document pour avoir son ID
                documentService.add(d);
                System.out.println("Document créé avec ID: " + d.getId());

                int linkedRapports = 0;
                int linkedOrdonnances = 0;

                // Lier les rapports spécifiques s'ils sont orphelins
                if (rapportsToLink != null) {
                    for (var r : rapportsToLink) {
                        if (linkItemToDocument("rapport", r.getId(), d.getId())) {
                            linkedRapports++;
                        }
                    }
                }
                // Lier les ordonnances spécifiques si elles sont orphelines
                if (ordonnancesToLink != null) {
                    for (var o : ordonnancesToLink) {
                        if (linkItemToDocument("ordonnances", o.getId(), d.getId())) {
                            linkedOrdonnances++;
                        }
                    }
                }

                // Mettre à jour le document avec les VRAIS nombres d'éléments liés
                d.setNbRapports(linkedRapports);
                d.setNbOrdonnances(linkedOrdonnances);
                documentService.update(d);

                System.out.println(
                        "Finalisation: " + linkedRapports + " rapports et " + linkedOrdonnances + " ordonnances liés.");
            } else {
                // Mise à jour
                documentService.update(d);
                System.out.println("Document modifié avec succès !");
            }

            closeWindow();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur Base de Données", "Impossible d'enregistrer le document.\nErreur : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur inattendue est survenue : " + e.getMessage());
        }
    }

    private boolean linkItemToDocument(String table, int id, int documentId) throws SQLException {
        String query = "UPDATE " + table + " SET document_id = ? WHERE id = ? AND document_id IS NULL";
        try (var pst = edu.connexion3a77.tools.MyConnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setInt(1, documentId);
            pst.setInt(2, id);
            int affected = pst.executeUpdate();
            if (affected > 0) {
                System.out.println("SQL: " + table + " " + id + " successfully linked to document " + documentId);
                return true;
            } else {
                System.out.println("SQL: " + table + " " + id + " was NOT linked (already has a document)");
                return false;
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
