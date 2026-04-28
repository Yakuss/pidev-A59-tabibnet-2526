package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Document;
import edu.connexion3a77.services.DocumentService;
import edu.connexion3a77.services.RapportService;
import edu.connexion3a77.services.OrdonnanceService;
import edu.connexion3a77.services.PdfService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    @FXML private TableColumn<Document, Integer> colRapports;
    @FXML private TableColumn<Document, Integer> colOrdonnances;
    @FXML private TableColumn<Document, Void> colActions;
    @FXML private TableColumn<Document, Void> colExport;
    
    private final DocumentService documentService = new DocumentService();
    private final RapportService rapportService = new RapportService();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();
    private final PdfService pdfService = new PdfService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInfos.setCellValueFactory(new PropertyValueFactory<>("nomFichier"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        // Formatage des dates
        colCreated.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDateCreation();
            return new javafx.beans.property.SimpleStringProperty(date != null ? dateFormat.format(date) : "");
        });
        
        colUpdated.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDateModification();
            return new javafx.beans.property.SimpleStringProperty(date != null ? dateFormat.format(date) : "");
        });

        colRapports.setCellValueFactory(new PropertyValueFactory<>("nbRapports"));
        colOrdonnances.setCellValueFactory(new PropertyValueFactory<>("nbOrdonnances"));
        
        setupRapportColumn();
        setupOrdonnanceColumn();
        setupActions();
        setupExportColumn();
    }

    private void setupRapportColumn() {
        colRapports.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnMod = new Button("Mod");
            private final Button btnSupp = new Button("Supp");
            private final HBox pane = new HBox(5, btnVoir, btnMod, btnSupp);

            {
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnMod.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnSupp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                
                btnVoir.setPrefWidth(50);
                btnMod.setPrefWidth(50);
                btnSupp.setPrefWidth(50);

                btnVoir.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleVoirRapports(d);
                });

                btnMod.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleModRapports(d);
                });

                btnSupp.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleSuppRapports(d);
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupOrdonnanceColumn() {
        colOrdonnances.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnMod = new Button("Mod");
            private final Button btnSupp = new Button("Supp");
            private final HBox pane = new HBox(5, btnVoir, btnMod, btnSupp);

            {
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnMod.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnSupp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                
                btnVoir.setPrefWidth(50);
                btnMod.setPrefWidth(50);
                btnSupp.setPrefWidth(50);

                btnVoir.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleVoirOrdonnances(d);
                });

                btnMod.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleModOrdonnances(d);
                });

                btnSupp.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleSuppOrdonnances(d);
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnMod = new Button("Mod");
            private final Button btnSupp = new Button("Supp");
            private final HBox pane = new HBox(5, btnVoir, btnMod, btnSupp);

            {
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnMod.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnSupp.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");

                btnVoir.setPrefWidth(50);
                btnMod.setPrefWidth(50);
                btnSupp.setPrefWidth(50);

                btnVoir.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    openModal("src/main/resources/DocumentView.fxml", "Détails Document", d, true);
                });

                btnMod.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    openModal("src/main/resources/DocumentView.fxml", "Modifier Document", d, false);
                });

                btnSupp.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleDeleteDocument(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupExportColumn() {
        colExport.setCellFactory(param -> new TableCell<>() {
            private final Button btnPdf = new Button("PDF");
            private final HBox pane = new HBox(btnPdf);
            {
                btnPdf.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnPdf.setPrefWidth(50);
                btnPdf.setOnAction(event -> {
                    Document d = getTableView().getItems().get(getIndex());
                    handleExportPdf(d);
                });
                pane.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private Button createStyledButton(String text, String icon, String bgColor, String textColor) {
        Button btn = new Button(icon + " " + text);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 11; -fx-padding: 6 12; -fx-background-radius: 8; -fx-border-color: %s33; -fx-border-radius: 8; -fx-cursor: hand;", bgColor, textColor, textColor));
        return btn;
    }

    private Button createActionButton(String text, String icon, String bgColor, String textColor) {
        Button btn = new Button(icon + " " + text);
        btn.setPrefWidth(110);
        btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btn.setStyle(String.format("-fx-background-color: white; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand;", textColor));
        
        btn.setOnMouseEntered(e -> btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-radius: 10; -fx-border-color: %s66; -fx-border-radius: 10; -fx-cursor: hand;", bgColor, textColor, textColor)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format("-fx-background-color: white; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand;", textColor)));
        
        return btn;
    }

    private void handleDeleteDocument(Document d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le document ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce document ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    documentService.delete(d.getId());
                    loadData();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    private void handleExportPdf(Document d) {
        try {
            var rapports = rapportService.findByDocumentId(d.getId());
            var ordonnances = ordonnanceService.findByDocumentId(d.getId());

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le Dossier Médical");
            fileChooser.setInitialFileName("Dossier_" + d.getNomFichier().replaceAll("\\s+", "_") + ".pdf");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            File file = fileChooser.showSaveDialog(documentsTable.getScene().getWindow());
            if (file != null) {
                pdfService.generateMedicalDossier(d, rapports, ordonnances, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le dossier médical a été exporté avec succès vers :\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'exportation", "Une erreur est survenue lors de l'exportation : " + e.getMessage());
        }
    }

    private void handleVoirRapports(Document d) {
        try {
            var rapports = rapportService.findByDocumentId(d.getId());
            if (rapports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun rapport trouvé pour ce document.");
                return;
            }
            showRapportsInModal(rapports, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSuppRapports(Document d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer les rapports ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer tous les rapports de ce document ?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    var list = rapportService.findByDocumentId(d.getId());
                    for (var r : list) rapportService.delete(r.getId());
                    d.setNbRapports(0);
                    documentService.update(d);
                    loadData();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void handleModRapports(Document d) {
        try {
            var rapports = rapportService.findByDocumentId(d.getId());
            if (rapports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun rapport trouvé pour ce document.");
                return;
            }
            showRapportsInModal(rapports, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleVoirOrdonnances(Document d) {
        try {
            var ords = ordonnanceService.findByDocumentId(d.getId());
            if (ords.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune ordonnance trouvée pour ce document.");
                return;
            }
            showOrdonnancesInModal(ords, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSuppOrdonnances(Document d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer les ordonnances ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer toutes les ordonnances de ce document ?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    var list = ordonnanceService.findByDocumentId(d.getId());
                    for (var o : list) ordonnanceService.delete(o.getId());
                    d.setNbOrdonnances(0);
                    documentService.update(d);
                    loadData();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void handleModOrdonnances(Document d) {
        try {
            var ords = ordonnanceService.findByDocumentId(d.getId());
            if (ords.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune ordonnance trouvée pour ce document.");
                return;
            }
            showOrdonnancesInModal(ords, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showRapportsInModal(java.util.List<edu.connexion3a77.entities.Rapport> rapports, boolean readOnly) {
        if (rapports.size() == 1) {
            openRapportModal(rapports.get(0), readOnly);
            return;
        }
        TabPane tabPane = new TabPane();
        for (var r : rapports) {
            try {
                String fxml = readOnly ? "src/main/resources/RapportView.fxml" : "src/main/resources/RapportMod.fxml";
                FXMLLoader loader = new FXMLLoader(new File(fxml).toURI().toURL());
                Parent root = loader.load();
                RapportController controller = loader.getController();
                controller.initData(r);
                if (readOnly) setReadOnly(root);
                Tab tab = new Tab("Rapport #" + r.getId(), root);
                tab.setClosable(false);
                tabPane.getTabs().add(tab);
            } catch (IOException e) { e.printStackTrace(); }
        }
        showStage(tabPane, readOnly ? "Détails des Rapports" : "Modifier les Rapports", 800, 600);
    }

    private void showOrdonnancesInModal(java.util.List<edu.connexion3a77.entities.Ordonnance> ords, boolean readOnly) {
        if (ords.size() == 1) {
            openOrdonnanceModal(ords.get(0), readOnly);
            return;
        }
        TabPane tabPane = new TabPane();
        for (var o : ords) {
            try {
                String fxml = readOnly ? "src/main/resources/OrdonnanceView.fxml" : "src/main/resources/OrdonnanceMod.fxml";
                FXMLLoader loader = new FXMLLoader(new File(fxml).toURI().toURL());
                Parent root = loader.load();
                OrdonnanceController controller = loader.getController();
                controller.initData(o);
                if (readOnly) setReadOnly(root);
                Tab tab = new Tab("Ordonnance #" + o.getId(), root);
                tab.setClosable(false);
                tabPane.getTabs().add(tab);
            } catch (IOException e) { e.printStackTrace(); }
        }
        showStage(tabPane, readOnly ? "Détails des Ordonnances" : "Modifier les Ordonnances", 900, 700);
    }

    private void openRapportModal(edu.connexion3a77.entities.Rapport r, boolean readOnly) {
        try {
            String fxml = readOnly ? "src/main/resources/RapportView.fxml" : "src/main/resources/RapportMod.fxml";
            FXMLLoader loader = new FXMLLoader(new File(fxml).toURI().toURL());
            Parent root = loader.load();
            RapportController controller = loader.getController();
            controller.initData(r);
            if (readOnly) setReadOnly(root);
            showStage(root, readOnly ? "Détails Rapport" : "Modifier Rapport", -1, -1);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openOrdonnanceModal(edu.connexion3a77.entities.Ordonnance o, boolean readOnly) {
        try {
            String fxml = readOnly ? "src/main/resources/OrdonnanceView.fxml" : "src/main/resources/OrdonnanceMod.fxml";
            FXMLLoader loader = new FXMLLoader(new File(fxml).toURI().toURL());
            Parent root = loader.load();
            OrdonnanceController controller = loader.getController();
            controller.initData(o);
            if (readOnly) setReadOnly(root);
            showStage(root, readOnly ? "Détails Ordonnance" : "Modifier Ordonnance", -1, -1);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showStage(Parent root, String title, double w, double h) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (w > 0 && h > 0) stage.setScene(new Scene(root, w, h));
        else stage.setScene(new Scene(root));
        stage.show();
    }

    private void openModal(String fxmlPath, String title, Document data, boolean readOnly) {
        try {
            File file = new File(fxmlPath);
            URL url = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            if (data != null) {
                DocumentController controller = loader.getController();
                controller.initData(data, readOnly);
            }

            if (readOnly) {
                setReadOnly(root);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + fxmlPath);
        }
    }

    private void setReadOnly(javafx.scene.Node node) {
        if (node instanceof TextField) {
            TextField tf = (TextField) node;
            tf.setEditable(false);
            tf.setMouseTransparent(true);
            tf.setFocusTraversable(false);
            tf.setDisable(true);
            tf.setStyle("-fx-opacity: 1.0; -fx-text-fill: #1e293b; -fx-background-color: #f1f5f9;");
        } else if (node instanceof TextArea) {
            TextArea ta = (TextArea) node;
            ta.setEditable(false);
            ta.setMouseTransparent(true);
            ta.setFocusTraversable(false);
            ta.setDisable(true);
            ta.setStyle("-fx-opacity: 1.0; -fx-text-fill: #1e293b; -fx-background-color: #f1f5f9;");
        } else if (node instanceof Button) {
            node.setVisible(false);
            node.setManaged(false);
        }

        if (node instanceof ScrollPane) {
            setReadOnly(((ScrollPane) node).getContent());
        } else if (node instanceof javafx.scene.Parent) {
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

    private void loadData() {
        try {
            ObservableList<Document> documents = FXCollections.observableArrayList(documentService.findAll());
            documentsTable.setItems(documents);
            
            // Mise à jour des stats dans le header
            statsNumberLabel.setText(String.valueOf(documents.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNouveauDocument(ActionEvent event) {
        System.out.println("Bouton Nouveau document cliqué");
        // Ce bouton pourrait aussi ouvrir le formulaire DocumentView.fxml
    }

    @FXML
    public void handleFiltrer(ActionEvent event) {
        loadData(); // Rafraîchir
    }
}
