package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Ordonnance;
import edu.connexion3a77.entities.Rapport;
import edu.connexion3a77.services.OrdonnanceService;
import edu.connexion3a77.services.RapportService;
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
import java.util.List;

public class DashboardController {

    // ==================== TABLEAUX ORDONNANCES ====================
    @FXML private TableView<Ordonnance> ordonnanceTable;
    @FXML private TableColumn<Ordonnance, Integer> ordIdCol;
    @FXML private TableColumn<Ordonnance, String> ordDateCol;
    @FXML private TableColumn<Ordonnance, String> ordDiagCol;
    @FXML private TableColumn<Ordonnance, String> ordMedCol;
    @FXML private TableColumn<Ordonnance, String> ordPosoCol;
    @FXML private TableColumn<Ordonnance, String> ordNotesCol;
    @FXML private TableColumn<Ordonnance, String> ordInstrCol;
    @FXML private TableColumn<Ordonnance, Void> ordActionsCol;

    // ==================== TABLEAUX RAPPORTS ====================
    @FXML private TableView<Rapport> rapportTable;
    @FXML private TableColumn<Rapport, Integer> rapIdCol;
    @FXML private TableColumn<Rapport, String> rapRaisonCol;
    @FXML private TableColumn<Rapport, String> rapDiagCol;
    @FXML private TableColumn<Rapport, String> rapObsCol;
    @FXML private TableColumn<Rapport, String> rapRecoCol;      // Ajouté si manquant
    @FXML private TableColumn<Rapport, String> rapTraitCol;     // Ajouté si manquant
    @FXML private TableColumn<Rapport, String> rapDateCol;      // Ajouté si manquant
    @FXML private TableColumn<Rapport, Void> rapActionsCol;

    // ==================== LABELS DE COMPTAGE DYNAMIQUES ====================
    @FXML private Label rapportCountLabel;
    @FXML private Label ordonnanceCountLabel;

    // ==================== CHAMPS DE RECHERCHE DYNAMIQUE ====================
    @FXML private TextField rapportSearchField;
    @FXML private TextField ordonnanceSearchField;

