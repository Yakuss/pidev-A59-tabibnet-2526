package com.pidev.controllers;

import com.pidev.services.VoskSpeechService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Voice Search Dialog using Vosk (offline speech recognition)
 * Pure JavaFX - no WebView needed!
 */
public class VoskVoiceSearchDialog {

    private Stage stage;
    private VoskSpeechService voskService;
    private Consumer<String> onSearchCallback;
    
    private Button micButton;
    private Label statusLabel;
    private TextArea transcriptArea;
    private Button searchButton;
    private Button clearButton;
    private ComboBox<String> languageCombo;
    private ProgressIndicator progressIndicator;
    
    private boolean isListening = false;
    private StringBuilder currentTranscript = new StringBuilder();

    /**
     * Create voice search dialog
     * @param owner Parent stage
     * @param modelPath Path to Vosk model (e.g., "models/vosk-model-small-fr-0.22")
     * @param onSearch Callback when search is triggered
     */
    public VoskVoiceSearchDialog(Stage owner, String modelPath, Consumer<String> onSearch) {
        this.onSearchCallback = onSearch;
        
        // Initialize Vosk
        try {
            voskService = new VoskSpeechService(modelPath);
            setupVoskCallbacks();
        } catch (Exception e) {
            showError("Erreur d'initialisation Vosk", e.getMessage());
            return;
        }
        
        // Create stage
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("🎤 Recherche Vocale (Vosk)");
        stage.setWidth(550);
        stage.setHeight(650);
        stage.setResizable(false);
        
        // Build UI
        VBox root = buildUI();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // Cleanup on close
        stage.setOnCloseRequest(e -> cleanup());
    }

    /**
     * Build the UI
     */
    private VBox buildUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");

        // Title
        Label title = new Label("🎤 Recherche Vocale");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Parlez pour rechercher un médecin");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.8);");

        // Language selector
        HBox langBox = new HBox(10);
        langBox.setAlignment(Pos.CENTER);
        Label langLabel = new Label("Langue:");
        langLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Français", "العربية", "English");
        languageCombo.setValue("Français");
        languageCombo.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        languageCombo.setDisable(true); // Disable during listening
        
        langBox.getChildren().addAll(langLabel, languageCombo);

        // Microphone button
        micButton = new Button();
        micButton.setPrefSize(120, 120);
        micButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 60;" +
            "-fx-font-size: 50px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        micButton.setText("🎤");
        micButton.setOnAction(e -> toggleListening());

        // Status
        statusLabel = new Label("Cliquez sur le microphone pour commencer");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(400);

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(30, 30);

        // Transcript area
        VBox transcriptBox = new VBox(10);
        transcriptBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label transcriptLabel = new Label("📝 Transcription:");
        transcriptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");
        
        transcriptArea = new TextArea();
        transcriptArea.setPromptText("Votre texte apparaîtra ici...");
        transcriptArea.setPrefRowCount(5);
        transcriptArea.setWrapText(true);
        transcriptArea.setEditable(false);
        transcriptArea.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10;" +
            "-fx-font-size: 14px;"
        );
        
        transcriptBox.getChildren().addAll(transcriptLabel, transcriptArea);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        searchButton = new Button("🔍 Rechercher");
        searchButton.setDisable(true);
        searchButton.setStyle(
            "-fx-background-color: #28a745;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 24;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        searchButton.setOnAction(e -> performSearch());
        
        clearButton = new Button("🔄 Effacer");
        clearButton.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 24;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        clearButton.setOnAction(e -> clearTranscript());
        
        Button closeButton = new Button("❌ Fermer");
        closeButton.setStyle(
            "-fx-background-color: #dc3545;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 24;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> {
            cleanup();
            stage.close();
        });
        
        buttonBox.getChildren().addAll(searchButton, clearButton, closeButton);

        // Examples
        VBox examplesBox = new VBox(8);
        examplesBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 12; -fx-padding: 15;");
        
        Label examplesLabel = new Label("💡 Exemples:");
        examplesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label ex1 = new Label("• Je cherche un cardiologue à Tunis");
        Label ex2 = new Label("• أريد طبيب أسنان في صفاقس");
        Label ex3 = new Label("• I need a pediatrician in Ariana");
        
        ex1.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");
        ex2.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");
        ex3.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");
        
        examplesBox.getChildren().addAll(examplesLabel, ex1, ex2, ex3);

        root.getChildren().addAll(
            title, subtitle, langBox, micButton, statusLabel, 
            progressIndicator, transcriptBox, buttonBox, examplesBox
        );

        return root;
    }

    /**
     * Setup Vosk callbacks
     */
    private void setupVoskCallbacks() {
        // Partial result (real-time)
        voskService.setOnPartialResult(text -> {
            Platform.runLater(() -> {
                transcriptArea.setText(currentTranscript.toString() + text);
            });
        });

        // Final result (complete sentence)
        voskService.setOnFinalResult(text -> {
            Platform.runLater(() -> {
                if (!text.isEmpty()) {
                    currentTranscript.append(text).append(" ");
                    transcriptArea.setText(currentTranscript.toString());
                    searchButton.setDisable(false);
                }
            });
        });

        // Error
        voskService.setOnError(error -> {
            Platform.runLater(() -> {
                statusLabel.setText("❌ Erreur: " + error);
                statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                stopListening();
            });
        });
    }

    /**
     * Toggle listening
     */
    private void toggleListening() {
        if (isListening) {
            stopListening();
        } else {
            startListening();
        }
    }

    /**
     * Start listening
     */
    private void startListening() {
        try {
            voskService.startListening();
            isListening = true;
            
            micButton.setStyle(
                "-fx-background-color: #ff6b6b;" +
                "-fx-background-radius: 60;" +
                "-fx-font-size: 50px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.5), 20, 0, 0, 5);"
            );
            micButton.setText("⏸️");
            
            statusLabel.setText("🎤 Écoute en cours... Parlez maintenant!");
            statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 16px;");
            
            languageCombo.setDisable(true);
            progressIndicator.setVisible(true);
            
        } catch (Exception e) {
            showError("Erreur microphone", e.getMessage());
        }
    }

    /**
     * Stop listening
     */
    private void stopListening() {
        voskService.stopListening();
        isListening = false;
        
        micButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 60;" +
            "-fx-font-size: 50px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        micButton.setText("🎤");
        
        statusLabel.setText("Cliquez sur le microphone pour recommencer");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        languageCombo.setDisable(false);
        progressIndicator.setVisible(false);
    }

    /**
     * Clear transcript
     */
    private void clearTranscript() {
        currentTranscript.setLength(0);
        transcriptArea.clear();
        searchButton.setDisable(true);
    }

    /**
     * Perform search
     */
    private void performSearch() {
        String query = transcriptArea.getText().trim();
        
        if (!query.isEmpty() && onSearchCallback != null) {
            onSearchCallback.accept(query);
            cleanup();
            stage.close();
        }
    }

    /**
     * Cleanup resources
     */
    private void cleanup() {
        try {
            if (isListening) {
                stopListening();
            }
            if (voskService != null) {
                voskService.dispose();
                voskService = null;
            }
        } catch (Exception e) {
            // Ignore cleanup errors
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Show the dialog
     */
    public void show() {
        stage.show();
    }
}
