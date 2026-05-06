package com.pidev.controllers;

import com.pidev.models.Document;
import com.pidev.models.Rapport;
import com.pidev.models.Ordonnance;
import com.pidev.services.DocumentService;
import com.pidev.services.RapportService;
import com.pidev.services.OrdonnanceService;
import com.pidev.services.PdfService;
import com.pidev.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur du Dossier Médical - Version Standardisée
 * Utilise une TableView unique de documents pour correspondre au design premium.
 */
public class DossierMedicalController {

    @FXML private Label statsNumberLabel;
    @FXML private Label statsLabelLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Document> documentsTable;
    
    @FXML private TableColumn<Document, Integer> colId;
    @FXML private TableColumn<Document, String> colInfos;
    @FXML private TableColumn<Document, String> colType;
    @FXML private TableColumn<Document, String> colCreated;
    @FXML private TableColumn<Document, String> colUpdated;
    @FXML private TableColumn<Document, Void> colRapports;
    @FXML private TableColumn<Document, Void> colOrdonnances;
    @FXML private TableColumn<Document, Void> colActions;
    @FXML private TableColumn<Document, Void> colExport;
    
    @FXML private Button nouveauDocButton;
    @FXML private Label paginationMetaLabel;

    private final DocumentService documentService = new DocumentService();
    private final RapportService rapportService = new RapportService();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();
    private final PdfService pdfService = new PdfService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int currentIdPatient;
    private int currentIdMedecin;
    private int currentIdRendezVous;

    @FXML
    public void initialize() {
        setupTable();
        // Don't loadData here if we expect initData to be called
    }

    public void initData(int idpatient, int idmedecin, int idrendezvous) {
        this.currentIdPatient = idpatient;
        this.currentIdMedecin = idmedecin;
        this.currentIdRendezVous = idrendezvous;
        
        // Synchroniser avec la session globale pour que les modals (Rapports/Ordonnances) aient le contexte
        com.pidev.utils.UserSession.getInstance().setSelectedPatientId(idpatient);
        com.pidev.utils.UserSession.getInstance().setSelectedMedecinId(idmedecin);
        com.pidev.utils.UserSession.getInstance().setSelectedAppointmentId(idrendezvous);
        
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfos.setCellValueFactory(new PropertyValueFactory<>("nomFichier"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        colCreated.setCellValueFactory(cellData -> {
            var date = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        
        colUpdated.setCellValueFactory(cellData -> {
            var date = cellData.getValue().getUpdatedAt();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        setupRapportsActions();
        setupOrdonnancesActions();
        setupActions();
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁 Voir");
            private final Button btnMod = new Button("✏️ Mod");
            private final Button btnSupp = new Button("🗑 Supp");
            private final HBox pane = new HBox(8, btnVoir, btnMod, btnSupp);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                btnMod.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSupp.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

                btnVoir.setPrefWidth(70);
                btnMod.setPrefWidth(70);
                btnSupp.setPrefWidth(75);

                btnVoir.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) openDocumentModal(d, true);
                });

                btnMod.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) openDocumentModal(d, false);
                });

                btnSupp.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleDeleteDocument(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        colExport.setCellFactory(param -> new TableCell<>() {
            private final Button btnPdf = new Button("PDF");
            {
                btnPdf.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnPdf.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleExportPdf(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnPdf);
            }
        });
    }

    private void setupRapportsActions() {
        colRapports.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnMod = new Button("✏️");
            private final Button btnSupp = new Button("🗑");
            private final HBox pane = new HBox(4, btnVoir, btnMod, btnSupp);
            private final Label emptyLabel = new Label("Aucun");

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");
                btnMod.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");
                btnSupp.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");

                btnVoir.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleViewRapports(d);
                });

