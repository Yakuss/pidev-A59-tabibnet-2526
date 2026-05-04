package com.pidev.controllers;

import com.pidev.models.Ordonnance;
import com.pidev.models.Rapport;
import com.pidev.models.Document;
import com.pidev.services.OrdonnanceService;
import com.pidev.services.RapportService;
import com.pidev.controllers.DossierMedicalController;
import com.pidev.controllers.DocumentController;
import com.pidev.controllers.OrdonnanceController;
import com.pidev.controllers.RapportController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    // ==================== TABLEAUX ORDONNANCES ====================
    @FXML
    private TableView<Ordonnance> ordonnanceTable;
    @FXML
    private TableColumn<Ordonnance, Integer> ordIdCol;
    @FXML
    private TableColumn<Ordonnance, String> ordDateCol;
    @FXML
    private TableColumn<Ordonnance, String> ordDiagCol;
    @FXML
    private TableColumn<Ordonnance, String> ordMedCol;
    @FXML
    private TableColumn<Ordonnance, String> ordPosoCol;
    @FXML
    private TableColumn<Ordonnance, String> ordNotesCol;
    @FXML
    private TableColumn<Ordonnance, String> ordInstrCol;
    @FXML
    private TableColumn<Ordonnance, Void> ordActionsCol;

    // ==================== TABLEAUX RAPPORTS ====================
    @FXML
    private TableView<Rapport> rapportTable;
    @FXML
    private TableColumn<Rapport, Integer> rapIdCol;
    @FXML
    private TableColumn<Rapport, String> rapRaisonCol;
    @FXML
    private TableColumn<Rapport, String> rapDiagCol;
    @FXML
    private TableColumn<Rapport, String> rapObsCol;
    @FXML
    private TableColumn<Rapport, String> rapRecoCol; // Ajouté si manquant
    @FXML
    private TableColumn<Rapport, String> rapTraitCol; // Ajouté si manquant
    @FXML
    private TableColumn<Rapport, String> rapDateCol; // Ajouté si manquant
    @FXML
    private TableColumn<Rapport, Void> rapActionsCol;

    // ==================== LABELS DE COMPTAGE DYNAMIQUES ====================
    @FXML
    private Label rapportCountLabel;
    @FXML
    private Label ordonnanceCountLabel;

    // ==================== CHAMPS DE RECHERCHE DYNAMIQUE ====================
    @FXML
    private TextField rapportSearchField;
    @FXML
    private TextField ordonnanceSearchField;

    // ==================== BOUTON CRÉER DOCUMENT ====================
    @FXML
    private Button btnCreerDocument;

    // ==================== LABEL INFO PATIENT ====================
    @FXML
    private Label patientInfoLabel;

    private OrdonnanceService ordonnanceService = new OrdonnanceService();
    private RapportService rapportService = new RapportService();
    private com.pidev.services.DocumentService documentService = new com.pidev.services.DocumentService();

    // Listes observables pour le binding automatique
    private ObservableList<Ordonnance> ordonnancesList;
    private ObservableList<Rapport> rapportsList;

    // Listes filtrées pour la recherche dynamique
    private FilteredList<Rapport> filteredRapports;
    private FilteredList<Ordonnance> filteredOrdonnances;

    @FXML
    public void initialize() {
        // Configuration Colonnes Ordonnance
        ordIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        ordDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateOrdonnance() != null) {
                return new SimpleStringProperty(cellData.getValue().getDateOrdonnance().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("");
        });
        ordDiagCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        ordMedCol.setCellValueFactory(new PropertyValueFactory<>("medicament"));
        ordPosoCol.setCellValueFactory(new PropertyValueFactory<>("posologie"));
        ordNotesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        ordInstrCol.setCellValueFactory(new PropertyValueFactory<>("instructions"));
        setupOrdonnanceActions();

        // Configuration Colonnes Rapport
        rapIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        rapRaisonCol.setCellValueFactory(new PropertyValueFactory<>("consultationReason"));
        rapDiagCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        rapObsCol.setCellValueFactory(new PropertyValueFactory<>("observations"));

        // Configuration des colonnes supplémentaires
        rapRecoCol.setCellValueFactory(new PropertyValueFactory<>("recommendations"));
        rapTraitCol.setCellValueFactory(new PropertyValueFactory<>("treatments"));
        rapDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("");
        });

        setupRapportActions();

        // Configuration des listeners de recherche dynamique
        setupSearchListeners();

        // Charger les données initiales
        refreshData();
    }

    /**
     * Configure les listeners de recherche dynamique pour les deux tableaux.
     * Filtre en temps réel dès que l'utilisateur tape du texte.
     */
    private void setupSearchListeners() {
        // ========== RECHERCHE DYNAMIQUE RAPPORTS ==========
        if (rapportSearchField != null) {
            rapportSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (filteredRapports != null) {
                    filteredRapports.setPredicate(rapport -> {
                        // Si le champ de recherche est vide, afficher tout
                        if (newVal == null || newVal.trim().isEmpty()) {
                            return true;
                        }
                        String searchLower = newVal.toLowerCase().trim();

                        // Recherche dans tous les champs textuels du rapport
                        if (rapport.getConsultationReason() != null
                                && rapport.getConsultationReason().toLowerCase().contains(searchLower))
                            return true;
                        if (rapport.getDiagnosis() != null
                                && rapport.getDiagnosis().toLowerCase().contains(searchLower))
                            return true;
                        if (rapport.getObservations() != null
                                && rapport.getObservations().toLowerCase().contains(searchLower))
                            return true;
                        if (rapport.getRecommendations() != null
                                && rapport.getRecommendations().toLowerCase().contains(searchLower))
                            return true;
                        if (rapport.getTreatments() != null
                                && rapport.getTreatments().toLowerCase().contains(searchLower))
                            return true;

                        // Recherche par ID
                        if (String.valueOf(rapport.getId()).contains(searchLower))
                            return true;

                        return false;
                    });
                    updateRapportCount();
                }
            });
        }

        // ========== RECHERCHE DYNAMIQUE ORDONNANCES ==========
        if (ordonnanceSearchField != null) {
            ordonnanceSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (filteredOrdonnances != null) {
                    filteredOrdonnances.setPredicate(ordonnance -> {
                        // Si le champ de recherche est vide, afficher tout
                        if (newVal == null || newVal.trim().isEmpty()) {
                            return true;
                        }
                        String searchLower = newVal.toLowerCase().trim();

                        // Recherche dans tous les champs textuels de l'ordonnance
                        if (ordonnance.getDiagnosis() != null
                                && ordonnance.getDiagnosis().toLowerCase().contains(searchLower))
                            return true;
                        if (ordonnance.getMedicament() != null
                                && ordonnance.getMedicament().toLowerCase().contains(searchLower))
                            return true;
                        if (ordonnance.getPosologie() != null
                                && ordonnance.getPosologie().toLowerCase().contains(searchLower))
                            return true;
                        if (ordonnance.getNotes() != null && ordonnance.getNotes().toLowerCase().contains(searchLower))
                            return true;
                        if (ordonnance.getInstructions() != null
                                && ordonnance.getInstructions().toLowerCase().contains(searchLower))
                            return true;

                        // Recherche par ID
                        if (String.valueOf(ordonnance.getId()).contains(searchLower))
                            return true;

                        return false;
                    });
                    updateOrdonnanceCount();
                }
            });
        }
    }

    @FXML
    public void refreshData() {
        try {
            // Check if an appointment is selected in session
            com.pidev.models.Appointment selectedAppointment = 
                com.pidev.utils.AppointmentSessionManager.getInstance().getSelectedAppointment();
            
            Integer patientId = null;
            Integer medecinId = null;
            
            if (selectedAppointment != null) {
                // Use appointment data
                patientId = selectedAppointment.getPatientId();
                medecinId = selectedAppointment.getDoctorId();
                System.out.println("📋 Filtrage par rendez-vous sélectionné:");
                System.out.println("   Patient ID: " + patientId);
                System.out.println("   Médecin ID: " + medecinId);
            } else {
                System.out.println("ℹ️ Aucun rendez-vous sélectionné - tables vides");
            }

            // ========== CHARGEMENT ORDONNANCES ==========
            List<Ordonnance> listOrdonnances;
            if (patientId != null && medecinId != null) {
                // Load ordonnances for this patient and doctor (including those linked to documents)
                listOrdonnances = ordonnanceService.getByPatientAndDoctor(patientId, medecinId);
                System.out.println("✅ Ordonnances trouvées: " + listOrdonnances.size());
            } else {
                // No appointment selected - show empty tables
                listOrdonnances = new ArrayList<>();
            }
            ordonnancesList = FXCollections.observableArrayList(listOrdonnances);
            filteredOrdonnances = new FilteredList<>(ordonnancesList, p -> true);
            ordonnanceTable.setItems(filteredOrdonnances);

            // Mise à jour du compteur d'ordonnances
            updateOrdonnanceCount();

            // ========== CHARGEMENT RAPPORTS ==========
            List<Rapport> listRapports;
            if (patientId != null && medecinId != null) {
                // Load rapports for this patient and doctor (including those linked to documents)
                listRapports = rapportService.getByPatientAndDoctor(patientId, medecinId);
                System.out.println("✅ Rapports trouvés: " + listRapports.size());
            } else {
                // No appointment selected - show empty tables
                listRapports = new ArrayList<>();
            }
            rapportsList = FXCollections.observableArrayList(listRapports);
            filteredRapports = new FilteredList<>(rapportsList, p -> true);
            rapportTable.setItems(filteredRapports);

            // Mise à jour du compteur de rapports
            updateRapportCount();

            // ========== GESTION VISIBILITÉ BOUTON CRÉER DOCUMENT ==========
            updateCreateDocumentButtonVisibility(patientId, medecinId);

            // ========== MISE À JOUR INFO PATIENT DANS HEADER ==========
            updatePatientInfoLabel(selectedAppointment);

            // ========== AJOUT DES LISTENERS POUR MISE À JOUR AUTOMATIQUE ==========
            setupListListeners();

            // Réappliquer le filtre si du texte est déjà saisi
            if (rapportSearchField != null && rapportSearchField.getText() != null
                    && !rapportSearchField.getText().isEmpty()) {
                rapportSearchField.setText(rapportSearchField.getText());
            }
            if (ordonnanceSearchField != null && ordonnanceSearchField.getText() != null
                    && !ordonnanceSearchField.getText().isEmpty()) {
                ordonnanceSearchField.setText(ordonnanceSearchField.getText());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de charger les données: " + e.getMessage());
        }
    }

    /**
     * Configure les listeners pour mettre à jour les compteurs automatiquement
     * quand des éléments sont ajoutés ou supprimés des tableaux
     */
    private void setupListListeners() {
        // Listener pour les ordonnances
        if (ordonnancesList != null) {
            ordonnancesList.addListener((ListChangeListener<Ordonnance>) change -> {
                while (change.next()) {
                    updateOrdonnanceCount();
                }
            });
        }

        // Listener pour les rapports
        if (rapportsList != null) {
            rapportsList.addListener((ListChangeListener<Rapport>) change -> {
                while (change.next()) {
                    updateRapportCount();
                }
            });
        }
    }

    /**
     * Met à jour le label du compteur d'ordonnances
     * Affiche le nombre filtré / total. Gère le singulier/pluriel.
     */
    private void updateOrdonnanceCount() {
        int filteredCount = (filteredOrdonnances != null) ? filteredOrdonnances.size() : 0;
        int totalCount = (ordonnancesList != null) ? ordonnancesList.size() : 0;
        String text;
        if (filteredCount == totalCount) {
            text = totalCount + " " + ((totalCount <= 1) ? "ordonnance" : "ordonnances");
        } else {
            text = filteredCount + " / " + totalCount + " ordonnances";
        }

        if (ordonnanceCountLabel != null) {
            ordonnanceCountLabel.setText(text);
        }
    }

    /**
     * Met à jour le label du compteur de rapports
     * Affiche le nombre filtré / total. Gère le singulier/pluriel.
     */
    private void updateRapportCount() {
        int filteredCount = (filteredRapports != null) ? filteredRapports.size() : 0;
        int totalCount = (rapportsList != null) ? rapportsList.size() : 0;
        String text;
        if (filteredCount == totalCount) {
            text = totalCount + " " + ((totalCount <= 1) ? "rapport" : "rapports");
        } else {
            text = filteredCount + " / " + totalCount + " rapports";
        }

        if (rapportCountLabel != null) {
            rapportCountLabel.setText(text);
        }
    }

    /**
     * Met à jour la visibilité du bouton "Créer un document"
     * Cache le bouton si un document existe déjà pour ce patient + médecin
     * Affiche le bouton si aucun document n'existe
     */
    private void updateCreateDocumentButtonVisibility(Integer patientId, Integer medecinId) {
        if (btnCreerDocument == null) {
            return; // Bouton non défini dans le FXML
        }

        try {
            if (patientId != null && medecinId != null) {
                // Vérifier si un document existe pour ce patient + médecin
                boolean documentExists = documentService.documentExists(patientId, medecinId);
                
                if (documentExists) {
                    btnCreerDocument.setVisible(false);
                    btnCreerDocument.setManaged(false);
                    System.out.println("🔒 Bouton 'Créer un document' caché - document existe déjà");
                } else {
                    btnCreerDocument.setVisible(true);
                    btnCreerDocument.setManaged(true);
                    System.out.println("✅ Bouton 'Créer un document' affiché - aucun document");
                }
            } else {
                // Aucun rendez-vous sélectionné - cacher le bouton
                btnCreerDocument.setVisible(false);
                btnCreerDocument.setManaged(false);
                System.out.println("ℹ️ Bouton 'Créer un document' caché - aucun rendez-vous sélectionné");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la vérification du document: " + e.getMessage());
            // En cas d'erreur, afficher le bouton par défaut
            btnCreerDocument.setVisible(true);
            btnCreerDocument.setManaged(true);
        }
    }

    /**
     * Met à jour le label d'information patient dans le header
     * Affiche le nom et prénom du patient au lieu de "Rendez-vous #12345"
     */
    private void updatePatientInfoLabel(com.pidev.models.Appointment selectedAppointment) {
        if (patientInfoLabel == null) {
            return; // Label non défini dans le FXML
        }

        if (selectedAppointment != null) {
            String patientName = selectedAppointment.getPatientName();
            if (patientName != null && !patientName.isEmpty()) {
                patientInfoLabel.setText(patientName);
                System.out.println("👤 Affichage du patient: " + patientName);
            } else {
                patientInfoLabel.setText("Patient inconnu");
            }
        } else {
            patientInfoLabel.setText("Aucun rendez-vous sélectionné");
        }
    }

    @FXML
    public void openAjouterOrdonnance(ActionEvent event) {
        System.out.println("Dashboard: openAjouterOrdonnance clicked");
        openModal("/views/OrdonnanceView.fxml", "Nouvelle Ordonnance", null);
    }

    @FXML
    public void openAjouterRapport(ActionEvent event) {
        System.out.println("Dashboard: openAjouterRapport clicked");
        openModal("/views/RapportView.fxml", "Nouveau Rapport", null);
    }

    @FXML
    public void openAiAssistant(ActionEvent event) {
        System.out.println("Dashboard: openAiAssistant clicked");
        openModal("/views/AiAssistantView.fxml", "Assistant IA", null);
    }

    @FXML
    public void openDossierMedical(ActionEvent event) {
        System.out.println("Dashboard: openDossierMedical clicked");
        openModal("/views/DossierMedicalView.fxml", "Dossier Médical Complet", null);
    }

    @FXML
    public void openAjouterDocument(ActionEvent event) {
        openModal("/views/DocumentView.fxml", "Nouveau Document", null);
    }

    private void setupOrdonnanceActions() {
        ordActionsCol.setCellFactory(param -> new TableCell<>() {
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
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    if (o != null) openModal("/views/OrdonnanceView.fxml", "Détails Ordonnance", o, true);
                });

                btnMod.setOnAction(event -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    if (o != null) openModal("/views/OrdonnanceMod.fxml", "Modifier Ordonnance", o, false);
                });

                btnSupp.setOnAction(event -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Supprimer l'ordonnance ?");
                    confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette ordonnance ?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                ordonnanceService.delete(o.getId());
                                // Suppression de la liste observable (déclenche le listener)
                                ordonnancesList.remove(o);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur",
                                        "Impossible de supprimer: " + e.getMessage());
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupRapportActions() {
        rapActionsCol.setCellFactory(param -> new TableCell<>() {
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
                    Rapport r = getTableView().getItems().get(getIndex());
                    if (r != null) openModal("/views/RapportView.fxml", "Détails Rapport", r, true);
                });

                btnMod.setOnAction(event -> {
                    Rapport r = getTableView().getItems().get(getIndex());
                    if (r != null) openModal("/views/RapportMod.fxml", "Modifier Rapport", r, false);
                });

                btnSupp.setOnAction(event -> {
                    Rapport r = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Supprimer le rapport ?");
                    confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce rapport ?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                rapportService.delete(r.getId());
                                // Suppression de la liste observable (déclenche le listener)
                                rapportsList.remove(r);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur",
                                        "Impossible de supprimer: " + e.getMessage());
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Ouvre un modal sans mode lecture seule (pour ajouter/modifier)
     */
    private void openModal(String fxmlPath, String title, Object data) {
        openModal(fxmlPath, title, data, false);
    }

    /**
     * Ouvre un modal avec support du mode lecture seule
     * 
     * @param readOnly si true, tous les champs sont désactivés et les boutons
     *                 d'action cachés
     */
    private void openModal(String fxmlPath, String title, Object data, boolean readOnly) {
        try {
            System.out.println("Dashboard: Loading modal from " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Injection des données si on est en mode "Modifier" ou "Voir"
            if (loader.getController() instanceof DocumentController) {
                DocumentController dc = loader.getController();
                if (data != null) {
                    dc.initData((Document) data, readOnly);
                } else {
                    // Nouveau document : on passe les rapports/ordonnances orphelins du dashboard
                    System.out.println("Dashboard: Passing " + rapportsList.size() + " rapports and "
                            + ordonnancesList.size() + " ordonnances to DocumentController");
                    dc.setItemsToLink(rapportsList, ordonnancesList);
                }
            } else if (data != null) {
                if (data instanceof Ordonnance) {
                    OrdonnanceController oc = loader.getController();
                    oc.initData((Ordonnance) data);
                } else if (data instanceof Rapport) {
                    RapportController rc = loader.getController();
                    rc.initData((Rapport) data);
                }
            } else if (loader.getController() instanceof DossierMedicalController) {
                DossierMedicalController dmc = loader.getController();
                Integer patientId = com.pidev.utils.UserSession.getInstance().getSelectedPatientId();
                Integer medecinId = com.pidev.utils.UserSession.getInstance().getSelectedMedecinId();
                Integer rvId = com.pidev.utils.UserSession.getInstance().getSelectedAppointmentId();
                dmc.initData(patientId != null ? patientId : 0, 
                            medecinId != null ? medecinId : 0, 
                            rvId != null ? rvId : 0);
            }

            // Mode lecture seule : désactiver tous les champs de saisie et cacher les
            // boutons d'action
            if (readOnly) {
                setReadOnly(root);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchit les tableaux quand le popup se ferme
            refreshData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", 
                "Impossible de charger la vue: " + fxmlPath + "\nErreur: " + e.toString());
        }
    }

    /**
     * Parcourt récursivement le graphe de scène et désactive tous les champs de
     * saisie.
     * Cache la barre d'actions (Créer, Annuler, "Création en cours...").
     * Les labels et la structure visuelle restent intacts.
     */
    private void setReadOnly(javafx.scene.Node node) {
        // ── Désactiver les champs de saisie ──
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

        // ── Cacher entièrement la barre d'actions en bas ──
        String style = node.getStyle();
        if (style != null && style.contains("-fx-border-width: 2 0 0 0")) {
            node.setVisible(false);
            node.setManaged(false);
            return;
        }

        // ── Cacher les boutons isolés ──
        if (node instanceof Button) {
            node.setVisible(false);
            node.setManaged(false);
        }

        // ── ScrollPane : traverser son contenu explicitement ──
        if (node instanceof ScrollPane) {
            javafx.scene.Node content = ((ScrollPane) node).getContent();
            if (content != null) {
                setReadOnly(content);
            }
        }

        // ── Parcours récursif des enfants ──
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
