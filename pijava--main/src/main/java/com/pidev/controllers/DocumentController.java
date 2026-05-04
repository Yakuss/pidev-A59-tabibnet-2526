package com.pidev.controllers;

import com.pidev.models.Document;
import com.pidev.models.Ordonnance;
import com.pidev.models.Rapport;
import com.pidev.services.DocumentService;
import com.pidev.services.OrdonnanceService;
import com.pidev.services.RapportService;
import com.pidev.utils.DataSource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    @FXML
    private TextField pathField;

    private Document currentDocument = null;
    private java.io.File selectedFile = null;
    private List<Rapport> rapportsToLink;
    private List<Ordonnance> ordonnancesToLink;

    private final DocumentService documentService = new DocumentService();
    private final RapportService rapportService = new RapportService();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();

    @FXML
    public void initialize() {
    }

    public void setItemsToLink(List<Rapport> rapports, List<Ordonnance> ords) {
        this.rapportsToLink = new ArrayList<>(rapports);
        this.ordonnancesToLink = new ArrayList<>(ords);
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
                if (actionBox != null) {
                    actionBox.setVisible(false);
                    actionBox.setManaged(false);
                }
                if (formCard != null) {
                    formCard.setMouseTransparent(true);
                    formCard.setFocusTraversable(false);
                }
            } else {
                titleLabel.setText("Modifier document");
                badgeLabel.setText("MODIFICATION DOCUMENT");
                statusLabel.setText("Modification en cours...");
                if (actionBox != null) {
                    actionBox.setVisible(true);
                    actionBox.setManaged(true);
                }
                if (formCard != null) {
                    formCard.setMouseTransparent(false);
                    formCard.setFocusTraversable(true);
                }
            }
        }
    }

    @FXML
    void handleImportFile(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir un document");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Documents", "*.pdf", "*.jpg", "*.png", "*.docx"),
            new javafx.stage.FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        java.io.File file = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (file != null) {
            this.selectedFile = file;
            pathField.setText(file.getAbsolutePath());
            
            // Auto-remplissage suggéré
            if (nomField.getText().isEmpty()) {
                nomField.setText(file.getName());
            }
            
            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                typeField.setText(name.substring(dotIndex + 1).toUpperCase());
            }
            
            tailleField.setText(String.valueOf(file.length() / 1024));
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
            
            if (selectedFile != null) {
                d.setCheminFichier(selectedFile.getAbsolutePath());
            }

            // Auto-populate context IDs from Session
            Integer medId = com.pidev.utils.UserSession.getInstance().getSelectedMedecinId();
            Integer patId = com.pidev.utils.UserSession.getInstance().getSelectedPatientId();
            
            // Fallback: try to get patientId from linked items
            if (patId == null || patId == 0) {
                if (rapportsToLink != null && !rapportsToLink.isEmpty()) {
                    patId = rapportsToLink.get(0).getPatientId();
                } else if (ordonnancesToLink != null && !ordonnancesToLink.isEmpty()) {
                    patId = ordonnancesToLink.get(0).getPatientId();
                }
            }

            if (patId == null || patId == 0) {
                showAlert("Erreur de contexte", "Impossible d'identifier le patient. Veuillez d'abord sélectionner un patient ou un rapport.");
                return;
            }

            d.setMedecinId(medId != null ? medId : 0);
            d.setPatientId(patId);

            if (currentDocument == null) {
                documentService.add(d);
                System.out.println("Document créé avec ID: " + d.getId());

                int linkedRapports = 0;
                int linkedOrdonnances = 0;

                if (rapportsToLink != null) {
                    for (var r : rapportsToLink) {
                        if (linkItemToDocument("rapport", r.getId(), d.getId())) {
                            linkedRapports++;
                        }
                    }
                }
                if (ordonnancesToLink != null) {
                    for (var o : ordonnancesToLink) {
                        if (linkItemToDocument("ordonnances", o.getId(), d.getId())) {
                            linkedOrdonnances++;
                        }
                    }
                }

                d.setNbRapports(linkedRapports);
                d.setNbOrdonnances(linkedOrdonnances);
                documentService.update(d);

                System.out.println("Finalisation: " + linkedRapports + " rapports et " + linkedOrdonnances + " ordonnances liés.");
            } else {
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
        try (var pst = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pst.setInt(1, documentId);
            pst.setInt(2, id);
            int affected = pst.executeUpdate();
            return affected > 0;
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