                btnMod.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleModifyRapports(d);
                });

                btnSupp.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleDeleteRapports(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null && d.getNbRapports() > 0) {
                        setGraphic(pane);
                    } else {
                        setGraphic(emptyLabel);
                    }
                }
            }
        });
    }

    private void setupOrdonnancesActions() {
        colOrdonnances.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnMod = new Button("✏️");
            private final Button btnSupp = new Button("🗑");
            private final HBox pane = new HBox(4, btnVoir, btnMod, btnSupp);
            private final Label emptyLabel = new Label("Aucun");

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");
                btnMod.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");
                btnSupp.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");

                btnVoir.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleViewOrdonnances(d);
                });

                btnMod.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleModifyOrdonnances(d);
                });

                btnSupp.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null) handleDeleteOrdonnances(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document d = getTableView().getItems().get(getIndex());
                    if (d != null && d.getNbOrdonnances() > 0) {
                        setGraphic(pane);
                    } else {
                        setGraphic(emptyLabel);
                    }
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Logic to filter the table
        });
    }

    private void loadData() {
        try {
            int patientId = (currentIdPatient > 0) ? currentIdPatient : (UserSession.getInstance().getSelectedPatientId() != null ? UserSession.getInstance().getSelectedPatientId() : 0);
            List<Document> list;
            if (patientId > 0) {
                list = documentService.findByPatient(patientId);
            } else {
                list = documentService.findAll();
            }
            documentsTable.setItems(FXCollections.observableArrayList(list));
            statsNumberLabel.setText(String.valueOf(list.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNouveauDocument(ActionEvent event) {
        openDocumentModal(null, false);
    }

    private void openDocumentModal(Document d, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DocumentView.fxml"));
            Parent root = loader.load();
            DocumentController controller = loader.getController();
            if (d != null) {
                controller.initData(d, readOnly);
            }
            
            Stage stage = new Stage();
            stage.setTitle(d == null ? "Nouveau Document" : "Détails Document");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            // Reload data when the modal closes
            stage.setOnHidden(event -> {
                System.out.println("📄 Modal fermée, rechargement des documents...");
                loadData();
            });
            
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteDocument(Document d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le document ?");
        confirm.setContentText("Êtes-vous sûr ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    documentService.delete(d.getId());
                    loadData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleExportPdf(Document d) {
        try {
            javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
            directoryChooser.setTitle("Sélectionner le dossier d'exportation");
            java.io.File selectedDirectory = directoryChooser.showDialog(documentsTable.getScene().getWindow());
            
            if (selectedDirectory == null) return;
            
            String fileName = "Dossier_Medical_" + d.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String fullPath = new java.io.File(selectedDirectory, fileName).getAbsolutePath();
            
            // Récupérer les données liées
            List<Rapport> rapports = rapportService.getByDocumentId(d.getId());
            List<Ordonnance> ordonnances = ordonnanceService.getByDocumentId(d.getId());
            
            com.pidev.services.PdfService pdfService = new com.pidev.services.PdfService();
            pdfService.generateMedicalDossier(d, rapports, ordonnances, fullPath);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportation PDF");
            alert.setHeaderText("Exportation réussie !");
            alert.setContentText("Le dossier médical a été généré avec succès dans :\n" + fullPath);
            alert.showAndWait();
            
            // Ouvrir le fichier automatiquement si possible
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(fullPath));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'exportation");
            alert.setHeaderText("Impossible de générer le PDF");
            alert.setContentText("Détail : " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ==================== GESTION DES RAPPORTS ====================
    
    private void handleViewRapports(Document d) {
        try {
            List<Rapport> rapports = rapportService.getByDocumentId(d.getId());
            if (rapports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucun rapport", "Ce document ne contient aucun rapport.");
                return;
            }
            
            if (rapports.size() == 1) {
                // Un seul rapport : l'ouvrir directement
                openRapportModal(rapports.get(0), true);
            } else {
                // Plusieurs rapports : afficher une liste de sélection
                Rapport selected = showRapportSelectionDialog(rapports, "Sélectionner un rapport à voir");
                if (selected != null) {
                    openRapportModal(selected, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les rapports: " + e.getMessage());
        }
    }
    
    private void handleModifyRapports(Document d) {
        try {
            List<Rapport> rapports = rapportService.getByDocumentId(d.getId());
            if (rapports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucun rapport", "Ce document ne contient aucun rapport.");
                return;
            }
            
            if (rapports.size() == 1) {
                // Un seul rapport : l'ouvrir directement
                openRapportModal(rapports.get(0), false);
            } else {
                // Plusieurs rapports : afficher une liste de sélection
                Rapport selected = showRapportSelectionDialog(rapports, "Sélectionner un rapport à modifier");
                if (selected != null) {
                    openRapportModal(selected, false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les rapports: " + e.getMessage());
        }
    }
    
    private void handleDeleteRapports(Document d) {
        try {
            List<Rapport> rapports = rapportService.getByDocumentId(d.getId());
            if (rapports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucun rapport", "Ce document ne contient aucun rapport.");
                return;
            }
            
            if (rapports.size() == 1) {
                // Un seul rapport : demander confirmation
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Supprimer le rapport ?");
                confirm.setContentText("Voulez-vous supprimer ce rapport ?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            rapportService.delete(rapports.get(0).getId());
                            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport supprimé.");
                            loadData();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                        }
                    }
                });
            } else {
                // Plusieurs rapports : afficher une liste de sélection
                Rapport selected = showRapportSelectionDialog(rapports, "Sélectionner un rapport à supprimer");
                if (selected != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Supprimer le rapport ?");
                    confirm.setContentText("Voulez-vous supprimer ce rapport ?\n\nMotif: " + selected.getConsultationReason());
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                rapportService.delete(selected.getId());
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport supprimé.");
                                loadData();
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les rapports: " + e.getMessage());
        }
    }
    
    private Rapport showRapportSelectionDialog(List<Rapport> rapports, String title) {
        Dialog<Rapport> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Ce document contient " + rapports.size() + " rapports. Sélectionnez-en un :");
        
        // Créer une ListView pour afficher les rapports
        ListView<Rapport> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(rapports));
        listView.setPrefHeight(300);
        listView.setPrefWidth(600);
        
        // Personnaliser l'affichage de chaque rapport
        listView.setCellFactory(param -> new ListCell<Rapport>() {
            @Override
            protected void updateItem(Rapport rapport, boolean empty) {
                super.updateItem(rapport, empty);
                if (empty || rapport == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String dateStr = rapport.getCreatedAt() != null ? 
                        rapport.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
                    setText(String.format("📋 ID: %d | Motif: %s | Diagnostic: %s | Date: %s",
                        rapport.getId(),
                        rapport.getConsultationReason() != null ? rapport.getConsultationReason() : "N/A",
                        rapport.getDiagnosis() != null ? rapport.getDiagnosis() : "N/A",
                        dateStr));
                    setStyle("-fx-padding: 10; -fx-font-size: 13px;");
                }
            }
        });
        
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Désactiver le bouton OK si aucune sélection
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null);
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }
    
    // ==================== GESTION DES ORDONNANCES ====================
    
    private void handleViewOrdonnances(Document d) {
        try {
            List<Ordonnance> ordonnances = ordonnanceService.getByDocumentId(d.getId());
            if (ordonnances.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucune ordonnance", "Ce document ne contient aucune ordonnance.");
                return;
            }
            
            if (ordonnances.size() == 1) {
                // Une seule ordonnance : l'ouvrir directement
                openOrdonnanceModal(ordonnances.get(0), true);
            } else {
                // Plusieurs ordonnances : afficher une liste de sélection
                Ordonnance selected = showOrdonnanceSelectionDialog(ordonnances, "Sélectionner une ordonnance à voir");
                if (selected != null) {
                    openOrdonnanceModal(selected, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ordonnances: " + e.getMessage());
        }
    }
    
    private void handleModifyOrdonnances(Document d) {
        try {
            List<Ordonnance> ordonnances = ordonnanceService.getByDocumentId(d.getId());
            if (ordonnances.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucune ordonnance", "Ce document ne contient aucune ordonnance.");
                return;
            }
            
            if (ordonnances.size() == 1) {
                // Une seule ordonnance : l'ouvrir directement
                openOrdonnanceModal(ordonnances.get(0), false);
            } else {
                // Plusieurs ordonnances : afficher une liste de sélection
                Ordonnance selected = showOrdonnanceSelectionDialog(ordonnances, "Sélectionner une ordonnance à modifier");
                if (selected != null) {
                    openOrdonnanceModal(selected, false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ordonnances: " + e.getMessage());
        }
    }
    
    private void handleDeleteOrdonnances(Document d) {
        try {
            List<Ordonnance> ordonnances = ordonnanceService.getByDocumentId(d.getId());
            if (ordonnances.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Aucune ordonnance", "Ce document ne contient aucune ordonnance.");
                return;
            }
            
            if (ordonnances.size() == 1) {
                // Une seule ordonnance : demander confirmation
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Supprimer l'ordonnance ?");
                confirm.setContentText("Voulez-vous supprimer cette ordonnance ?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            ordonnanceService.delete(ordonnances.get(0).getId());
                            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance supprimée.");
                            loadData();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                        }
                    }
                });
            } else {
                // Plusieurs ordonnances : afficher une liste de sélection
                Ordonnance selected = showOrdonnanceSelectionDialog(ordonnances, "Sélectionner une ordonnance à supprimer");
                if (selected != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Supprimer l'ordonnance ?");
                    confirm.setContentText("Voulez-vous supprimer cette ordonnance ?\n\nMédicament: " + selected.getMedicament());
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                ordonnanceService.delete(selected.getId());
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance supprimée.");
                                loadData();
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ordonnances: " + e.getMessage());
        }
    }
    
    private Ordonnance showOrdonnanceSelectionDialog(List<Ordonnance> ordonnances, String title) {
        Dialog<Ordonnance> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Ce document contient " + ordonnances.size() + " ordonnances. Sélectionnez-en une :");
        
        // Créer une ListView pour afficher les ordonnances
        ListView<Ordonnance> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(ordonnances));
        listView.setPrefHeight(300);
        listView.setPrefWidth(600);
        
        // Personnaliser l'affichage de chaque ordonnance
        listView.setCellFactory(param -> new ListCell<Ordonnance>() {
            @Override
            protected void updateItem(Ordonnance ordonnance, boolean empty) {
                super.updateItem(ordonnance, empty);
                if (empty || ordonnance == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String dateStr = ordonnance.getDateOrdonnance() != null ? 
                        ordonnance.getDateOrdonnance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
                    setText(String.format("💊 ID: %d | Médicament: %s | Diagnostic: %s | Date: %s",
                        ordonnance.getId(),
                        ordonnance.getMedicament() != null ? ordonnance.getMedicament() : "N/A",
                        ordonnance.getDiagnosis() != null ? ordonnance.getDiagnosis() : "N/A",
                        dateStr));
                    setStyle("-fx-padding: 10; -fx-font-size: 13px;");
                }
            }
        });
        
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Désactiver le bouton OK si aucune sélection
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null);
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }
    
    // ==================== MODALS ====================
    
    private void openRapportModal(Rapport r, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RapportView.fxml"));
            Parent root = loader.load();
            RapportController controller = loader.getController();
            controller.initData(r);
            
            if (readOnly) {
                setReadOnly(root);
            }
            
            Stage stage = new Stage();
            stage.setTitle(readOnly ? "Détails Rapport" : "Modifier Rapport");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHidden(event -> loadData());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le rapport: " + e.getMessage());
        }
    }
    
    private void openOrdonnanceModal(Ordonnance o, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OrdonnanceView.fxml"));
            Parent root = loader.load();
            OrdonnanceController controller = loader.getController();
            controller.initData(o);
            
            if (readOnly) {
                setReadOnly(root);
            }
            
            Stage stage = new Stage();
            stage.setTitle(readOnly ? "Détails Ordonnance" : "Modifier Ordonnance");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHidden(event -> loadData());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'ordonnance: " + e.getMessage());
        }
    }
    
    private void setReadOnly(javafx.scene.Node node) {
        if (node instanceof TextField) {
            ((TextField) node).setEditable(false);
            node.setFocusTraversable(false);
            node.setMouseTransparent(true);
            node.setStyle(node.getStyle() + " -fx-opacity: 0.9; -fx-background-color: #f1f5f9;");
        } else if (node instanceof TextArea) {
            ((TextArea) node).setEditable(false);
            node.setFocusTraversable(false);
            node.setMouseTransparent(true);
            node.setStyle(node.getStyle() + " -fx-opacity: 0.9; -fx-background-color: #f1f5f9;");
        } else if (node instanceof DatePicker) {
            ((DatePicker) node).setDisable(true);
            node.setStyle(node.getStyle() + " -fx-opacity: 0.9;");
        } else if (node instanceof ComboBox) {
            ((ComboBox<?>) node).setDisable(true);
            node.setStyle(node.getStyle() + " -fx-opacity: 0.9;");
        }
        
        String style = node.getStyle();
        if (style != null && style.contains("-fx-border-width: 2 0 0 0")) {
            node.setVisible(false);
            node.setManaged(false);
            return;
        }
        
        if (node instanceof Button) {
            node.setVisible(false);
            node.setManaged(false);
        }
        
        if (node instanceof ScrollPane) {
            javafx.scene.Node content = ((ScrollPane) node).getContent();
            if (content != null) {
                setReadOnly(content);
            }
        }
        
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                setReadOnly(child);
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
