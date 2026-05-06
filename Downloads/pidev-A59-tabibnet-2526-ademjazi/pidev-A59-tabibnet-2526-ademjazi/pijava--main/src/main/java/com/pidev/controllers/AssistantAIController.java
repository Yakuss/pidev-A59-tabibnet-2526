package com.pidev.controllers;

import com.pidev.services.GeminiAIService;
import com.pidev.services.VoskSpeechService;
import com.pidev.utils.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AssistantAIController implements Initializable {

    @FXML private Label modeBadge;
    @FXML private AnchorPane uploadZone;
    @FXML private Label selectedFileLabel;
    @FXML private TextArea questionTextArea;
    @FXML private Button sendButton;
    @FXML private Button micButton;
    @FXML private VBox responseBox;
    @FXML private TextArea responseTextArea;

    private final GeminiAIService aiService = new GeminiAIService();
    private VoskSpeechService speechService;
    private File selectedPdf;
    private boolean isListening = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupModeBadge();
        try {
            // Path to model identified in the project
            speechService = new VoskSpeechService("models/vosk-model-small-fr-0.22");
            speechService.setOnFinalResult(text -> {
                Platform.runLater(() -> {
                    questionTextArea.setText(questionTextArea.getText() + " " + text);
                });
            });
        } catch (Exception e) {
            System.err.println("Speech service not available: " + e.getMessage());
        }
    }

    private void setupModeBadge() {
        if (UserSession.getInstance().getSelectedPatientId() != null) {
            modeBadge.setText("MODE : DOSSIER PATIENT #" + UserSession.getInstance().getSelectedPatientId());
            modeBadge.setStyle("-fx-background-color: #eff6ff; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: #2563eb;");
        } else {
            modeBadge.setText("MODE : GÉNÉRAL");
            modeBadge.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: #64748b;");
        }
    }

    @FXML
    private void handleUploadClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        selectedPdf = fileChooser.showOpenDialog(uploadZone.getScene().getWindow());
        
        if (selectedPdf != null) {
            selectedFileLabel.setText("Fichier sélectionné : " + selectedPdf.getName());
            selectedFileLabel.setVisible(true);
            uploadZone.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 12; -fx-background-radius: 12;");
        }
    }

    @FXML
    private void handleSendMessage() {
        String question = questionTextArea.getText();
        if (question == null || question.trim().isEmpty()) return;

        sendButton.setDisable(true);
        sendButton.setText("Analyse en cours...");

        new Thread(() -> {
            try {
                String prompt = question;
                if (selectedPdf != null) {
                    prompt = "Analyse ce document médical et réponds à ma question: " + question;
                }
                
                String response = aiService.askAI(prompt);
                
                Platform.runLater(() -> {
                    responseTextArea.setText(response);
                    responseBox.setVisible(true);
                    responseBox.setManaged(true);
                    sendButton.setDisable(false);
                    sendButton.setText("🔍  Envoyer la question");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    sendButton.setDisable(false);
                    sendButton.setText("🔍  Envoyer la question");
                });
            }
        }).start();
    }

    @FXML
    private void handleMicClick() {
        if (speechService == null) return;

        try {
            if (!isListening) {
                isListening = true;
                micButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 24; -fx-background-radius: 10;");
                speechService.startListening();
            } else {
                isListening = false;
                micButton.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #10b981, #059669); -fx-text-fill: white; -fx-font-size: 24; -fx-background-radius: 10;");
                speechService.stopListening();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
