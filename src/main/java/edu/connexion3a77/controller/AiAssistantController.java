package edu.connexion3a77.controller;

import edu.connexion3a77.entities.Ordonnance;
import edu.connexion3a77.entities.Rapport;
import edu.connexion3a77.services.AiApiService;
import edu.connexion3a77.services.OrdonnanceService;
import edu.connexion3a77.services.RapportService;
import edu.connexion3a77.services.VoiceRecognitionService;
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

    // ==================== FXML FIELDS ====================
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private AnchorPane mainContainer;
    @FXML
    private AnchorPane headerPane;
    @FXML
    private AnchorPane mainCard;
    @FXML
    private HBox cardHeader;
    @FXML
    private VBox cardBody;
    @FXML
    private VBox documentSection;
    @FXML
    private VBox questionSection;
    @FXML
    private HBox footerBox;

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

    // ==================== SERVICES ====================
    private final RapportService rapportService = new RapportService();
    private final OrdonnanceService ordonnanceService = new OrdonnanceService();

    // Clé API Gemini configurée
    private final AiApiService aiApiService = new AiApiService("AIzaSyDq_F5_WE1UgJu3lhg0bTk9fxAp_taqW1M");
    private final VoiceRecognitionService voiceService = new VoiceRecognitionService();
    private boolean isListening = false;

    // ==================== ÉTAT DU DOCUMENT ====================
    private File uploadedFile;
    private String extractedPdfText = "";
    private String dbContextText = "";

    // ==================== STRUCTURES DE DONNÉES ====================zzz

    /**
     * Représente les informations du document extraites du PDF
     * Correspond à la section "Informations du Document" du PdfService
     */
    private static class DocumentInfo {
        String nomFichier = "Non spécifié";
        String typeDossier = "Non spécifié";
        String dateCreation = "Non spécifiée";
        String description = "Non spécifiée";

        @Override
        public String toString() {
            return String.format("DocumentInfo[nom=%s, type=%s, date=%s, desc=%s]",
                    nomFichier, typeDossier, dateCreation, description);
        }
    }

    /**
     * Représente un rapport médical extrait du PDF
     * Correspond aux cartes de la section "Rapports Médicaux"
     */
    private static class ParsedRapport {
        String motif = "";
        String diagnostic = "";
        String observations = "";

        public boolean isValid() {
            return !motif.isEmpty() || !diagnostic.isEmpty();
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            if (!motif.isEmpty())
                map.put("motif", motif);
            if (!diagnostic.isEmpty())
                map.put("diagnostic", diagnostic);
            if (!observations.isEmpty())
                map.put("observations", observations);
            return map;
        }
    }

    /**
     * Représente une ordonnance extraite du PDF
     * Correspond aux cartes de la section "Ordonnances"
     */
    private static class ParsedOrdonnance {
        String medicament = "";
        String posologie = "";
        String instructions = "";

        public boolean isValid() {
            return !medicament.isEmpty() || !posologie.isEmpty();
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            if (!medicament.isEmpty())
                map.put("médicament", medicament);
            if (!posologie.isEmpty())
                map.put("posologie", posologie);
            if (!instructions.isEmpty())
                map.put("instructions", instructions);
            return map;
        }
    }

    /**
     * Contient l'analyse complète d'un PDF médical
     */
    private static class PdfAnalysis {
        String documentType = "Inconnu";
        DocumentInfo documentInfo = new DocumentInfo();
        List<ParsedRapport> rapports = new ArrayList<>();
        List<ParsedOrdonnance> ordonnances = new ArrayList<>();

        void clear() {
            documentType = "Inconnu";
            documentInfo = new DocumentInfo();
            rapports.clear();
            ordonnances.clear();
        }

        boolean hasData() {
            return !rapports.isEmpty() || !ordonnances.isEmpty() ||
                    !documentInfo.nomFichier.equals("Non spécifié");
        }

        int getRapportCount() {
            return rapports.size();
        }

        int getOrdonnanceCount() {
            return ordonnances.size();
        }
    }

    private final PdfAnalysis currentAnalysis = new PdfAnalysis();

    // ==================== INITIALISATION ====================

    @FXML
    public void initialize() {
        if (responseBox != null) {
            responseBox.setVisible(false);
            responseBox.setManaged(false);
        }
        buildDbContext();
    }

    /**
     * Charge le contexte de la base de données en arrière-plan
     */
    private void buildDbContext() {
        new Thread(() -> {
            try {
                List<Rapport> rapports = rapportService.findAll();
                List<Ordonnance> ordonnances = ordonnanceService.findAll();

                StringBuilder sb = new StringBuilder();
                sb.append("HISTORIQUE COMPLET DU DOSSIER PATIENT (BASE DE DONNÉES) :\n\n");
                
                sb.append("--- RAPPORTS MÉDICAUX ENREGISTRÉS ---\n");
                for (Rapport r : rapports) {
                    sb.append("- Rapport ID: ").append(r.getId());
                    if (r.getDocument() != null) sb.append(" | Dans Document: ").append(r.getDocument().getNomFichier());
                    if (r.getPatient() != null) sb.append(" | Patient: ").append(r.getPatient().getNom());
                    sb.append("\n  Motif: ").append(r.getConsultationReason())
                      .append("\n  Diagnostic: ").append(r.getDiagnosis())
                      .append("\n  Observations: ").append(r.getObservations()).append("\n\n");
                }
                
                sb.append("--- ORDONNANCES ENREGISTRÉES ---\n");
                for (Ordonnance o : ordonnances) {
                    sb.append("- Ordonnance ID: ").append(o.getId());
                    if (o.getDocument() != null) sb.append(" | Dans Document: ").append(o.getDocument().getNomFichier());
                    sb.append("\n  Médicament: ").append(o.getMedicament())
                      .append("\n  Posologie: ").append(o.getPosologie())
                      .append("\n  Instructions: ").append(o.getInstructions()).append("\n\n");
                }
                
                this.dbContextText = sb.toString();
                System.out.println("IA: Contexte DB chargé avec " + rapports.size() +
                        " rapports et " + ordonnances.size() + " ordonnances.");
            } catch (SQLException e) {
                System.err.println("Erreur chargement DB Context: " + e.getMessage());
            }
        }).start();
    }

    // ==================== GESTION DES MESSAGES ====================

    @FXML
    public void handleSendMessage() {
        String question = questionTextArea.getText().trim();
        if (question.isEmpty())
            return;

        sendButton.setDisable(true);
        sendButton.setText("⌛ Analyse en cours...");

        new Thread(() -> {
            try {
                Thread.sleep(600); // Effet de réflexion
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                try {
                    String response = generateResponse(question);
                    responseTextArea.setText(response);
                    confidenceIndicator.setFill(javafx.scene.paint.Color.web("#10b981"));
                    confidenceLabel.setText("Réponse générée par IA (API)");
                } catch (Exception e) {
                    responseTextArea.setText("### ❌ Erreur API\n\n" + e.getMessage() +
                            "\n\nVérifiez votre connexion internet et votre clé API dans le code.");
                    confidenceIndicator.setFill(javafx.scene.paint.Color.web("#ef4444"));
                    confidenceLabel.setText("Erreur lors de la génération");
                }

                responseBox.setVisible(true);
                responseBox.setManaged(true);
                sendButton.setDisable(false);
                sendButton.setText("🔍 Envoyer la question");
            });
        }).start();
    }

    @FXML
    public void handleMicClick() {
        if (!isListening) {
            isListening = true;
            micButton.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ef4444, #dc2626); -fx-text-fill: white; -fx-font-size: 24; -fx-background-radius: 10; -fx-cursor: hand;");
            
            voiceService.startListening(new VoiceRecognitionService.TranscriptionListener() {
                @Override
                public void onTranscription(String text) {
                    Platform.runLater(() -> {
                        String current = questionTextArea.getText();
                        questionTextArea.setText(current + (current.isEmpty() ? "" : " ") + text);
                    });
                }

                @Override
                public void onError(String error) {
                    Platform.runLater(() -> {
                        isListening = false;
                        resetMicButton();
                        questionTextArea.setText("Erreur vocale: " + error);
                    });
                }

                @Override
                public void onStatus(String status) {
                    Platform.runLater(() -> {
                        confidenceLabel.setText(status);
                        confidenceIndicator.setFill(javafx.scene.paint.Color.web("#3b82f6"));
                        responseBox.setVisible(true);
                        responseBox.setManaged(true);
                    });
                }
            });
        } else {
            isListening = false;
            voiceService.stopListening();
            resetMicButton();
        }
    }

    private void resetMicButton() {
        Platform.runLater(() -> {
            micButton.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #10b981, #059669); -fx-text-fill: white; -fx-font-size: 24; -fx-background-radius: 10; -fx-cursor: hand;");
        });
    }

    // ==================== DRAG & DROP ====================

    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    public void handleDragEntered(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            uploadZone.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #2563eb; " +
                    "-fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 12; -fx-background-radius: 12;");
        }
        event.consume();
    }

    @FXML
    public void handleDragExited(DragEvent event) {
        resetUploadZoneStyle();
        event.consume();
    }

    @FXML
    public void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            if (!files.isEmpty()) {
                File file = files.get(0);
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    setUploadedFile(file);
                    success = true;
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void handleUploadClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un document médical (PDF)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File file = fileChooser.showOpenDialog(uploadZone.getScene().getWindow());
        if (file != null) {
            setUploadedFile(file);
        }
    }

    // ==================== EXTRACTION PDF ====================

    private void setUploadedFile(File file) {
        this.uploadedFile = file;
        this.extractedPdfText = extractTextFromPdf(file);

        // Parsing intelligent adapté au format exact de PdfService
        parsePdfStructure(this.extractedPdfText);

        selectedFileLabel.setText(String.format(
                "✅ Document analysé : %s | %d rapport(s), %d ordonnance(s)",
                file.getName(),
                currentAnalysis.getRapportCount(),
                currentAnalysis.getOrdonnanceCount()));
        selectedFileLabel.setVisible(true);

        uploadZone.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #10b981; " +
                "-fx-border-width: 2; -fx-border-style: solid; -fx-border-radius: 12; -fx-background-radius: 12;");

        updateBadge("MODE : PDF ANALYSE (STRUCTURÉ)", "#f0fdf4", "#10b981");
    }

    private void resetUploadZoneStyle() {
        uploadZone.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; " +
                "-fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 12; -fx-background-radius: 12;");
    }

    /**
     * Extrait le texte brut d'un PDF en préservant la structure
     */
    private String extractTextFromPdf(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(true);
            return stripper.getText(document);
        } catch (IOException e) {
            System.err.println("Erreur d'extraction PDF: " + e.getMessage());
            return "";
        }
    }

    // ==================== PARSING INTELLIGENT DU PDF ====================

    /**
     * Parse la structure exacte des PDF générés par PdfService.
     *
     * STRUCTURE ATTENDUE DU TEXTE EXTRAIT :
     * -----------------------------------------------------------
     * 🏥 DOSSIER MÉDICAL
     * Gestion complète des documents de santé
     *
     * Informations du Document
     * [barre verticale bleue] Informations du Document
     *
     * Nom du fichier : [valeur]
     * Type de dossier : [valeur]
     * Date de création : [valeur]
     * Description : [valeur] (optionnel)
     *
     * Rapports Médicaux (N)
     * [barre verticale bleue] Rapports Médicaux (N)
     *
     * Motif : [valeur]
     * Diagnostic : [valeur]
     * Observations : [valeur] (optionnel)
     *
     * [Répété pour chaque rapport...]
     *
     * Ordonnances (N)
     * [barre verticale verte] Ordonnances (N)
     *
     * Médicament : [valeur]
     * Posologie : [valeur]
     * Instructions : [valeur]
     *
     * [Répété pour chaque ordonnance...]
     *
     * Page 1
     * -----------------------------------------------------------
     */
    private void parsePdfStructure(String text) {
        currentAnalysis.clear();

        if (text == null || text.trim().isEmpty()) {
            System.err.println("PDF vide ou illisible");
            return;
        }

        // Nettoyage intelligent du texte
        String cleanText = text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("[\\t\\f]", " ")
                .replaceAll(" {2,}", " ")
                .trim();

        // Supprimer le footer "Page N" à la fin
        cleanText = cleanText.replaceAll("\\nPage\\s+\\d+\\s*$", "");

        // Détection du type de document
        if (cleanText.contains("DOSSIER MÉDICAL")) {
            currentAnalysis.documentType = "Dossier Médical Complet";
        }

        System.out.println("=== TEXTE PDF NETTOYÉ ===");
        System.out.println(cleanText);
        System.out.println("=========================");

        // Extraction par sections délimitées
        extractDocumentInfoSection(cleanText);
        extractRapportsSection(cleanText);
        extractOrdonnancesSection(cleanText);

        System.out.println(String.format("PDF Parse terminé: %d rapports, %d ordonnances",
                currentAnalysis.getRapportCount(), currentAnalysis.getOrdonnanceCount()));
    }

    /**
     * Extrait la section "Informations du Document"
     * Délimiteurs: début = "Informations du Document", fin = "Rapports Médicaux" ou
     * "Ordonnances" ou fin
     */
    private void extractDocumentInfoSection(String text) {
        // Pattern pour isoler la section entre "Informations du Document" et la
        // prochaine section
        Pattern sectionPattern = Pattern.compile(
                "Informations du Document\\s*(?:\\n[^\\n]*?\\n)?\\s*" + // Titre de section (avec barre)
                        "(.*?)" + // Contenu capturé
                        "(?=\\nRapports Médicaux|\\nOrdonnances|\\z)", // Fin au prochain titre
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = sectionPattern.matcher(text);
        if (matcher.find()) {
            String section = matcher.group(1).trim();

            currentAnalysis.documentInfo.nomFichier = extractFieldValueSafe(section, "Nom du fichier");
            currentAnalysis.documentInfo.typeDossier = extractFieldValueSafe(section, "Type de dossier");
            currentAnalysis.documentInfo.dateCreation = extractFieldValueSafe(section, "Date de création");
            currentAnalysis.documentInfo.description = extractFieldValueSafe(section, "Description");

            System.out.println("Document Info: " + currentAnalysis.documentInfo);
        }
    }

    /**
     * Extrait la section "Rapports Médicaux"
     * Chaque rapport est délimité par "Motif :" et se termine avant le prochain
     * "Motif :" ou "Ordonnances"
     */
    private void extractRapportsSection(String text) {
        // Isoler la section rapports
        Pattern sectionPattern = Pattern.compile(
                "Rapports Médicaux\\s*\\(\\d+\\)\\s*(?:\\n[^\\n]*?\\n)?\\s*(.*?)(?=\\nOrdonnances|\\z)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher sectionMatcher = sectionPattern.matcher(text);
        if (!sectionMatcher.find()) {
            System.out.println("Aucune section 'Rapports Médicaux' trouvée");
            return;
        }

        String rapportsSection = sectionMatcher.group(1).trim();

        // Diviser en rapports individuels par "Motif :"
        // Utiliser un split qui préserve le délimiteur
        String[] rapportBlocks = rapportsSection.split("(?=Motif\\s*:\\s*)");

        for (String block : rapportBlocks) {
            block = block.trim();
            if (block.isEmpty())
                continue;

            // Vérifier que c'est bien un rapport (doit contenir Motif ou Diagnostic)
            if (!block.contains("Motif") && !block.contains("Diagnostic")) {
                continue;
            }

            ParsedRapport rapport = new ParsedRapport();
            rapport.motif = extractFieldValueSafe(block, "Motif");
            rapport.diagnostic = extractFieldValueSafe(block, "Diagnostic");
            rapport.observations = extractFieldValueSafe(block, "Observations");

            if (rapport.isValid()) {
                currentAnalysis.rapports.add(rapport);
                System.out.println("Rapport trouvé: " + rapport.motif + " -> " + rapport.diagnostic);
            }
        }
    }

    /**
     * Extrait la section "Ordonnances"
     * Chaque ordonnance est délimitée par "Médicament :" et se termine avant le
     * prochain "Médicament :" ou fin
     */
    private void extractOrdonnancesSection(String text) {
        // Isoler la section ordonnances
        Pattern sectionPattern = Pattern.compile(
                "Ordonnances\\s*\\(\\d+\\)\\s*(?:\\n[^\\n]*?\\n)?\\s*(.*?)(?=\\z)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher sectionMatcher = sectionPattern.matcher(text);
        if (!sectionMatcher.find()) {
            System.out.println("Aucune section 'Ordonnances' trouvée");
            return;
        }

        String ordonnancesSection = sectionMatcher.group(1).trim();

        // Supprimer le footer s'il reste
        ordonnancesSection = ordonnancesSection.replaceAll("Page\\s+\\d+\\s*$", "").trim();

        // Diviser en ordonnances individuelles par "Médicament :"
        String[] ordonnanceBlocks = ordonnancesSection.split("(?=Médicament\\s*:\\s*)");

        for (String block : ordonnanceBlocks) {
            block = block.trim();
            if (block.isEmpty())
                continue;

            // Vérifier que c'est bien une ordonnance
            if (!block.contains("Médicament") && !block.contains("Posologie")) {
                continue;
            }

            ParsedOrdonnance ordonnance = new ParsedOrdonnance();
            ordonnance.medicament = extractFieldValueSafe(block, "Médicament");
            ordonnance.posologie = extractFieldValueSafe(block, "Posologie");
            ordonnance.instructions = extractFieldValueSafe(block, "Instructions");

            if (ordonnance.isValid()) {
                currentAnalysis.ordonnances.add(ordonnance);
                System.out.println("Ordonnance trouvée: " + ordonnance.medicament);
            }
        }
    }

    /**
     * Extrait la valeur d'un champ de manière sécurisée et intelligente.
     * Capable de capturer du texte multi-ligne jusqu'au prochain label.
     */
    private String extractFieldValueSafe(String text, String fieldName) {
        String[] knownLabels = {
                "Nom du fichier", "Type de dossier", "Date de création", "Description",
                "Motif", "Diagnostic", "Observations",
                "Médicament", "Posologie", "Instructions",
                "Rapports Médicaux", "Ordonnances", "Page"
        };

        // On cherche le début du champ
        String patternStr = "(?i)" + fieldName + "\\s*:\\s*(.*)";
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String fullRemaining = matcher.group(1).trim();

            // On doit trouver où s'arrêter : au prochain label ou à la fin
            int earliestStop = fullRemaining.length();

            for (String label : knownLabels) {
                if (label.equalsIgnoreCase(fieldName))
                    continue;

                // Recherche du label suivi de ':' pour éviter les faux positifs dans le texte
                Pattern labelPattern = Pattern.compile("(?i)\\n?\\s*" + label + "\\s*:");
                Matcher labelMatcher = labelPattern.matcher(fullRemaining);
                if (labelMatcher.find()) {
                    earliestStop = Math.min(earliestStop, labelMatcher.start());
                }
            }

            return fullRemaining.substring(0, earliestStop).trim();
        }

        return "";
    }

    // ==================== UTILITAIRES UI ====================

    private void updateBadge(String text, String bgColor, String textColor) {
        Platform.runLater(() -> {
            modeBadge.setText(text);
            modeBadge.setStyle("-fx-background-color: " + bgColor +
                    "; -fx-padding: 4 8; -fx-background-radius: 6; " +
                    "-fx-font-weight: bold; -fx-font-size: 10;");
            modeBadge.setTextFill(javafx.scene.paint.Color.web(textColor));
        });
    }

    // ==================== GÉNÉRATION DE RÉPONSES ====================

    /**
     * Génère une réponse via l'API LLM en utilisant le contexte disponible
     */
    private String generateResponse(String input) throws Exception {
        input = input.toLowerCase().trim();
        String context = "";
        String mode = "";

        // 1. DÉFINITION DU CONTEXTE PRIORITAIRE
        if (uploadedFile != null && !extractedPdfText.isEmpty()) {
            context = "CONTENU DU PDF ANALYSÉ :\n" + extractedPdfText;
            mode = "ANALYSE PDF (API)";
            updateBadge("MODE : " + mode, "#f0fdf4", "#10b981");
        } else if (!dbContextText.isEmpty()) {
            context = "CONTENU DU DOSSIER PATIENT (BASE DE DONNÉES) :\n" + dbContextText;
            mode = "ANALYSE DOSSIER (API)";
            updateBadge("MODE : " + mode, "#eff6ff", "#2563eb");
        } else {
            context = "Aucun document ou historique patient n'est chargé.";
            mode = "IA GÉNÉRALE (API)";
            updateBadge("MODE : " + mode, "#f8fafc", "#64748b");
        }

        // Appel à l'API Gemini
        return aiApiService.getAiResponse(input, context);
    }

    /**
     * Répond aux questions basées sur le contenu du PDF analysé avec une logique
     * sémantique.
     */
    private String answerFromPdf(String input) {
        String intent = identifyIntent(input);
        StringBuilder sb = new StringBuilder();
        sb.append("### 🤖 Assistant TabibNet - Analyse PDF\n\n");

        switch (intent) {
            case "IDENTITY":
                return sb.append("Ce document est un **").append(currentAnalysis.documentType).append("**.\n")
                        .append("- **Fichier** : ").append(currentAnalysis.documentInfo.nomFichier).append("\n")
                        .append("- **Date** : ").append(currentAnalysis.documentInfo.dateCreation).toString();

            case "COUNT":
                return sb.append("J'ai détecté :\n")
                        .append("- **").append(currentAnalysis.getRapportCount()).append("** rapport(s) médical(aux)\n")
                        .append("- **").append(currentAnalysis.getOrdonnanceCount())
                        .append("** ordonnance(s) de soins.").toString();

            case "DIAGNOSTIC":
                return formatSpecificResults(sb, "diagnostic", "Diagnostics trouvés");

            case "TREATMENT":
                return formatSpecificResults(sb, "médicament", "Traitements et Médicaments");

            case "REASON":
                return formatSpecificResults(sb, "motif", "Motifs de consultation");

            case "SUMMARY":
                return generateFullSummary(sb);

            case "SPECIFIC_ITEM":
                return handleSpecificItem(input, sb);

            default:
                // Recherche sémantique par mot-clé si aucun intent n'est clair
                return semanticSearchInPdf(input, sb);
        }
    }

    private String identifyIntent(String input) {
        if (input.contains("qui es-tu") || input.contains("quel document") || input.contains("c'est quoi"))
            return "IDENTITY";
        if (input.contains("combien") || input.contains("nombre") || input.contains("total"))
            return "COUNT";
        if (input.contains("diagnostic") || input.contains("maladie") || input.contains("souffre")
                || input.contains("problème"))
            return "DIAGNOSTIC";
        if (input.contains("traitement") || input.contains("médicament") || input.contains("prendre")
                || input.contains("ordonnance") || input.contains("soin"))
            return "TREATMENT";
        if (input.contains("motif") || input.contains("pourquoi") || input.contains("raison"))
            return "REASON";
        if (input.contains("résume") || input.contains("résumé") || input.contains("tout") || input.contains("contenu")
                || input.contains("synthèse"))
            return "SUMMARY";
        if (Pattern.compile("(rapport|ordonnance)\\s*(\\d+)").matcher(input).find())
            return "SPECIFIC_ITEM";
        return "UNKNOWN";
    }

    private String formatSpecificResults(StringBuilder sb, String key, String title) {
        sb.append("#### 📋 ").append(title).append("\n\n");
        boolean found = false;

        // Rapports
        for (int i = 0; i < currentAnalysis.rapports.size(); i++) {
            ParsedRapport r = currentAnalysis.rapports.get(i);
            String val = switch (key) {
                case "motif" -> r.motif;
                case "diagnostic" -> r.diagnostic;
                default -> "";
            };
            if (!val.isEmpty()) {
                sb.append("- **Rapport ").append(i + 1).append("** : ").append(val).append("\n");
                found = true;
            }
        }

        // Ordonnances
        for (int i = 0; i < currentAnalysis.ordonnances.size(); i++) {
            ParsedOrdonnance o = currentAnalysis.ordonnances.get(i);
            String val = switch (key) {
                case "médicament" -> o.medicament;
                default -> "";
            };
            if (!val.isEmpty()) {
                sb.append("- **Ordonnance ").append(i + 1).append("** : ").append(val).append("\n");
                found = true;
            }
        }

        if (!found)
            return sb.append("Je n'ai trouvé aucune information explicite concernant les ").append(title.toLowerCase())
                    .append(" dans ce document.").toString();
        return sb.toString();
    }

    private String handleSpecificItem(String input, StringBuilder sb) {
        Matcher m = Pattern.compile("(rapport|ordonnance)\\s*(\\d+)").matcher(input);
        if (m.find()) {
            int idx = Integer.parseInt(m.group(2)) - 1;
            String type = m.group(1);
            if (type.contains("rapport") && idx >= 0 && idx < currentAnalysis.rapports.size()) {
                ParsedRapport r = currentAnalysis.rapports.get(idx);
                return sb.append("#### 🩺 Détails du Rapport n°").append(idx + 1).append("\n")
                        .append("- **Motif** : ").append(r.motif).append("\n")
                        .append("- **Diagnostic** : ").append(r.diagnostic).append("\n")
                        .append("- **Observations** : ").append(r.observations).toString();
            }
            if (type.contains("ordonnance") && idx >= 0 && idx < currentAnalysis.ordonnances.size()) {
                ParsedOrdonnance o = currentAnalysis.ordonnances.get(idx);
                return sb.append("#### 💊 Détails de l'Ordonnance n°").append(idx + 1).append("\n")
                        .append("- **Médicament** : ").append(o.medicament).append("\n")
                        .append("- **Posologie** : ").append(o.posologie).append("\n")
                        .append("- **Instructions** : ").append(o.instructions).toString();
            }
        }
        return sb.append("Désolé, je ne trouve pas cet élément spécifique dans le document.").toString();
    }

    private String generateFullSummary(StringBuilder sb) {
        sb.append("#### 📑 Synthèse Globale du Dossier\n\n")
                .append("D'après mon analyse, ce dossier contient ")
                .append(currentAnalysis.getRapportCount()).append(" rapport(s) et ")
                .append(currentAnalysis.getOrdonnanceCount()).append(" ordonnance(s).\n\n");

        if (!currentAnalysis.rapports.isEmpty()) {
            sb.append("**Résumé Clinique :**\n");
            for (ParsedRapport r : currentAnalysis.rapports) {
                sb.append("- Patient venu pour : *").append(r.motif).append("*. Diagnostic : **").append(r.diagnostic)
                        .append("**.\n");
            }
        }

        if (!currentAnalysis.ordonnances.isEmpty()) {
            sb.append("\n**Plan de Traitement :**\n");
            for (ParsedOrdonnance o : currentAnalysis.ordonnances) {
                sb.append("- Prescription de **").append(o.medicament).append("** (").append(o.posologie)
                        .append(").\n");
            }
        }

        return sb.toString();
    }

    private String semanticSearchInPdf(String input, StringBuilder sb) {
        // Recherche brute dans tous les champs si aucun intent n'est détecté
        sb.append(
                "Je n'ai pas trouvé de section spécifique pour votre demande, mais voici les mentions trouvées pour '")
                .append(input).append("' :\n\n");
        boolean found = false;

        for (ParsedRapport r : currentAnalysis.rapports) {
            if (r.motif.toLowerCase().contains(input) || r.diagnostic.toLowerCase().contains(input)
                    || r.observations.toLowerCase().contains(input)) {
                sb.append("- **Dans les rapports** : \"...").append(r.diagnostic).append("...\"\n");
                found = true;
            }
        }

        for (ParsedOrdonnance o : currentAnalysis.ordonnances) {
            if (o.medicament.toLowerCase().contains(input) || o.posologie.toLowerCase().contains(input)
                    || o.instructions.toLowerCase().contains(input)) {
                sb.append("- **Dans les ordonnances** : ").append(o.medicament).append(" (").append(o.posologie)
                        .append(")\n");
                found = true;
            }
        }

        if (!found)
            return "### 🤖 Assistant TabibNet\n\nJe n'ai trouvé aucune information concernant '" + input
                    + "' dans ce document. Pouvez-vous préciser votre question ?";
        return sb.toString();
    }

    // ==================== RECHERCHE EN BASE DE DONNÉES ====================

    private String searchInContext(String input, String context, String title) {
        StringBuilder response = new StringBuilder();
        response.append("### ").append(title).append("\n\n");

        if (input.contains("diagnostic") || input.contains("maladie")) {
            String diagnostic = findInText("Diagnostic", context);
            if (!diagnostic.isEmpty()) {
                return response.append("Diagnostic trouvé : **")
                        .append(diagnostic).append("**.").toString();
            }
        }

        if (input.contains("médicament") || input.contains("traitement") || input.contains("prendre")) {
            String medications = findInText("Médicament", context);
            if (medications.isEmpty())
                medications = findInText("Prescription", context);
            if (!medications.isEmpty()) {
                return response.append("Médicaments : **")
                        .append(medications).append("**.").toString();
            }
        }

        if (context.toLowerCase().contains(input)) {
            return response.append("Mention trouvée :\n\n> ...")
                    .append(getContextSnippet(input, context)).append("...").toString();
        }

        return null;
    }

    private String findInText(String key, String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains(key.toLowerCase() + " :")) {
                String[] parts = line.split(":", 2);
                return parts.length > 1 ? parts[1].trim() : "";
            }
        }
        return "";
    }

    private String getContextSnippet(String keyword, String text) {
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1)
            return "";
        int start = Math.max(0, index - 40);
        int end = Math.min(text.length(), index + keyword.length() + 40);
        return text.substring(start, end).replace("\n", " ");
    }
}