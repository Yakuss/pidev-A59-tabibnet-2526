package com.pidev.controllers;

import com.pidev.models.Question;
import com.pidev.models.Reponse;
import com.pidev.models.Specialite;
import com.pidev.services.QuestionService;
import com.pidev.services.ReponseService;
import com.pidev.services.SpecialiteService;
import com.pidev.services.GeminiAIService;
import com.pidev.utils.TTSService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Forum Controller — community-style question/answer board with card-based layout.
 * Inspired by modern forum designs with specialty filtering and rich card UI.
 */
public class ForumController {

    @FXML private VBox specialitySidebar;
    @FXML private VBox questionsContainer;
    @FXML private TextField searchField;
    @FXML private Label lblQuestionCount, lblAnswerCount, lblStatus;
    @FXML private ScrollPane scrollPane;

    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private final SpecialiteService specialiteService = new SpecialiteService();

    private ObservableList<Question> allQuestions = FXCollections.observableArrayList();
    private List<Specialite> specialites;
    private int selectedSpecialiteId = -1; // -1 = All
    private Button activeFilterBtn = null;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

    // Color palette for category badges — matches new design system
    private static final String[] BADGE_COLORS = {
            "#5b6ef5", "#22c55e", "#f59e0b", "#f43f5e", "#06b6d4",
            "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#a78bfa"
    };

    @FXML
    public void initialize() {
        loadSpecialites();
        loadQuestions();
        updateStats();

        // Live search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterQuestions());

        showStatus("Forum communautaire chargé", "#22c55e");
    }

    // ═══════════════════════════════════════════════
    //  DATA LOADING
    // ═══════════════════════════════════════════════

    private void loadSpecialites() {
        try {
            specialites = specialiteService.getAll();
            buildSpecialitySidebar();
        } catch (Exception e) {
            System.err.println("❌ Error loading specialites: " + e.getMessage());
        }
    }

    private void loadQuestions() {
        try {
            if (selectedSpecialiteId == -1) {
                allQuestions.setAll(questionService.getAll());
            } else {
                allQuestions.setAll(questionService.getBySpecialite(selectedSpecialiteId));
            }
            renderQuestionCards();
            updateStats();
        } catch (Exception e) {
            showStatus("Erreur de chargement des questions", "#f43f5e");
            System.err.println("❌ Error loading questions: " + e.getMessage());
        }
    }

    private void updateStats() {
        lblQuestionCount.setText(String.valueOf(allQuestions.size()));
        try {
            lblAnswerCount.setText(String.valueOf(reponseService.getAll().size()));
        } catch (Exception e) {
            lblAnswerCount.setText("—");
        }
    }

    // ═══════════════════════════════════════════════
    //  SPECIALITY SIDEBAR
    // ═══════════════════════════════════════════════

    private void buildSpecialitySidebar() {
        specialitySidebar.getChildren().clear();

        // "All" button
        Button btnAll = createFilterButton("🌐  Tous", -1);
        btnAll.getStyleClass().add("filter-btn-active");
        activeFilterBtn = btnAll;
        specialitySidebar.getChildren().add(btnAll);

        // Individual specialties
        if (specialites != null) {
            for (Specialite s : specialites) {
                Button btn = createFilterButton("✦  " + s.getNom(), s.getId());
                specialitySidebar.getChildren().add(btn);
            }
        }
    }