    private OrdonnanceService ordonnanceService = new OrdonnanceService();
    private RapportService rapportService = new RapportService();

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
                return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getDateOrdonnance()));
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
                return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getCreatedAt()));
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
                        if (rapport.getConsultationReason() != null && rapport.getConsultationReason().toLowerCase().contains(searchLower)) return true;
                        if (rapport.getDiagnosis() != null && rapport.getDiagnosis().toLowerCase().contains(searchLower)) return true;
                        if (rapport.getObservations() != null && rapport.getObservations().toLowerCase().contains(searchLower)) return true;
                        if (rapport.getRecommendations() != null && rapport.getRecommendations().toLowerCase().contains(searchLower)) return true;
                        if (rapport.getTreatments() != null && rapport.getTreatments().toLowerCase().contains(searchLower)) return true;

                        // Recherche par ID
                        if (String.valueOf(rapport.getId()).contains(searchLower)) return true;

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
                        if (ordonnance.getDiagnosis() != null && ordonnance.getDiagnosis().toLowerCase().contains(searchLower)) return true;
                        if (ordonnance.getMedicament() != null && ordonnance.getMedicament().toLowerCase().contains(searchLower)) return true;
                        if (ordonnance.getPosologie() != null && ordonnance.getPosologie().toLowerCase().contains(searchLower)) return true;
                        if (ordonnance.getNotes() != null && ordonnance.getNotes().toLowerCase().contains(searchLower)) return true;
                        if (ordonnance.getInstructions() != null && ordonnance.getInstructions().toLowerCase().contains(searchLower)) return true;

                        // Recherche par ID
                        if (String.valueOf(ordonnance.getId()).contains(searchLower)) return true;

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
            // ========== CHARGEMENT ORDONNANCES ==========
            List<Ordonnance> listOrdonnances = ordonnanceService.findAll();
            ordonnancesList = FXCollections.observableArrayList(listOrdonnances);
            filteredOrdonnances = new FilteredList<>(ordonnancesList, p -> true);
            ordonnanceTable.setItems(filteredOrdonnances);

            // Mise à jour du compteur d'ordonnances
            updateOrdonnanceCount();

            // ========== CHARGEMENT RAPPORTS ==========
            List<Rapport> listRapports = rapportService.findAll();
            rapportsList = FXCollections.observableArrayList(listRapports);
            filteredRapports = new FilteredList<>(rapportsList, p -> true);
            rapportTable.setItems(filteredRapports);

            // Mise à jour du compteur de rapports
            updateRapportCount();

            // ========== AJOUT DES LISTENERS POUR MISE À JOUR AUTOMATIQUE ==========
            setupListListeners();

            // Réappliquer le filtre si du texte est déjà saisi
            if (rapportSearchField != null && rapportSearchField.getText() != null && !rapportSearchField.getText().isEmpty()) {
                rapportSearchField.setText(rapportSearchField.getText());
            }
            if (ordonnanceSearchField != null && ordonnanceSearchField.getText() != null && !ordonnanceSearchField.getText().isEmpty()) {
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

    @FXML
    public void openAjouterOrdonnance(ActionEvent event) {
        openModal("src/main/resources/OrdonnanceView.fxml", "Nouvelle Ordonnance", null);
    }

    @FXML
    public void openAjouterRapport(ActionEvent event) {
        openModal("src/main/resources/RapportView.fxml", "Nouveau Rapport", null);
    }

    private void setupOrdonnanceActions() {
        ordActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnMod = new Button("Mod");
            private final Button btnSupp = new Button("Supp");
            private final HBox pane = new HBox(5, btnVoir, btnMod, btnSupp);

            {
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");
                btnMod.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px;");
                btnSupp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px;");

                // Taille fixe pour les boutons d'action
                btnVoir.setPrefWidth(50);
                btnMod.setPrefWidth(50);
                btnSupp.setPrefWidth(50);

                btnVoir.setOnAction(event -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    openModal("src/main/resources/OrdonnanceView.fxml", "Détails Ordonnance", o, true);
                });

                btnMod.setOnAction(event -> {
                    Ordonnance o = getTableView().getItems().get(getIndex());
                    openModal("src/main/resources/OrdonnanceMod.fxml", "Modifier Ordonnance", o, false);
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
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
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
            private final Button btnVoir = new Button("Voir");
            private final Button btnMod = new Button("Mod");
            private final Button btnSupp = new Button("Supp");
            private final HBox pane = new HBox(5, btnVoir, btnMod, btnSupp);

            {
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");
                btnMod.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px;");
                btnSupp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px;");

                // Taille fixe pour les boutons d'action
                btnVoir.setPrefWidth(50);
                btnMod.setPrefWidth(50);
                btnSupp.setPrefWidth(50);

                btnVoir.setOnAction(event -> {
                    Rapport r = getTableView().getItems().get(getIndex());
                    openModal("src/main/resources/RapportView.fxml", "Détails Rapport", r, true);
                });

                btnMod.setOnAction(event -> {
                    Rapport r = getTableView().getItems().get(getIndex());
                    openModal("src/main/resources/RapportMod.fxml", "Modifier Rapport", r, false);
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
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
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
     * @param readOnly si true, tous les champs sont désactivés et les boutons d'action cachés
     */
    private void openModal(String fxmlPath, String title, Object data, boolean readOnly) {
        try {
            File file = new File(fxmlPath);
            URL url = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Injection des données si on est en mode "Modifier" ou "Voir"
            if (data != null) {
                if (data instanceof Ordonnance) {
                    OrdonnanceController oc = loader.getController();
                    oc.initData((Ordonnance) data);
                } else if (data instanceof Rapport) {
                    RapportController rc = loader.getController();
                    rc.initData((Rapport) data);
                }
            }

            // Mode lecture seule : désactiver tous les champs de saisie et cacher les boutons d'action
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

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + fxmlPath);
        }
    }

    /**
     * Parcourt récursivement le graphe de scène et désactive tous les champs de saisie.
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