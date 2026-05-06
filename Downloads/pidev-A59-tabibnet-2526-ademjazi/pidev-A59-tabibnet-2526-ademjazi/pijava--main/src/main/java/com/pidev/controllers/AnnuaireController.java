package com.pidev.controllers;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;
import com.pidev.models.DoctorAPI;
import com.pidev.services.DoctorAPIService;
import com.pidev.services.RAGSearchService;
import com.pidev.controllers.VoskVoiceSearchDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.util.List;

/**
 * Controller for the National Doctor Directory (Annuaire)
 * Consumes data from Flask API running on localhost:5000
 */
public class AnnuaireController {

    @FXML private TextField searchNameField;
    @FXML private ComboBox<Specialty> searchSpecialtyCombo;
    @FXML private ComboBox<Governorate> searchGovernorateCombo;
    @FXML private ListView<DoctorAPI> doctorListView;
    @FXML private Label statusLabel;
    @FXML private Label paginationLabel;
    @FXML private Label paginationLabel2;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private Label apiStatusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private StackPane loadingOverlay;
    @FXML private VBox contentArea;
    @FXML private Label errorLabel;

    private final DoctorAPIService apiService = new DoctorAPIService();
    private ObservableList<DoctorAPI> doctorList = FXCollections.observableArrayList();
    private DoctorAPIService.PaginationInfo paginationInfo = new DoctorAPIService.PaginationInfo();
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;

    @FXML
    public void initialize() {
        setupDoctorCards();
        setupComboBoxes();
        checkAPIStatus();
        loadDoctors();
        
        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        
        // Add listeners to clear error when typing
        if (searchNameField != null) {
            searchNameField.textProperty().addListener((obs, old, val) -> hideError());
        }
        if (searchSpecialtyCombo != null) {
            searchSpecialtyCombo.valueProperty().addListener((obs, old, val) -> hideError());
        }
        if (searchGovernorateCombo != null) {
            searchGovernorateCombo.valueProperty().addListener((obs, old, val) -> hideError());
        }
    }
    
