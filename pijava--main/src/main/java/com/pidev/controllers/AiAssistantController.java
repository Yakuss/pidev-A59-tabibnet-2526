package com.pidev.controllers;

import com.pidev.models.BaseUser;
import com.pidev.models.Ordonnance;
import com.pidev.models.Rapport;
import com.pidev.services.AiApiService;
import com.pidev.services.OrdonnanceService;
import com.pidev.services.RapportService;
import com.pidev.services.VoiceRecognitionService;
import com.pidev.utils.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiAssistantController {

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private AnchorPane uploadZone;
    @FXML
    private Label selectedFileLabel;
    @FXML
    private TextArea questionTextArea;
    @FXML
    private Button sendButton;
    @FXML
    private Button micButton;
    @FXML
    private Label modeBadge;
    @FXML
    private VBox responseBox;
    @FXML
    private TextArea responseTextArea;
    @FXML
    private Circle confidenceIndicator;
    @FXML
    private Label confidenceLabel;

    private final RapportService rapportService = new RapportService();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();
    private final AiApiService aiApiService = new AiApiService("AIzaSyBr58APNDtuV-DaSxfGmXu__WubA1yFw7Q");
    private final VoiceRecognitionService voiceService = new VoiceRecognitionService();
    
    private boolean isListening = false;
    private File uploadedFile;
    private String extractedPdfText = "";
    private String dbContextText = "";

    @FXML
    public void initialize() {
        if (responseBox != null) {
            responseBox.setVisible(false);
            responseBox.setManaged(false);
        }
        buildDbContext();
    }

    private void buildDbContext() {
        new Thread(() -> {
            try {
                // Get connected doctor information
                BaseUser currentUser = UserSession.getInstance().getUser();
                Integer medecinId = currentUser != null ? currentUser.getId() : null;
                String medecinName = currentUser != null ? currentUser.getEmail() : "Inconnu";
                
                // Filter by doctor ID - only show data belonging to this doctor
                List<Rapport> rapports = new ArrayList<>();
                List<Ordonnance> ordonnances = new ArrayList<>();
                
                if (medecinId != null) {
                    // Get all reports and prescriptions for this doctor
                    rapports = rapportService.getAll().stream()
                        .filter(r -> r.getMedecinId() == medecinId)
                        .toList();
                    ordonnances = ordonnanceService.getAll().stream()
                        .filter(o -> o.getMedecinId() == medecinId)
                        .toList();
                }

                StringBuilder sb = new StringBuilder();
                sb.append("=== CONTEXTE MÉDECIN ===\n");
                sb.append("Médecin connecté: ").append(medecinName).append("\n");
                sb.append("ID Médecin: ").append(medecinId).append("\n\n");
                
                sb.append("IMPORTANT: Vous êtes un assistant médical. Vous ne devez répondre QU'AUX questions médicales.\n");
                sb.append("Vous avez accès UNIQUEMENT aux données de ce médecin (ID: ").append(medecinId).append(").\n");
                sb.append("Refusez poliment toute question non médicale (politique, sport, cuisine, etc.).\n\n");
                
                sb.append("=== HISTORIQUE DU DOSSIER PATIENT ===\n\n");

                sb.append("--- RAPPORTS MÉDICAUX (").append(rapports.size()).append(" rapports) ---\n");
                for (Rapport r : rapports) {
                    sb.append("- Rapport ID: ").append(r.getId())
                      .append(" | Patient ID: ").append(r.getPatientId())
                      .append("\n  Motif: ").append(r.getConsultationReason())
                      .append("\n  Diagnostic: ").append(r.getDiagnosis())
                      .append("\n  Observations: ").append(r.getObservations())
                      .append("\n  Recommandations: ").append(r.getRecommendations())
                      .append("\n  Traitements: ").append(r.getTreatments()).append("\n\n");
                }

                sb.append("--- ORDONNANCES (").append(ordonnances.size()).append(" ordonnances) ---\n");
                for (Ordonnance o : ordonnances) {
                    sb.append("- Ordonnance ID: ").append(o.getId())
                      .append(" | Patient ID: ").append(o.getPatientId())
                      .append("\n  Diagnostic: ").append(o.getDiagnosis())
                      .append("\n  Médicament: ").append(o.getMedicament())
                      .append("\n  Posologie: ").append(o.getPosologie())
                      .append("\n  Instructions: ").append(o.getInstructions())
                      .append("\n  Notes: ").append(o.getNotes()).append("\n\n");
                }

                this.dbContextText = sb.toString();
                System.out.println("✅ Contexte médical chargé: " + rapports.size() + " rapports, " + ordonnances.size() + " ordonnances");
            } catch (SQLException e) {
                System.err.println("❌ Erreur chargement context: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    public void handleSendMessage() {
        String question = questionTextArea.getText().trim();
        if (question.isEmpty()) return;

        String lowerQuestion = question.toLowerCase();
        
        // Handle greetings directly
        if (isGreeting(lowerQuestion)) {
            BaseUser currentUser = UserSession.getInstance().getUser();
            String doctorName = currentUser != null ? currentUser.getEmail().split("@")[0] : "Docteur";
            String greetingResponse = "Bonjour Docteur " + doctorName + " ! 👨‍⚕️\n\n" +
                "Je suis votre assistant médical intelligent. Comment puis-je vous aider aujourd'hui ?\n\n" +
                "Je peux vous aider avec :\n" +
                "• Vos rapports médicaux\n" +
                "• Vos ordonnances\n" +
                "• Les diagnostics de vos patients\n" +
                "• Les traitements et médicaments\n" +
                "• Les questions médicales générales";
            
            responseTextArea.setText(greetingResponse);
            responseBox.setVisible(true);
            responseBox.setManaged(true);
            confidenceIndicator.setFill(javafx.scene.paint.Color.web("#10b981"));
            confidenceLabel.setText("Salutation");
            return;
        }
        
        // Handle questions about the assistant itself
        if (isAboutAssistant(lowerQuestion)) {
            String aboutResponse = "🤖 À PROPOS DE MOI\n\n" +
                "Je suis un assistant médical intelligent spécialisé dans le domaine de la santé.\n\n" +
                "Mon rôle est de vous aider avec :\n" +
                "✓ L'analyse de vos rapports médicaux\n" +
                "✓ La consultation de vos ordonnances\n" +
                "✓ Les informations sur les diagnostics et traitements\n" +
                "✓ Les questions médicales et anatomiques\n\n" +
                "Je suis conçu UNIQUEMENT pour le domaine médical et la santé.\n" +
                "Je ne peux pas répondre aux questions sur d'autres sujets.";
            
            responseTextArea.setText(aboutResponse);
            responseBox.setVisible(true);
            responseBox.setManaged(true);
            confidenceIndicator.setFill(javafx.scene.paint.Color.web("#10b981"));
            confidenceLabel.setText("Information");
            return;
        }

        // Validate if question is forbidden (non-medical)
        if (isForbiddenTopic(question)) {
            responseTextArea.setText("❌ SUJET NON AUTORISÉ\n\n" +
                "Je suis un assistant médical spécialisé dans le domaine de la santé.\n\n" +
                "Je ne peux PAS répondre aux questions concernant :\n" +
                "• Le sport (football, matchs, équipes, etc.)\n" +
                "• La programmation (code, algorithmes, etc.)\n" +
                "• La cuisine (recettes, plats, etc.)\n" +
                "• La politique (élections, gouvernement, etc.)\n" +
                "• Le divertissement (films, musique, séries, etc.)\n" +
                "• L'art et la culture non médicale\n" +
                "• Les demandes de lister TOUTES les données\n\n" +
                "Veuillez me poser une question médicale ou de santé.");
            responseBox.setVisible(true);
            responseBox.setManaged(true);
            confidenceIndicator.setFill(javafx.scene.paint.Color.web("#ef4444"));
            confidenceLabel.setText("Sujet refusé");
            return;
        }

        sendButton.setDisable(true);
        sendButton.setText("⌛ Analyse...");

        new Thread(() -> {
            try {
                String context = extractedPdfText.isEmpty() ? dbContextText : "PDF: " + extractedPdfText + "\n\nDB: " + dbContextText;
                
                // Add medical restriction to the prompt
                String enhancedPrompt = "RÈGLES STRICTES:\n" +
                    "1. Vous êtes un assistant médical professionnel\n" +
                    "2. Répondez UNIQUEMENT aux questions médicales et de santé\n" +
                    "3. Utilisez UNIQUEMENT les données du médecin connecté\n" +
                    "4. Soyez précis et professionnel\n" +
                    "5. INTERDICTIONS ABSOLUES:\n" +
                    "   - Ne donnez JAMAIS de code informatique\n" +
                    "   - Ne donnez JAMAIS de recettes de cuisine\n" +
                    "   - Ne parlez JAMAIS de sport, politique, ou divertissement\n" +
                    "   - Ne listez JAMAIS tous les patients/ordonnances/rapports\n" +
                    "   - Répondez uniquement sur les données spécifiques demandées\n\n" +
                    "QUESTION: " + question + "\n\n" +
                    "CONTEXTE:\n" + context;
                
                String response = aiApiService.getAiResponse(enhancedPrompt, "");
                
                Platform.runLater(() -> {
                    responseTextArea.setText(response);
                    responseBox.setVisible(true);
                    responseBox.setManaged(true);
                    confidenceIndicator.setFill(javafx.scene.paint.Color.web("#10b981"));
                    confidenceLabel.setText("Réponse générée");
                    sendButton.setDisable(false);
                    sendButton.setText("🔍 Envoyer");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    responseTextArea.setText("Erreur: " + e.getMessage());
                    responseBox.setVisible(true);
                    responseBox.setManaged(true);
                    confidenceIndicator.setFill(javafx.scene.paint.Color.web("#ef4444"));
                    sendButton.setDisable(false);
                    sendButton.setText("🔍 Envoyer");
                });
            }
        }).start();
    }

    /**
     * Check if the message is a greeting
     */
    private boolean isGreeting(String question) {
        String[] greetings = {
            "bonjour", "bonsoir", "salut", "hello", "hi", "hey"
        };
        
        for (String greeting : greetings) {
            if (question.equals(greeting) || question.startsWith(greeting + " ") || question.startsWith(greeting + ",")) {
                System.out.println("✅ Salutation détectée");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the question is about the assistant itself
     */
    private boolean isAboutAssistant(String question) {
        String[] aboutKeywords = {
            "qui es-tu", "qui êtes-vous", "c'est quoi", "qu'est-ce que",
            "ton rôle", "votre rôle", "ton mail", "votre mail",
            "ton nom", "votre nom", "tu es qui", "vous êtes qui",
            "présente-toi", "présentez-vous"
        };
        
        for (String keyword : aboutKeywords) {
            if (question.contains(keyword)) {
                System.out.println("✅ Question sur l'assistant");
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if the question is about forbidden topics
     * Returns true for non-medical questions that should be rejected
     */
    private boolean isForbiddenTopic(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // Forbidden keywords for non-medical topics
        String[] forbiddenKeywords = {
            // Sports
            "football", "match", "équipe", "joueur", "coupe", "mondiale", "champion",
            "basket", "tennis", "rugby", "sport", "ballon", "stade",
            // Programming
            "code", "programmer", "python", "java", "javascript", "html", "css",
            "fonction", "variable", "algorithme", "script", "développer", "coder",
            // Cooking
            "recette", "cuisine", "gâteau", "chocolat", "cuire", "four", "ingrédient",
            "plat", "restaurant", "manger", "cuisiner",
            // Politics
            "politique", "élection", "président", "gouvernement", "parti", "vote",
            "ministre", "député", "parlement",
            // Entertainment
            "film", "cinéma", "série", "acteur", "musique", "chanson", "concert",
            "artiste", "album", "télévision", "tv",
            // Art & Culture (non-medical)
            "peinture", "tableau", "sculpture", "exposition", "musée",
            "roman", "livre", "poésie", "théâtre",
            // Security breaches - asking for ALL data
            "tous les patients", "toutes les ordonnances", "tous les rapports",
            "tous les documents", "liste complète", "base de données complète",
            "tout le monde", "tous les médecins", "liste de tous"
        };
        
        // Check for forbidden keywords
        for (String keyword : forbiddenKeywords) {
            if (lowerQuestion.contains(keyword)) {
                System.out.println("❌ Sujet interdit détecté: '" + keyword + "'");
                return true;
            }
        }
        
        return false;
    }

    @FXML
    public void handleMicClick() {
        if (!isListening) {
            isListening = true;
            micButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
            voiceService.startListening(new VoiceRecognitionService.TranscriptionListener() {
                @Override
                public void onTranscription(String text) {
                    Platform.runLater(() -> {
                        questionTextArea.setText(questionTextArea.getText() + " " + text);
                    });
                }
                @Override
                public void onError(String error) {
                    Platform.runLater(() -> {
                        isListening = false;
                        micButton.setStyle("");
                        confidenceLabel.setText("Erreur vocale: " + error);
                    });
                }
                @Override
                public void onStatus(String status) {
                    Platform.runLater(() -> confidenceLabel.setText(status));
                }
            });
        } else {
            isListening = false;
            voiceService.stopListening();
            micButton.setStyle("");
        }
    }

    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) event.acceptTransferModes(TransferMode.COPY);
        event.consume();
    }

    @FXML
    public void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                setUploadedFile(file);
            }
        }
        event.setDropCompleted(true);
        event.consume();
    }

    @FXML
    public void handleUploadClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) setUploadedFile(file);
    }

    private void setUploadedFile(File file) {
        this.uploadedFile = file;
        try (PDDocument document = PDDocument.load(file)) {
            extractedPdfText = new PDFTextStripper().getText(document);
            selectedFileLabel.setText("Fichier: " + file.getName());
            selectedFileLabel.setVisible(true);
            modeBadge.setText("MODE: ANALYSE PDF");
        } catch (IOException e) {
            extractedPdfText = "";
        }
    }
    
    // Stub methods for missing FXML handlers if any
    @FXML public void handleDragEntered(DragEvent e) {}
    @FXML public void handleDragExited(DragEvent e) {}
}