    private Button createFilterButton(String text, int specialiteId) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(getFilterBtnStyle(false));
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setOnMouseEntered(e -> {
            if (btn != activeFilterBtn) btn.setStyle(getFilterBtnHoverStyle());
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeFilterBtn) btn.setStyle(getFilterBtnStyle(false));
        });

        btn.setOnAction(e -> {
            if (activeFilterBtn != null) activeFilterBtn.setStyle(getFilterBtnStyle(false));
            activeFilterBtn = btn;
            btn.setStyle(getFilterBtnStyle(true));
            selectedSpecialiteId = specialiteId;
            loadQuestions();
            showStatus(specialiteId == -1 ? "🌐 Toutes les spécialités" :
                    "🔍 Filtre : " + text.replace("✦  ", ""), "#a78bfa");
        });

        return btn;
    }

    private String getFilterBtnStyle(boolean active) {
        if (active) {
            return "-fx-background-color: rgba(91,110,245,0.15); -fx-text-fill: #818cf8; " +
                   "-fx-font-weight: 700; -fx-padding: 9 14; -fx-background-radius: 8; " +
                   "-fx-border-color: #5b6ef5; -fx-border-radius: 0 8 8 0; -fx-border-width: 0 0 0 3; " +
                   "-fx-font-size: 13px;";
        }
        return "-fx-background-color: transparent; -fx-text-fill: #94a3b8; " +
               "-fx-padding: 9 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-border-width: 0;";
    }

    private String getFilterBtnHoverStyle() {
        return "-fx-background-color: #1c2133; -fx-text-fill: #f1f5f9; " +
               "-fx-padding: 9 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-border-width: 0;";
    }

    // ═══════════════════════════════════════════════
    //  QUESTION CARDS RENDERING
    // ═══════════════════════════════════════════════

    private void renderQuestionCards() {
        questionsContainer.getChildren().clear();

        if (allQuestions.isEmpty()) {
            VBox emptyState = new VBox(12);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(60, 0, 60, 0));
            Label emptyIcon = new Label("💬");
            emptyIcon.setStyle("-fx-font-size: 40px;");
            Label emptyMsg = new Label("Aucune question pour le moment");
            emptyMsg.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 16px; -fx-font-weight: 600;");
            Label emptySub = new Label("Soyez le premier à poser une question !");
            emptySub.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
            emptyState.getChildren().addAll(emptyIcon, emptyMsg, emptySub);
            questionsContainer.getChildren().add(emptyState);
            return;
        }

        for (Question q : allQuestions) {
            VBox card = createQuestionCard(q);
            questionsContainer.getChildren().add(card);

            // Fade-in animation
            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    private VBox createQuestionCard(Question q) {
        // ── Outer card wrapper (left accent border via HBox trick) ──
        VBox card = new VBox(0);
        card.setMaxWidth(Double.MAX_VALUE);

        String accentColor = getBadgeColor(q.getSpecialiteId());

        String cardBase = "-fx-background-color: #0e1220;" +
                          "-fx-background-radius: 10;" +
                          "-fx-border-radius: 10;" +
                          "-fx-border-color: #252d42;" +
                          "-fx-border-width: 1;" +
                          "-fx-cursor: hand;";
        String cardHover = "-fx-background-color: #141826;" +
                           "-fx-background-radius: 10;" +
                           "-fx-border-radius: 10;" +
                           "-fx-border-color: " + accentColor + "66;" +
                           "-fx-border-width: 1;" +
                           "-fx-cursor: hand;";

        card.setStyle(cardBase);
        card.setOnMouseEntered(e -> card.setStyle(cardHover));
        card.setOnMouseExited(e  -> card.setStyle(cardBase));

        // ── Top row: badge + date ──
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(14, 18, 0, 18));

        // Specialty badge pill
        String specName = (q.getSpecialiteNom() != null && !q.getSpecialiteNom().isEmpty())
                ? q.getSpecialiteNom() : "Général";
        Label badge = new Label(specName.toUpperCase());
        badge.setStyle("-fx-background-color: " + accentColor + "22;" +
                       "-fx-text-fill: " + accentColor + ";" +
                       "-fx-font-size: 10px; -fx-font-weight: 700;" +
                       "-fx-padding: 3 10; -fx-background-radius: 20;" +
                       "-fx-border-color: " + accentColor + "55;" +
                       "-fx-border-width: 1; -fx-border-radius: 20;");

        // Status dot
        String statusColor = "open".equalsIgnoreCase(q.getStatus()) ? "#22c55e" : "#f43f5e";
        Label statusDot = new Label();
        statusDot.setStyle("-fx-background-color: " + statusColor + ";" +
                           "-fx-background-radius: 50; -fx-min-width: 7;" +
                           "-fx-min-height: 7; -fx-max-width: 7; -fx-max-height: 7;");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        // Date
        String dateStr = q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FMT) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");

        topRow.getChildren().addAll(badge, statusDot, topSpacer, dateLabel);

        // ── Body: title + description ──
        VBox body = new VBox(6);
        body.setPadding(new Insets(10, 18, 12, 18));

        Label title = new Label(q.getTitre() != null ? q.getTitre() : "Sans titre");
        title.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 15px; -fx-font-weight: 700;");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);

        String desc = q.getDescription() != null ? q.getDescription() : "";
        if (desc.length() > 160) desc = desc.substring(0, 160) + "…";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-line-spacing: 2;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(title, descLabel);
        
        // ── Display image if exists ──
        if (q.getImageName() != null && !q.getImageName().isEmpty()) {
            try {
                // Try multiple possible paths
                File imageFile = resolveImageFile(q.getImageName());
                
                if (imageFile != null && imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    
                    // Image container with rounded corners
                    VBox imageContainer = new VBox();
                    imageContainer.setStyle(
                        "-fx-background-color: #141826;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8;"
                    );
                    VBox.setMargin(imageContainer, new Insets(4, 0, 4, 0));
                    
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(460);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setStyle(
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);" +
                        "-fx-background-radius: 6;"
                    );
                    imageView.setCursor(javafx.scene.Cursor.HAND);
                    
                    // 📸 label
                    Label imgLabel = new Label("📸 Image jointe");
                    imgLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-padding: 0 0 4 0;");
                    
                    imageContainer.getChildren().addAll(imgLabel, imageView);
                    body.getChildren().add(imageContainer);
                    
                    // Click to view full size
                    imageView.setOnMouseClicked(evt -> {
                        evt.consume();
                        Stage imageStage = new Stage();
                        imageStage.setTitle("📸 " + q.getTitre());
                        
                        ImageView fullImageView = new ImageView(image);
                        fullImageView.setPreserveRatio(true);
                        fullImageView.setFitWidth(850);
                        fullImageView.setSmooth(true);
                        
                        ScrollPane sp = new ScrollPane(fullImageView);
                        sp.setStyle("-fx-background: #0e1220; -fx-background-color: #0e1220;");
                        sp.setFitToWidth(true);
                        
                        Scene scene = new Scene(sp, 900, 650);
                        scene.setFill(javafx.scene.paint.Color.web("#0e1220"));
                        imageStage.setScene(scene);
                        imageStage.show();
                    });
                } else {
                    System.out.println("⚠️ Image not found: " + q.getImageName());
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to load image: " + e.getMessage());
            }
        }

        // ── Divider ──
        Region divider = new Region();
        divider.setMaxHeight(1);
        divider.setMinHeight(1);
        divider.setStyle("-fx-background-color: #1c2133;");

        // ── Footer: author + stats + action ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 18, 12, 18));

        // Author avatar initials
        String authorName = q.getPatientName() != null ? q.getPatientName() : "Anonyme";
        String initials = authorName.length() >= 2
                ? String.valueOf(authorName.charAt(0)).toUpperCase()
                : "?";
        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(28, 28);
        avatarPane.setMaxSize(28, 28);
        Region avatarBg = new Region();
        avatarBg.setStyle("-fx-background-color: " + accentColor + "33;" +
                          "-fx-background-radius: 50;");
        Label avatarLbl = new Label(initials);
        avatarLbl.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        avatarPane.getChildren().addAll(avatarBg, avatarLbl);

        Label authorLabel = new Label(authorName);
        authorLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Answer count chip
        HBox answerChip = new HBox(5);
        answerChip.setAlignment(Pos.CENTER);
        answerChip.setStyle("-fx-background-color: rgba(91,110,245,0.1);" +
                            "-fx-background-radius: 20; -fx-padding: 4 10;");
        Label answerIcon = new Label("💬");
        answerIcon.setStyle("-fx-font-size: 11px;");
        Label answerCount = new Label(q.getAnswerCount() + " réponse" + (q.getAnswerCount() != 1 ? "s" : ""));
        answerCount.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: 600;");
        answerChip.getChildren().addAll(answerIcon, answerCount);

        // Voice button
        Button btnVoice = createVoiceButton(
                q.getTitre() + ". " + (q.getDescription() != null ? q.getDescription() : "")
        );
        
        // Translate button
        Button btnTranslate = new Button("🌐");
        String translateBase = "-fx-background-color: transparent;" +
                          "-fx-text-fill: #3b82f6; -fx-font-size: 12px;" +
                          "-fx-padding: 4 12; -fx-background-radius: 6;" +
                          "-fx-border-color: #252d42; -fx-border-width: 1;" +
                          "-fx-border-radius: 6; -fx-cursor: hand;";
        String translateHover = "-fx-background-color: #1c2133;" +
                           "-fx-text-fill: #60a5fa; -fx-font-size: 12px;" +
                           "-fx-padding: 4 12; -fx-background-radius: 6;" +
                           "-fx-border-color: #3b82f6; -fx-border-width: 1;" +
                           "-fx-border-radius: 6; -fx-cursor: hand;";
        btnTranslate.setStyle(translateBase);
        btnTranslate.setOnMouseEntered(e2 -> btnTranslate.setStyle(translateHover));
        btnTranslate.setOnMouseExited(e2  -> btnTranslate.setStyle(translateBase));
        btnTranslate.setTooltip(new Tooltip("Traduire"));
        
        btnTranslate.setOnAction(e -> {
            btnTranslate.setDisable(true);
            btnTranslate.setText("⏳");
            
            new Thread(() -> {
                try {
                    GeminiAIService aiService = new GeminiAIService();
                    
                    if (!aiService.isConfigured()) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Clé API non configurée!");
                            btnTranslate.setDisable(false);
                            btnTranslate.setText("🌐");
                        });
                        return;
                    }
                    
                    String translatedTitle = aiService.autoTranslate(q.getTitre());
                    String translatedDesc = aiService.autoTranslate(q.getDescription() != null ? q.getDescription() : "");
                    
                    Platform.runLater(() -> {
                        title.setText(translatedTitle);
                        String shortDesc = translatedDesc.length() > 160 ? translatedDesc.substring(0, 160) + "…" : translatedDesc;
                        descLabel.setText(shortDesc);
                        btnTranslate.setText("✓");
                        btnTranslate.setDisable(false);
                        showStatus("✓ Traduction terminée", "#22c55e");
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        btnTranslate.setDisable(false);
                        btnTranslate.setText("🌐");
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Échec de la traduction: " + ex.getMessage());
                    });
                }
            }).start();
        });

        // Edit button
        Button btnEdit = new Button("Éditer");
        String editBase = "-fx-background-color: transparent;" +
                          "-fx-text-fill: #475569; -fx-font-size: 12px;" +
                          "-fx-padding: 4 12; -fx-background-radius: 6;" +
                          "-fx-border-color: #252d42; -fx-border-width: 1;" +
                          "-fx-border-radius: 6; -fx-cursor: hand;";
        String editHover = "-fx-background-color: #1c2133;" +
                           "-fx-text-fill: #94a3b8; -fx-font-size: 12px;" +
                           "-fx-padding: 4 12; -fx-background-radius: 6;" +
                           "-fx-border-color: #2e3a52; -fx-border-width: 1;" +
                           "-fx-border-radius: 6; -fx-cursor: hand;";
        btnEdit.setStyle(editBase);
        btnEdit.setOnMouseEntered(e2 -> btnEdit.setStyle(editHover));
        btnEdit.setOnMouseExited(e2  -> btnEdit.setStyle(editBase));
        btnEdit.setOnAction(e -> openEditDialog(q));

        footer.getChildren().addAll(avatarPane, authorLabel, footerSpacer, answerChip, btnVoice, btnTranslate, btnEdit);

        card.getChildren().addAll(topRow, body, divider, footer);

        // Fade-in handled by caller; click to open detail
        card.setOnMouseClicked(e -> {
            if (e.getTarget() != btnEdit && !btnEdit.isHover()) openDetailDialog(q);
        });

        return card;
    }

    private String getBadgeColor(int specialiteId) {
        if (specialiteId <= 0) return BADGE_COLORS[0];
        return BADGE_COLORS[specialiteId % BADGE_COLORS.length];
    }

    /**
     * Resolve image file from multiple possible paths
     */
    private File resolveImageFile(String imageName) {
        String[] possiblePaths = {
            "uploads/forum_images/" + imageName,
            "pijava--main/uploads/forum_images/" + imageName,
            System.getProperty("user.dir") + "/uploads/forum_images/" + imageName,
            System.getProperty("user.dir") + "/pijava--main/uploads/forum_images/" + imageName,
            "../uploads/forum_images/" + imageName,
        };

        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                System.out.println("✅ Image found at: " + f.getAbsolutePath());
                return f;
            }
        }

        // Log all tried paths for debugging
        System.out.println("⚠️ Image '" + imageName + "' not found. Tried:");
        for (String path : possiblePaths) {
            System.out.println("   - " + new File(path).getAbsolutePath());
        }
        return null;
    }

    // ═══════════════════════════════════════════════
    //  SEARCH & FILTER
    // ═══════════════════════════════════════════════

    private void filterQuestions() {
        String query = searchField.getText().toLowerCase().trim();
        questionsContainer.getChildren().clear();

        if (query.isEmpty()) {
            renderQuestionCards();
            return;
        }

        List<Question> filtered = allQuestions.filtered(q ->
                (q.getTitre() != null && q.getTitre().toLowerCase().contains(query)) ||
                (q.getDescription() != null && q.getDescription().toLowerCase().contains(query)) ||
                (q.getPatientName() != null && q.getPatientName().toLowerCase().contains(query)) ||
                (q.getSpecialiteNom() != null && q.getSpecialiteNom().toLowerCase().contains(query))
        );

        if (filtered.isEmpty()) {
            Label empty = new Label("Aucun résultat pour « " + query + " »");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-font-style: italic; -fx-padding: 40;");
            questionsContainer.getChildren().add(empty);
        } else {
            for (Question q : filtered) {
                questionsContainer.getChildren().add(createQuestionCard(q));
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  DIALOGS
    // ═══════════════════════════════════════════════

    @FXML
    public void openAddDialog() {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Question");
        dialog.setHeaderText("Poser une nouvelle question");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #0e1220;");
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20, 24, 8, 24));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: #0e1220;");

        TextField tfTitre = new TextField();
        tfTitre.setPromptText("Titre de votre question…");
        tfTitre.setStyle(getInputStyle());

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Décrivez votre question en détail…");
        taDesc.setPrefRowCount(5);
        taDesc.setStyle(getInputStyle());
        taDesc.setWrapText(true);

        ComboBox<Specialite> cbSpec = new ComboBox<>();
        cbSpec.setPromptText("Choisir une spécialité");
        cbSpec.setMaxWidth(Double.MAX_VALUE);
        cbSpec.setStyle(getInputStyle());
        if (specialites != null) cbSpec.setItems(FXCollections.observableArrayList(specialites));

        // ═══════════════════════════════════════════════════════════════
        // 📸 IMAGE UPLOAD SECTION
        // ═══════════════════════════════════════════════════════════════
        Label imageLabel = createFormLabel("📸 Image médicale (optionnel)");
        
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        
        Label selectedImageLabel = new Label("Aucune image sélectionnée");
        selectedImageLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        
        Button chooseImageBtn = new Button("Choisir une image");
        chooseImageBtn.setStyle(
            "-fx-background-color: #8b5cf6;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        File[] selectedImage = new File[1];
        
        chooseImageBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Choisir une image médicale");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                long fileSizeInMB = file.length() / (1024 * 1024);
                if (fileSizeInMB > 5) {
                    showAlert(Alert.AlertType.WARNING, "Attention", 
                        "L'image est trop grande! Maximum 5 MB.");
                    return;
                }
                
                selectedImage[0] = file;
                selectedImageLabel.setText("✓ " + file.getName());
                selectedImageLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 12px; -fx-font-weight: 600;");
            }
        });
        
        imageBox.getChildren().addAll(chooseImageBtn, selectedImageLabel);
        
        // ═══════════════════════════════════════════════════════════════
        // ✨ AI ANALYSIS BUTTON
        // ═══════════════════════════════════════════════════════════════
        Button analyzeImageBtn = new Button("✨ Générer titre et description automatiquement");
        analyzeImageBtn.setStyle(
            "-fx-background-color: #f59e0b;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: 600;"
        );
        
        analyzeImageBtn.setOnAction(e -> {
            if (selectedImage[0] == null) {
                showAlert(Alert.AlertType.WARNING, "Attention", 
                    "Veuillez d'abord choisir une image!");
                return;
            }
            
            analyzeImageBtn.setDisable(true);
            analyzeImageBtn.setText("⏳ Analyse en cours...");
            
            new Thread(() -> {
                try {
                    com.pidev.services.GeminiAIService aiService = new com.pidev.services.GeminiAIService();
                    
                    if (!aiService.isConfigured()) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Clé API non configurée! Veuillez ajouter votre clé Gemini API dans GeminiAIService.java");
                            analyzeImageBtn.setDisable(false);
                            analyzeImageBtn.setText("✨ Générer titre et description automatiquement");
                        });
                        return;
                    }
                    
                    org.json.JSONObject result = aiService.analyzeImage(selectedImage[0]);
                    
                    javafx.application.Platform.runLater(() -> {
                        tfTitre.setText(result.getString("title"));
                        taDesc.setText(result.getString("description"));
                        
                        analyzeImageBtn.setDisable(false);
                        analyzeImageBtn.setText("✓ Analyse terminée");
                        analyzeImageBtn.setStyle(
                            "-fx-background-color: #22c55e;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 8 16;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-weight: 600;"
                        );
                        
                        showStatus("✓ Titre et description générés automatiquement", "#22c55e");
                    });
                    
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        analyzeImageBtn.setDisable(false);
                        analyzeImageBtn.setText("✨ Générer titre et description automatiquement");
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Échec de l'analyse: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                }
            }).start();
        });

        content.getChildren().addAll(
                createFormLabel("Titre *"), tfTitre,
                createFormLabel("Description"), taDesc,
                createFormLabel("Spécialité"), cbSpec,
                imageLabel, imageBox, analyzeImageBtn
        );

        pane.setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (tfTitre.getText().trim().isEmpty()) return null;
                
                // Save image if selected
                String imageName = null;
                if (selectedImage[0] != null) {
                    try {
                        // Use absolute path based on working directory
                        String uploadDirPath = System.getProperty("user.dir") + "/uploads/forum_images";
                        // Also try pijava--main subfolder
                        File uploadDir = new File(uploadDirPath);
                        if (!uploadDir.exists()) {
                            uploadDirPath = System.getProperty("user.dir") + "/pijava--main/uploads/forum_images";
                            uploadDir = new File(uploadDirPath);
                        }
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                            System.out.println("✅ Created directory: " + uploadDir.getAbsolutePath());
                        }
                        
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String extension = selectedImage[0].getName()
                            .substring(selectedImage[0].getName().lastIndexOf("."));
                        imageName = "question_" + timestamp + extension;
                        
                        java.io.File destFile = new java.io.File(uploadDir, imageName);
                        java.nio.file.Files.copy(selectedImage[0].toPath(), destFile.toPath(), 
                                   java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        System.out.println("✅ Image saved: " + imageName);
                        
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Échec de la sauvegarde de l'image: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                
                Question q = new Question();
                q.setTitre(tfTitre.getText().trim());
                q.setDescription(taDesc.getText().trim());
                q.setStatus("open");
                q.setImageName(imageName);
                if (cbSpec.getValue() != null) q.setSpecialiteId(cbSpec.getValue().getId());
                com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
                if (user != null) q.setPatientId(user.getId());
                return q;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(q -> {
            try {
                questionService.add(q);
                loadQuestions();
                showStatus("Question publiée avec succès", "#22c55e");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de publier: " + e.getMessage());
            }
        });
    }

    private void openEditDialog(Question q) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Modifier la question");
        dialog.setHeaderText("Modifier « " + q.getTitre() + " »");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #0e1220;");
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        // No delete button for questions — only edit is allowed

        VBox content = new VBox(14);
        content.setPadding(new Insets(20, 24, 8, 24));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: #0e1220;");

        TextField tfTitre = new TextField(q.getTitre());
        tfTitre.setStyle(getInputStyle());

        TextArea taDesc = new TextArea(q.getDescription());
        taDesc.setPrefRowCount(5);
        taDesc.setStyle(getInputStyle());
        taDesc.setWrapText(true);

        ComboBox<Specialite> cbSpec = new ComboBox<>();
        cbSpec.setMaxWidth(Double.MAX_VALUE);
        cbSpec.setStyle(getInputStyle());
        if (specialites != null) {
            cbSpec.setItems(FXCollections.observableArrayList(specialites));
            for (Specialite s : specialites) {
                if (s.getId() == q.getSpecialiteId()) { cbSpec.setValue(s); break; }
            }
        }

        content.getChildren().addAll(
                createFormLabel("Titre"), tfTitre,
                createFormLabel("Description"), taDesc,
                createFormLabel("Spécialité"), cbSpec
        );

        pane.setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                q.setTitre(tfTitre.getText().trim());
                q.setDescription(taDesc.getText().trim());
                if (cbSpec.getValue() != null) q.setSpecialiteId(cbSpec.getValue().getId());
                return q;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(edited -> {
            try {
                questionService.update(edited);
                loadQuestions();
                showStatus("Question modifiée avec succès", "#22c55e");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    private void openDetailDialog(Question q) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(q.getTitre());
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.setStyle("-fx-background-color: #080b14; -fx-padding: 0;");
        pane.setPrefWidth(640);
        pane.setPrefHeight(680);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #080b14;");

        // ── Hero header ──
        String accentColor = getBadgeColor(q.getSpecialiteId());
        VBox hero = new VBox(12);
        hero.setPadding(new Insets(28, 28, 24, 28));
        hero.setStyle("-fx-background-color: #0e1220;" +
                      "-fx-border-color: #252d42; -fx-border-width: 0 0 1 0;");

        // Specialty badge
        String specName = (q.getSpecialiteNom() != null && !q.getSpecialiteNom().isEmpty())
                ? q.getSpecialiteNom().toUpperCase() : "GÉNÉRAL";
        Label specTag = new Label(specName);
        specTag.setStyle("-fx-background-color: " + accentColor + "22;" +
                         "-fx-text-fill: " + accentColor + ";" +
                         "-fx-font-size: 10px; -fx-font-weight: 700;" +
                         "-fx-padding: 3 12; -fx-background-radius: 20;" +
                         "-fx-border-color: " + accentColor + "55;" +
                         "-fx-border-width: 1; -fx-border-radius: 20;");

        Label titleLabel = new Label(q.getTitre());
        titleLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 20px; -fx-font-weight: 700;");
        titleLabel.setWrapText(true);

        HBox metaBox = new HBox(16);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        Label authorLabel = new Label("Par " + (q.getPatientName() != null ? q.getPatientName() : "Anonyme"));
        authorLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        Label dateLabel = new Label(q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FMT) : "");
        dateLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        metaBox.getChildren().addAll(authorLabel, dateLabel);

        hero.getChildren().addAll(specTag, titleLabel, metaBox);

        // ── Scrollable content ──
        ScrollPane sp = new ScrollPane();
        sp.setStyle("-fx-background-color: transparent; -fx-background: #080b14; -fx-border-color: transparent;");
        sp.setFitToWidth(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox content = new VBox(24);
        content.setPadding(new Insets(24, 28, 24, 28));
        content.setStyle("-fx-background-color: #080b14;");

        // Question body + voice button
        HBox descRow = new HBox(10);
        descRow.setAlignment(Pos.TOP_RIGHT);

        Label descLabel = new Label(q.getDescription() != null ? q.getDescription() : "");
        descLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-line-spacing: 4;");
        descLabel.setWrapText(true);
        HBox.setHgrow(descLabel, Priority.ALWAYS);

        Button btnVoiceQuestion = createVoiceButton(
                q.getTitre() + ". " + (q.getDescription() != null ? q.getDescription() : "")
        );

        descRow.getChildren().addAll(descLabel, btnVoiceQuestion);
        
        // ── Display image in detail view if exists ──
        VBox imageContainer = null;
        if (q.getImageName() != null && !q.getImageName().isEmpty()) {
            try {
                File imageFile = new File("uploads/forum_images/" + q.getImageName());
                if (imageFile.exists()) {
                    Label imageLabel = new Label("📸 Image médicale:");
                    imageLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-weight: 600;");
                    
                    Image image = new Image(imageFile.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(580);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle(
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);"
                    );
                    imageView.setCursor(javafx.scene.Cursor.HAND);
                    
                    imageContainer = new VBox(10);
                    imageContainer.getChildren().addAll(imageLabel, imageView);
                    VBox.setMargin(imageContainer, new Insets(10, 0, 10, 0));
                    
                    // Click to view full size
                    imageView.setOnMouseClicked(evt -> {
                        Stage imageStage = new Stage();
                        imageStage.setTitle("Image - " + q.getTitre());
                        
                        ImageView fullImageView = new ImageView(image);
                        fullImageView.setPreserveRatio(true);
                        fullImageView.setFitWidth(900);
                        
                        ScrollPane scrollPane = new ScrollPane(fullImageView);
                        scrollPane.setStyle("-fx-background: #0e1220;");
                        
                        Scene scene = new Scene(scrollPane, 950, 700);
                        imageStage.setScene(scene);
                        imageStage.show();
                    });
                }
            } catch (Exception e) {
                System.err.println("Failed to load image in detail view: " + e.getMessage());
            }
        }
        
        // ── Translate button for question in detail view ──
        Button btnTranslateDetail = new Button("🌐 Traduire la question");
        btnTranslateDetail.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;"
        );
        btnTranslateDetail.setOnMouseEntered(e -> btnTranslateDetail.setStyle(
            "-fx-background-color: #60a5fa;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;"
        ));
        btnTranslateDetail.setOnMouseExited(e -> btnTranslateDetail.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;"
        ));
        
        btnTranslateDetail.setOnAction(e -> {
            btnTranslateDetail.setDisable(true);
            btnTranslateDetail.setText("⏳ Traduction en cours...");
            
            new Thread(() -> {
                try {
                    GeminiAIService aiService = new GeminiAIService();
                    
                    if (!aiService.isConfigured()) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Clé API non configurée!");
                            btnTranslateDetail.setDisable(false);
                            btnTranslateDetail.setText("🌐 Traduire la question");
                        });
                        return;
                    }
                    
                    String translatedTitle = aiService.autoTranslate(q.getTitre());
                    String translatedDesc = aiService.autoTranslate(q.getDescription() != null ? q.getDescription() : "");
                    
                    Platform.runLater(() -> {
                        titleLabel.setText(translatedTitle);
                        descLabel.setText(translatedDesc);
                        btnTranslateDetail.setText("✓ Traduit");
                        btnTranslateDetail.setDisable(false);
                        showStatus("✓ Question traduite", "#22c55e");
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        btnTranslateDetail.setDisable(false);
                        btnTranslateDetail.setText("🌐 Traduire la question");
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Échec de la traduction: " + ex.getMessage());
                    });
                }
            }).start();
        });

        // Responses header
        HBox respHeader = new HBox(8);
        respHeader.setAlignment(Pos.CENTER_LEFT);
        Label respTitle = new Label("Réponses");
        respTitle.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 15px; -fx-font-weight: 700;");
        Label respCount = new Label("(" + q.getAnswerCount() + ")");
        respCount.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        respHeader.getChildren().addAll(respTitle, respCount);

        // Responses list
        VBox responsesBox = new VBox(10);
        try {
            List<Reponse> reponses = reponseService.getByQuestion(q.getId());
            if (reponses.isEmpty()) {
                Label msg = new Label("Aucune réponse pour le moment. Soyez le premier à répondre !");
                msg.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 16 0;");
                responsesBox.getChildren().add(msg);
            } else {
                for (Reponse r : reponses) {
                    responsesBox.getChildren().add(createResponseBubble(r));
                }
            }
        } catch (Exception e) {
            Label err = new Label("Impossible de charger les réponses.");
            err.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 13px;");
            responsesBox.getChildren().add(err);
        }

        // Divider
        Region div = new Region();
        div.setMinHeight(1); div.setMaxHeight(1);
        div.setStyle("-fx-background-color: #1c2133;");

        // Reply input
        VBox inputSection = new VBox(10);
        Label addRespMsg = new Label("Votre réponse");
        addRespMsg.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 14px; -fx-font-weight: 600;");

        TextArea taReponse = new TextArea();
        taReponse.setPromptText("Écrivez une réponse détaillée…");
        taReponse.setWrapText(true);
        taReponse.setPrefRowCount(4);
        taReponse.setStyle(getInputStyle());

        Button btnSubmit = new Button("Publier la réponse");
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setCursor(javafx.scene.Cursor.HAND);
        btnSubmit.setStyle("-fx-background-color: #5b6ef5; -fx-text-fill: white;" +
                           "-fx-font-weight: 700; -fx-padding: 11 0;" +
                           "-fx-background-radius: 8; -fx-font-size: 13px; -fx-border-width: 0;");
        btnSubmit.setOnMouseEntered(e -> btnSubmit.setStyle(
                "-fx-background-color: #818cf8; -fx-text-fill: white;" +
                "-fx-font-weight: 700; -fx-padding: 11 0;" +
                "-fx-background-radius: 8; -fx-font-size: 13px; -fx-border-width: 0;"));
        btnSubmit.setOnMouseExited(e -> btnSubmit.setStyle(
                "-fx-background-color: #5b6ef5; -fx-text-fill: white;" +
                "-fx-font-weight: 700; -fx-padding: 11 0;" +
                "-fx-background-radius: 8; -fx-font-size: 13px; -fx-border-width: 0;"));

        btnSubmit.setOnAction(e -> {
            if (taReponse.getText().trim().isEmpty()) return;
            try {
                Reponse r = new Reponse();
                r.setContenu(taReponse.getText().trim());
                r.setQuestionId(q.getId());
                com.pidev.models.BaseUser user = com.pidev.utils.UserSession.getInstance().getUser();
                if (user != null) r.setMedecinId(user.getId());
                reponseService.add(r);
                dialog.setResult(null);
                dialog.close();
                loadQuestions();
                showStatus("Réponse publiée avec succès", "#22c55e");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });

        inputSection.getChildren().addAll(addRespMsg, taReponse, btnSubmit);
        
        // Add all elements to content
        content.getChildren().add(descRow);
        content.getChildren().add(btnTranslateDetail);
        if (imageContainer != null) {
            content.getChildren().add(imageContainer);
        }
        content.getChildren().addAll(respHeader, responsesBox, div, inputSection);
        
        sp.setContent(content);

        // ── Bottom bar ──
        HBox actionBar = new HBox();
        actionBar.setPadding(new Insets(12, 24, 12, 24));
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.setStyle("-fx-background-color: #0e1220;" +
                           "-fx-border-color: #252d42; -fx-border-width: 1 0 0 0;");

        Button btnClose = new Button("Fermer");
        btnClose.setCursor(javafx.scene.Cursor.HAND);
        btnClose.setStyle("-fx-background-color: #1c2133; -fx-text-fill: #94a3b8;" +
                          "-fx-padding: 8 24; -fx-background-radius: 8;" +
                          "-fx-border-color: #252d42; -fx-border-radius: 8; -fx-border-width: 1;");

        pane.getButtonTypes().add(ButtonType.CLOSE);
        Button standardClose = (Button) pane.lookupButton(ButtonType.CLOSE);
        standardClose.setManaged(false);
        standardClose.setVisible(false);

        btnClose.setOnAction(e -> { dialog.setResult(null); dialog.close(); });
        actionBar.getChildren().add(btnClose);

        root.getChildren().addAll(hero, sp, actionBar);
        pane.setContent(root);
        VBox.setVgrow(sp, Priority.ALWAYS);

        dialog.showAndWait();
    }

    private VBox createResponseBubble(Reponse r) {
        VBox bubble = new VBox(8);
        bubble.setPadding(new Insets(14, 16, 14, 16));
        bubble.setStyle("-fx-background-color: #0e1220;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #252d42;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;");

        // ── Header row: avatar + name + date + actions ──
        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);

        // Avatar initials
        String medName = r.getMedecinName() != null ? r.getMedecinName() : "Expert";
        String initials = medName.length() >= 1 ? String.valueOf(medName.charAt(0)).toUpperCase() : "E";
        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(30, 30);
        avatarPane.setMaxSize(30, 30);
        Region avatarBg = new Region();
        avatarBg.setStyle("-fx-background-color: rgba(91,110,245,0.2); -fx-background-radius: 50;");
        Label avatarLbl = new Label(initials);
        avatarLbl.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 12px; -fx-font-weight: 700;");
        avatarPane.getChildren().addAll(avatarBg, avatarLbl);

        Label name = new Label(medName);
        name.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: 600; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(r.getCreatedAt() != null ? r.getCreatedAt().format(DATE_FMT) : "");
        date.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");

        // ── Voice button for response ──
        Button btnVoiceReponse = createVoiceButton(
                medName + " dit : " + r.getContenu()
        );
        
        // ── Translate button for response ──
        Button btnTranslateReponse = new Button("🌐");
        String translateBase = "-fx-background-color: transparent;" +
                          "-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: 600;" +
                          "-fx-padding: 3 10; -fx-background-radius: 6;" +
                          "-fx-border-color: rgba(59,130,246,0.3); -fx-border-width: 1;" +
                          "-fx-border-radius: 6; -fx-cursor: hand;";
        String translateHover = "-fx-background-color: rgba(59,130,246,0.15);" +
                           "-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: 600;" +
                           "-fx-padding: 3 10; -fx-background-radius: 6;" +
                           "-fx-border-color: rgba(59,130,246,0.5); -fx-border-width: 1;" +
                           "-fx-border-radius: 6; -fx-cursor: hand;";
        btnTranslateReponse.setStyle(translateBase);
        btnTranslateReponse.setOnMouseEntered(e -> btnTranslateReponse.setStyle(translateHover));
        btnTranslateReponse.setOnMouseExited(e  -> btnTranslateReponse.setStyle(translateBase));
        btnTranslateReponse.setTooltip(new Tooltip("Traduire"));

        // ── Edit button ──
        Button btnEditReponse = new Button("Modifier");        String editBase = "-fx-background-color: transparent;" +
                          "-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: 600;" +
                          "-fx-padding: 3 10; -fx-background-radius: 6;" +
                          "-fx-border-color: rgba(91,110,245,0.3); -fx-border-width: 1;" +
                          "-fx-border-radius: 6; -fx-cursor: hand;";
        String editHover = "-fx-background-color: rgba(91,110,245,0.15);" +
                           "-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: 600;" +
                           "-fx-padding: 3 10; -fx-background-radius: 6;" +
                           "-fx-border-color: rgba(91,110,245,0.5); -fx-border-width: 1;" +
                           "-fx-border-radius: 6; -fx-cursor: hand;";
        btnEditReponse.setStyle(editBase);
        btnEditReponse.setOnMouseEntered(e -> btnEditReponse.setStyle(editHover));
        btnEditReponse.setOnMouseExited(e  -> btnEditReponse.setStyle(editBase));
        btnEditReponse.setOnAction(e -> openEditReponseDialog(r, bubble));

        // ── Delete button ──
        Button btnDeleteReponse = new Button("Supprimer");
        String delBase = "-fx-background-color: transparent;" +
                         "-fx-text-fill: #f43f5e; -fx-font-size: 11px; -fx-font-weight: 600;" +
                         "-fx-padding: 3 10; -fx-background-radius: 6;" +
                         "-fx-border-color: rgba(244,63,94,0.3); -fx-border-width: 1;" +
                         "-fx-border-radius: 6; -fx-cursor: hand;";
        String delHover = "-fx-background-color: rgba(244,63,94,0.1);" +
                          "-fx-text-fill: #f43f5e; -fx-font-size: 11px; -fx-font-weight: 600;" +
                          "-fx-padding: 3 10; -fx-background-radius: 6;" +
                          "-fx-border-color: rgba(244,63,94,0.5); -fx-border-width: 1;" +
                          "-fx-border-radius: 6; -fx-cursor: hand;";
        btnDeleteReponse.setStyle(delBase);
        btnDeleteReponse.setOnMouseEntered(e -> btnDeleteReponse.setStyle(delHover));
        btnDeleteReponse.setOnMouseExited(e  -> btnDeleteReponse.setStyle(delBase));
        btnDeleteReponse.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer la réponse");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer cette réponse ?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        reponseService.delete(r.getId());
                        // Remove bubble from parent without closing dialog
                        if (bubble.getParent() instanceof VBox parent) {
                            parent.getChildren().remove(bubble);
                        }
                        updateStats();
                        showStatus("Réponse supprimée", "#f43f5e");
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                    }
                }
            });
        });

        head.getChildren().addAll(avatarPane, name, spacer, date, btnVoiceReponse, btnTranslateReponse, btnEditReponse, btnDeleteReponse);

        // ── Content ──
        Label text = new Label(r.getContenu());
        text.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-line-spacing: 3;");
        text.setWrapText(true);

        bubble.getChildren().addAll(head, text);
        
        // ── Translate button action (after text label is created) ──
        btnTranslateReponse.setOnAction(e -> {
            btnTranslateReponse.setDisable(true);
            btnTranslateReponse.setText("⏳");
            
            new Thread(() -> {
                try {
                    GeminiAIService aiService = new GeminiAIService();
                    
                    if (!aiService.isConfigured()) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Clé API non configurée!");
                            btnTranslateReponse.setDisable(false);
                            btnTranslateReponse.setText("🌐");
                        });
                        return;
                    }
                    
                    String translatedText = aiService.autoTranslate(r.getContenu());
                    
                    Platform.runLater(() -> {
                        text.setText(translatedText);
                        btnTranslateReponse.setText("✓");
                        btnTranslateReponse.setDisable(false);
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        btnTranslateReponse.setDisable(false);
                        btnTranslateReponse.setText("🌐");
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Échec de la traduction: " + ex.getMessage());
                    });
                }
            }).start();
        });
        
        return bubble;
    }

    private void openEditReponseDialog(Reponse r, VBox bubble) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifier la réponse");
        dialog.setHeaderText("Modifier votre réponse");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #0e1220;");
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 24, 8, 24));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: #0e1220;");

        TextArea taContenu = new TextArea(r.getContenu());
        taContenu.setPrefRowCount(6);
        taContenu.setStyle(getInputStyle());
        taContenu.setWrapText(true);

        content.getChildren().addAll(createFormLabel("Contenu"), taContenu);
        pane.setContent(content);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? taContenu.getText().trim() : null);

        dialog.showAndWait().ifPresent(newContenu -> {
            if (newContenu.isEmpty()) return;
            try {
                r.setContenu(newContenu);
                reponseService.update(r);
                // Update the text label inside the bubble in-place
                if (bubble.getChildren().size() >= 2 && bubble.getChildren().get(1) instanceof Label lbl) {
                    lbl.setText(newContenu);
                }
                showStatus("Réponse modifiée avec succès", "#22c55e");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    // ═══════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════

    // ═══════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════

    /**
     * Creates a styled voice/speaker toggle button that reads the given text aloud.
     * Shows ▶ when idle, ■ when speaking (for this specific text).
     */
    private Button createVoiceButton(String textToRead) {
        Button btn = new Button("🔊");
        String idleStyle =
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #475569; -fx-font-size: 13px;" +
                "-fx-padding: 3 8; -fx-background-radius: 6;" +
                "-fx-border-color: #252d42; -fx-border-width: 1;" +
                "-fx-border-radius: 6; -fx-cursor: hand;";
        String activeStyle =
                "-fx-background-color: rgba(91,110,245,0.15);" +
                "-fx-text-fill: #818cf8; -fx-font-size: 13px;" +
                "-fx-padding: 3 8; -fx-background-radius: 6;" +
                "-fx-border-color: rgba(91,110,245,0.5); -fx-border-width: 1;" +
                "-fx-border-radius: 6; -fx-cursor: hand;";
        String hoverStyle =
                "-fx-background-color: #1c2133;" +
                "-fx-text-fill: #94a3b8; -fx-font-size: 13px;" +
                "-fx-padding: 3 8; -fx-background-radius: 6;" +
                "-fx-border-color: #2e3a52; -fx-border-width: 1;" +
                "-fx-border-radius: 6; -fx-cursor: hand;";

        btn.setStyle(idleStyle);
        btn.setOnMouseEntered(e -> { if (!TTSService.getInstance().isSpeaking()) btn.setStyle(hoverStyle); });
        btn.setOnMouseExited(e  -> { if (!TTSService.getInstance().isSpeaking()) btn.setStyle(idleStyle); });

        btn.setOnAction(e -> {
            TTSService tts = TTSService.getInstance();
            if (tts.isSpeaking()) {
                // Stop and reset this button
                tts.stop();
                btn.setText("🔊");
                btn.setStyle(idleStyle);
            } else {
                // Start speaking — update button to "stop" state
                btn.setText("⏹");
                btn.setStyle(activeStyle);
                tts.setOnStop(() -> {
                    btn.setText("🔊");
                    btn.setStyle(idleStyle);
                });
                tts.speak(textToRead);
            }
        });

        return btn;
    }

    private Label createFormLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-font-size: 12px;");
        return lbl;
    }

    private String getInputStyle() {
        return "-fx-background-color: #1c2133; -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #475569; " +
               "-fx-border-color: #252d42; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 12; " +
               "-fx-font-size: 13px;";
    }

    private void showStatus(String message, String color) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), lblStatus);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