    /**
     * Setup ComboBoxes with enum values
     */
    private void setupComboBoxes() {
        // Setup Specialty ComboBox
        if (searchSpecialtyCombo != null) {
            ObservableList<Specialty> specialtyOptions = FXCollections.observableArrayList();
            specialtyOptions.add(null); // "Toutes les spécialités"
            specialtyOptions.addAll(Specialty.values());
            searchSpecialtyCombo.setItems(specialtyOptions);
            
            // Custom cell factory
            searchSpecialtyCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<Specialty>() {
                @Override
                protected void updateItem(Specialty item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Toutes les spécialités");
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });
            
            searchSpecialtyCombo.setButtonCell(new javafx.scene.control.ListCell<Specialty>() {
                @Override
                protected void updateItem(Specialty item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Toutes les spécialités");
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });
            
            searchSpecialtyCombo.setValue(null);
        }
        
        // Setup Governorate ComboBox
        if (searchGovernorateCombo != null) {
            ObservableList<Governorate> governorateOptions = FXCollections.observableArrayList();
            governorateOptions.add(null); // "Tous les gouvernorats"
            governorateOptions.addAll(Governorate.values());
            searchGovernorateCombo.setItems(governorateOptions);
            
            // Custom cell factory
            searchGovernorateCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<Governorate>() {
                @Override
                protected void updateItem(Governorate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Tous les gouvernorats");
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });
            
            searchGovernorateCombo.setButtonCell(new javafx.scene.control.ListCell<Governorate>() {
                @Override
                protected void updateItem(Governorate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Tous les gouvernorats");
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });
            
            searchGovernorateCombo.setValue(null);
        }
    }

    /**
     * Check if Flask API is running and accessible
     */
    private void checkAPIStatus() {
        new Thread(() -> {
            try {
                JSONObject status = apiService.getAPIStatus();
                String message = status.optString("message", "Unknown");
                boolean dataLoaded = status.optBoolean("data_loaded", false);
                int recordCount = status.optInt("record_count", 0);
                
                Platform.runLater(() -> {
                    if (apiStatusLabel != null) {
                        if (dataLoaded) {
                            apiStatusLabel.setText("✅ API Connectée • " + recordCount + " médecins disponibles");
                            apiStatusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 12px;");
                        } else {
                            apiStatusLabel.setText("⚠️ API connectée mais données non chargées");
                            apiStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px;");
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (apiStatusLabel != null) {
                        apiStatusLabel.setText("❌ API non disponible (localhost:5000)");
                        apiStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
                    }
                    showError("Impossible de se connecter à l'API. Assurez-vous que le serveur Flask est démarré sur le port 5000.");
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Setup custom cell factory for doctor cards
     */
    private void setupDoctorCards() {
        doctorListView.setCellFactory(listView -> new ListCell<DoctorAPI>() {
            @Override
            protected void updateItem(DoctorAPI doctor, boolean empty) {
                super.updateItem(doctor, empty);
                
                if (empty || doctor == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createDoctorCard(doctor));
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        });
    }

    /**
     * Create a beautiful card for each doctor
     */
    private VBox createDoctorCard(DoctorAPI doctor) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: #0e1220;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        // Header with name and specialty
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        // Avatar circle with initials
        Label avatar = new Label(doctor.getInitials());
        avatar.setStyle(
            "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 40px;" +
            "-fx-min-height: 40px;" +
            "-fx-max-width: 40px;" +
            "-fx-max-height: 40px;" +
            "-fx-background-radius: 20;" +
            "-fx-alignment: center;"
        );

        // Name and specialty
        VBox nameBox = new VBox();
        nameBox.setSpacing(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label nameLabel = new Label("Dr. " + (doctor.getName() != null ? doctor.getName() : "Nom inconnu"));
        nameLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);

        Label specialtyLabel = new Label(doctor.getSpecialty() != null ? doctor.getSpecialty() : "Spécialité non spécifiée");
        specialtyLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 13px;");
        specialtyLabel.setWrapText(true);

        nameBox.getChildren().addAll(nameLabel, specialtyLabel);

        // Mode badge
        Label modeBadge = new Label("🏥 Libre Pratique");
        modeBadge.setStyle(
            "-fx-background-color: rgba(34,197,94,0.15);" +
            "-fx-text-fill: #22c55e;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 4 8;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(34,197,94,0.3);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;"
        );

        header.getChildren().addAll(avatar, nameBox, modeBadge);

        // Contact and location info
        VBox infoBox = new VBox();
        infoBox.setSpacing(6);

        if (doctor.getGovernorate() != null && !doctor.getGovernorate().isEmpty()) {
            Label govLabel = new Label("📍 " + doctor.getGovernorate());
            govLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            infoBox.getChildren().add(govLabel);
        }

        if (doctor.getAddress() != null && !doctor.getAddress().isEmpty()) {
            Label addressLabel = new Label("🏠 " + doctor.getAddress());
            addressLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            addressLabel.setWrapText(true);
            infoBox.getChildren().add(addressLabel);
        }

        if (doctor.getPhone() != null && !doctor.getPhone().isEmpty()) {
            Label phoneLabel = new Label("📞 " + doctor.getPhone());
            phoneLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            infoBox.getChildren().add(phoneLabel);
        }

        if (doctor.getEmail() != null && !doctor.getEmail().isEmpty()) {
            Label emailLabel = new Label("📧 " + doctor.getEmail());
            emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            emailLabel.setWrapText(true);
            infoBox.getChildren().add(emailLabel);
        }

        card.getChildren().addAll(header, infoBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #141826;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #667eea;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 12, 0, 0, 4);" +
            "-fx-cursor: hand;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #0e1220;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #252d42;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        ));

        return card;
    }

    /**
     * Load doctors from API
     */
    private void loadDoctors() {
        showLoading(true);
        hideError();
        
        new Thread(() -> {
            try {
                String name = searchNameField != null ? searchNameField.getText() : null;
                String specialty = null;
                String governorate = null;
                
                // Get specialty display name if selected
                if (searchSpecialtyCombo != null && searchSpecialtyCombo.getValue() != null) {
                    specialty = searchSpecialtyCombo.getValue().getDisplayName();
                }
                
                // Get governorate display name if selected
                if (searchGovernorateCombo != null && searchGovernorateCombo.getValue() != null) {
                    governorate = searchGovernorateCombo.getValue().getDisplayName();
                }
                
                JSONObject response = apiService.searchDoctors(name, specialty, governorate, currentPage, PAGE_SIZE);
                List<DoctorAPI> doctors = apiService.parseDoctorsFromResponse(response);
                paginationInfo = apiService.getPaginationInfo(response);
                
                Platform.runLater(() -> {
                    doctorList.setAll(doctors);
                    doctorListView.setItems(doctorList);
                    updatePaginationControls();
                    updateStatusLabel();
                    showLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Erreur lors du chargement des médecins: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Search button handler
     */
    @FXML
    public void handleSearch() {
        currentPage = 1; // Reset to first page on new search
        loadDoctors();
    }

    /**
     * Clear filters button handler
     */
    @FXML
    public void handleClearFilters() {
        if (searchNameField != null) searchNameField.clear();
        if (searchSpecialtyCombo != null) searchSpecialtyCombo.setValue(null);
        if (searchGovernorateCombo != null) searchGovernorateCombo.setValue(null);
        currentPage = 1;
        loadDoctors();
    }

    /**
     * Previous page button handler
     */
    @FXML
    public void handlePreviousPage() {
        if (paginationInfo.hasPreviousPage()) {
            currentPage--;
            loadDoctors();
        }
    }

    /**
     * Next page button handler
     */
    @FXML
    public void handleNextPage() {
        if (paginationInfo.hasNextPage()) {
            currentPage++;
            loadDoctors();
        }
    }

    /**
     * Refresh button handler
     */
    @FXML
    public void handleRefresh() {
        checkAPIStatus();
        loadDoctors();
    }

    /**
     * Update pagination controls
     */
    private void updatePaginationControls() {
        String paginationText = String.format("Page %d / %d", 
            paginationInfo.currentPage, 
            Math.max(1, paginationInfo.totalPages));
        
        if (paginationLabel != null) {
            paginationLabel.setText(paginationText);
        }
        
        if (paginationLabel2 != null) {
            paginationLabel2.setText(paginationText);
        }
        
        if (btnPrevious != null) {
            btnPrevious.setDisable(!paginationInfo.hasPreviousPage());
        }
        
        if (btnNext != null) {
            btnNext.setDisable(!paginationInfo.hasNextPage());
        }
    }

    /**
     * Update status label
     */
    private void updateStatusLabel() {
        if (statusLabel != null) {
            if (doctorList.isEmpty()) {
                statusLabel.setText("Aucun médecin trouvé");
                statusLabel.setStyle("-fx-text-fill: #9ca3af;");
            } else {
                statusLabel.setText(doctorList.size() + " médecin(s) affiché(s)");
                statusLabel.setStyle("-fx-text-fill: #22c55e;");
            }
        }
    }

    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);
        }
        if (contentArea != null) {
            contentArea.setDisable(show);
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText("❌ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Hide error message
     */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Open Vosk voice search dialog
     */
    @FXML
    public void openVoskVoiceSearch() {
        try {
            // Get current stage
            Stage stage = (Stage) searchNameField.getScene().getWindow();
            
            // Path to Vosk model - try multiple locations
            String modelPath = findVoskModel();
            
            if (modelPath == null) {
                showError("Modèle Vosk introuvable!\n\n" +
                    "Téléchargez le modèle depuis:\n" +
                    "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip\n\n" +
                    "Extrayez-le dans: pijava--main/models/vosk-model-small-fr-0.22/");
                return;
            }
            
            // Create and show dialog
            VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
                stage, 
                modelPath, 
                this::performRAGSearch
            );
            dialog.show();
            
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la recherche vocale: " + e.getMessage() + 
                "\n\nAssurez-vous que le modèle Vosk est téléchargé dans le dossier 'models/'");
            e.printStackTrace();
        }
    }
    
    /**
     * Find Vosk model in multiple possible locations
     */
    private String findVoskModel() {
        String[] possiblePaths = {
            "models/vosk-model-small-fr-0.22",
            "pijava--main/models/vosk-model-small-fr-0.22",
            "../models/vosk-model-small-fr-0.22",
            System.getProperty("user.dir") + "/models/vosk-model-small-fr-0.22",
            System.getProperty("user.dir") + "/pijava--main/models/vosk-model-small-fr-0.22"
        };
        
        for (String path : possiblePaths) {
            java.io.File modelDir = new java.io.File(path);
            if (modelDir.exists() && modelDir.isDirectory()) {
                // Check if it contains the required files
                java.io.File amDir = new java.io.File(modelDir, "am");
                if (amDir.exists()) {
                    System.out.println("✅ Found Vosk model at: " + modelDir.getAbsolutePath());
                    return modelDir.getAbsolutePath();
                }
            }
        }
        
        System.err.println("❌ Vosk model not found in any of these locations:");
        for (String path : possiblePaths) {
            System.err.println("   - " + new java.io.File(path).getAbsolutePath());
        }
        
        return null;
    }

    /**
     * Perform RAG search with transcribed text from voice
     * @param query Natural language query from voice recognition
     */
    private void performRAGSearch(String query) {
        showLoading(true);
        hideError();
        
        new Thread(() -> {
            try {
                RAGSearchService ragService = new RAGSearchService();
                
                // Query the RAG API with natural language
                JSONObject response = ragService.query(query, 8);
                
                Platform.runLater(() -> {
                    if (ragService.isSuccess(response)) {
                        // Success - display results
                        String aiResponse = ragService.getResponseSentence(response);
                        List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
                        
                        doctorList.setAll(doctors);
                        doctorListView.setItems(doctorList);
                        
                        // Update status with AI response
                        if (statusLabel != null) {
                            statusLabel.setText("🤖 " + aiResponse + " (" + doctors.size() + " résultat(s))");
                            statusLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: 600;");
                        }
                        
                        // Clear pagination (RAG doesn't use pagination)
                        if (paginationLabel != null) {
                            paginationLabel.setText("Recherche IA");
                        }
                        if (paginationLabel2 != null) {
                            paginationLabel2.setText("Recherche IA");
                        }
                        if (btnPrevious != null) {
                            btnPrevious.setDisable(true);
                        }
                        if (btnNext != null) {
                            btnNext.setDisable(true);
                        }
                        
                    } else if (ragService.isGreeting(response)) {
                        // User just said hello
                        showError("👋 " + ragService.getResponseSentence(response));
                        
                    } else if (ragService.isInsufficientContext(response)) {
                        // No results found
                        showError("❌ " + ragService.getResponseSentence(response));
                        doctorList.clear();
                    }
                    
                    showLoading(false);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Erreur de recherche IA: " + e.getMessage() + 
                        "\n\nAssurez-vous que l'API Flask est démarrée sur le port 5000.");
                });
                e.printStackTrace();
            }
        }).start();
    }
}
