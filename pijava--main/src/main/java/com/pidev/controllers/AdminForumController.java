package com.pidev.controllers;

import com.pidev.models.Question;
import com.pidev.services.QuestionService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin Forum Moderation Controller
 * Beautiful card-based view with bad word detection and delete functionality
 */
public class AdminForumController {

    @FXML private VBox questionsContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblBadWords;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;

    private final QuestionService questionService = new QuestionService();
    private List<Question> allQuestions;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

    private static final String[] BADGE_COLORS = {
        "#5b6ef5", "#22c55e", "#f59e0b", "#f43f5e", "#06b6d4",
        "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#a78bfa"
    };

    private static final Set<String> BAD_WORDS = Set.of(
        "fuck", "shit", "bitch", "ass", "damn", "crap", "bastard",
        "putain", "merde", "connard", "salope", "con", "enculé", "bordel",
        "idiot", "imbécile", "crétin", "abruti", "nul", "stupide",
        "كلب", "حمار", "غبي", "احمق", "لعنة", "زبالة"
    );

    @FXML
    public void initialize() {
        filterCombo.setItems(FXCollections.observableArrayList(
            "Toutes les questions",
            "🚫 Mots grossiers uniquement",
            "✅ Questions propres"
        ));
        filterCombo.setValue("Toutes les questions");
        filterCombo.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        loadQuestions();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DATA
    // ═══════════════════════════════════════════════════════════════════════

    private void loadQuestions() {
        // Show loading state
        questionsContainer.getChildren().clear();
        Label loading = new Label("⏳ Chargement des questions...");
        loading.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 40;");
        questionsContainer.getChildren().add(loading);

        new Thread(() -> {
            try {
                allQuestions = questionService.getAll();
                Platform.runLater(() -> {
                    updateStats();
                    applyFilter();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Erreur de chargement: " + e.getMessage()));
            }
        }).start();
    }

    private void updateStats() {
        if (allQuestions == null) return;
        long badCount = allQuestions.stream().filter(this::containsBadWord).count();
        lblTotal.setText(allQuestions.size() + " question" + (allQuestions.size() > 1 ? "s" : ""));
        lblBadWords.setText(badCount + " signalée" + (badCount > 1 ? "s" : ""));
        lblBadWords.setStyle(badCount > 0
            ? "-fx-text-fill: #f43f5e; -fx-font-size: 12px; -fx-font-weight: 700;"
            : "-fx-text-fill: #22c55e; -fx-font-size: 12px; -fx-font-weight: 700;");
    }

    private void applyFilter() {
        if (allQuestions == null) return;
        String search = searchField.getText().toLowerCase().trim();
        String filter = filterCombo.getValue();

        List<Question> filtered = allQuestions.stream().filter(q -> {
            boolean matchSearch = search.isEmpty()
                || (q.getTitre() != null && q.getTitre().toLowerCase().contains(search))
                || (q.getDescription() != null && q.getDescription().toLowerCase().contains(search))
                || (q.getPatientName() != null && q.getPatientName().toLowerCase().contains(search));

            boolean matchFilter = switch (filter) {
                case "🚫 Mots grossiers uniquement" -> containsBadWord(q);
                case "✅ Questions propres"         -> !containsBadWord(q);
                default                             -> true;
            };
            return matchSearch && matchFilter;
        }).collect(Collectors.toList());

        renderCards(filtered);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  RENDER
    // ═══════════════════════════════════════════════════════════════════════

    private void renderCards(List<Question> questions) {
        questionsContainer.getChildren().clear();

        if (questions.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(80, 0, 80, 0));
            Label icon = new Label("✅");
            icon.setStyle("-fx-font-size: 48px;");
            Label msg = new Label("Aucune question trouvée");
            msg.setStyle("-fx-text-fill: #475569; -fx-font-size: 16px; -fx-font-weight: 600;");
            Label sub = new Label("Essayez de modifier vos filtres");
            sub.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
            empty.getChildren().addAll(icon, msg, sub);
            questionsContainer.getChildren().add(empty);
            return;
        }

        for (Question q : questions) {
            VBox card = buildCard(q);
            questionsContainer.getChildren().add(card);
            FadeTransition ft = new FadeTransition(Duration.millis(250), card);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }
    }

    private VBox buildCard(Question q) {
        boolean hasBadWord = containsBadWord(q);
        String accentColor = hasBadWord ? "#f43f5e" : getBadgeColor(q.getSpecialiteId());
        String bgColor     = hasBadWord ? "#160a0d" : "#0e1220";
        String borderColor = hasBadWord ? "#f43f5e88" : "#252d42";

        VBox card = new VBox(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: " + (hasBadWord ? "1.5" : "1") + ";" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
        );

        // ── TOP ROW: badges + status + date ──────────────────────────────
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(14, 18, 0, 18));

        // Bad word alert badge
        if (hasBadWord) {
            Label alertBadge = new Label("🚫 CONTENU INAPPROPRIÉ");
            alertBadge.setStyle(
                "-fx-background-color: rgba(244,63,94,0.2);" +
                "-fx-text-fill: #f43f5e; -fx-font-size: 10px; -fx-font-weight: 800;" +
                "-fx-padding: 3 10; -fx-background-radius: 20;" +
                "-fx-border-color: #f43f5e55; -fx-border-width: 1; -fx-border-radius: 20;"
            );
            topRow.getChildren().add(alertBadge);
        }

        // Specialty badge
        String spec = (q.getSpecialiteNom() != null && !q.getSpecialiteNom().isEmpty())
            ? q.getSpecialiteNom().toUpperCase() : "GÉNÉRAL";
        Label specBadge = new Label(spec);
        specBadge.setStyle(
            "-fx-background-color: " + accentColor + "22;" +
            "-fx-text-fill: " + accentColor + ";" +
            "-fx-font-size: 10px; -fx-font-weight: 700;" +
            "-fx-padding: 3 10; -fx-background-radius: 20;" +
            "-fx-border-color: " + accentColor + "55;" +
            "-fx-border-width: 1; -fx-border-radius: 20;"
        );
        topRow.getChildren().add(specBadge);

        // Status dot
        String statusColor = "open".equalsIgnoreCase(q.getStatus()) ? "#22c55e" : "#f43f5e";
        Label statusDot = new Label();
        statusDot.setStyle(
            "-fx-background-color: " + statusColor + ";" +
            "-fx-background-radius: 50; -fx-min-width: 7; -fx-min-height: 7;" +
            "-fx-max-width: 7; -fx-max-height: 7;"
        );
        topRow.getChildren().add(statusDot);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        // ID chip
        Label idChip = new Label("#" + q.getId());
        idChip.setStyle(
            "-fx-background-color: #1c2133; -fx-text-fill: #475569;" +
            "-fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10;"
        );

        // Date
        String dateStr = q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FMT) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");

        topRow.getChildren().addAll(topSpacer, idChip, dateLabel);

        // ── BODY: title + description + image ────────────────────────────
        VBox body = new VBox(8);
        body.setPadding(new Insets(10, 18, 12, 18));

        Label titleLabel = new Label(q.getTitre() != null ? q.getTitre() : "Sans titre");
        titleLabel.setStyle(
            "-fx-text-fill: " + (hasBadWord ? "#fca5a5" : "#f1f5f9") + ";" +
            "-fx-font-size: 15px; -fx-font-weight: 700;"
        );
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        String desc = q.getDescription() != null ? q.getDescription() : "";
        if (desc.length() > 180) desc = desc.substring(0, 180) + "…";
        Label descLabel = new Label(hasBadWord ? "⚠️ " + desc : desc);
        descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-line-spacing: 2;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(titleLabel, descLabel);

        // Image preview if exists
        if (q.getImageName() != null && !q.getImageName().isEmpty()) {
            File imgFile = resolveImageFile(q.getImageName());
            if (imgFile != null && imgFile.exists()) {
                try {
                    Image img = new Image(imgFile.toURI().toString());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(320);
                    iv.setFitHeight(160);
                    iv.setPreserveRatio(true);
                    iv.setSmooth(true);
                    iv.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);");

                    VBox imgBox = new VBox(4);
                    imgBox.setStyle(
                        "-fx-background-color: #141826; -fx-background-radius: 8; -fx-padding: 8;"
                    );
                    Label imgLbl = new Label("📸 Image jointe");
                    imgLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 10px;");
                    imgBox.getChildren().addAll(imgLbl, iv);
                    VBox.setMargin(imgBox, new Insets(4, 0, 0, 0));
                    body.getChildren().add(imgBox);
                } catch (Exception ignored) {}
            }
        }

        // ── DIVIDER ───────────────────────────────────────────────────────
        Region divider = new Region();
        divider.setMinHeight(1); divider.setMaxHeight(1);
        divider.setStyle("-fx-background-color: #1c2133;");

        // ── FOOTER: avatar + author + stats + actions ─────────────────────
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 18, 12, 18));

        // Avatar
        String authorName = q.getPatientName() != null ? q.getPatientName() : "Anonyme";
        String initials = authorName.length() >= 1
            ? String.valueOf(authorName.charAt(0)).toUpperCase() : "?";
        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(30, 30); avatarPane.setMaxSize(30, 30);
        Region avatarBg = new Region();
        avatarBg.setStyle(
            "-fx-background-color: " + accentColor + "33; -fx-background-radius: 50;"
        );
        Label avatarLbl = new Label(initials);
        avatarLbl.setStyle(
            "-fx-text-fill: " + accentColor + "; -fx-font-size: 12px; -fx-font-weight: 700;"
        );
        avatarPane.getChildren().addAll(avatarBg, avatarLbl);

        Label authorLabel = new Label(authorName);
        authorLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Stats chips
        HBox answersChip = makeChip("💬 " + q.getAnswerCount(), "#5b6ef5");
        HBox likesChip   = makeChip("👍 " + q.getLikes(), "#22c55e");

        // ── Delete button ─────────────────────────────────────────────────
        Button btnDelete = new Button("🗑  Supprimer");
        String delNormal =
            "-fx-background-color: rgba(244,63,94,0.12); -fx-text-fill: #f43f5e;" +
            "-fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 6 18;" +
            "-fx-background-radius: 8; -fx-border-color: rgba(244,63,94,0.35);" +
            "-fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;";
        String delHover =
            "-fx-background-color: #f43f5e; -fx-text-fill: white;" +
            "-fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 6 18;" +
            "-fx-background-radius: 8; -fx-border-color: #f43f5e;" +
            "-fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;";
        btnDelete.setStyle(delNormal);
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(delHover));
        btnDelete.setOnMouseExited(e  -> btnDelete.setStyle(delNormal));
        btnDelete.setOnAction(e -> confirmDelete(q, card));

        footer.getChildren().addAll(
            avatarPane, authorLabel, footerSpacer,
            answersChip, likesChip, btnDelete
        );

        card.getChildren().addAll(topRow, body, divider, footer);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private HBox makeChip(String text, String color) {
        HBox chip = new HBox();
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-background-radius: 20; -fx-padding: 4 10;" +
            "-fx-border-color: " + color + "33; -fx-border-width: 1; -fx-border-radius: 20;"
        );
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        chip.getChildren().add(lbl);
        return chip;
    }

    private String getBadgeColor(int specialiteId) {
        if (specialiteId <= 0) return BADGE_COLORS[0];
        return BADGE_COLORS[specialiteId % BADGE_COLORS.length];
    }

    private boolean containsBadWord(Question q) {
        String text = ((q.getTitre() != null ? q.getTitre() : "") + " " +
                       (q.getDescription() != null ? q.getDescription() : "")).toLowerCase();
        for (String word : BAD_WORDS) {
            if (text.contains(word.toLowerCase())) return true;
        }
        return false;
    }

    private File resolveImageFile(String imageName) {
        String[] paths = {
            System.getProperty("user.dir") + "/bibliotheque/forum_images/" + imageName,
            System.getProperty("user.dir") + "/pijava--main/bibliotheque/forum_images/" + imageName,
            System.getProperty("user.dir") + "/uploads/forum_images/" + imageName,
            System.getProperty("user.dir") + "/pijava--main/uploads/forum_images/" + imageName,
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        return null;
    }

    // ── Delete ────────────────────────────────────────────────────────────

    private void confirmDelete(Question q, VBox card) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la question");
        confirm.setHeaderText("Supprimer « " + q.getTitre() + " » ?");
        confirm.setContentText(
            "Cette action supprimera la question et toutes ses réponses.\n" +
            "Cette action est irréversible."
        );
        confirm.getDialogPane().setStyle("-fx-background-color: #0e1220;");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        questionService.delete(q.getId());
                        allQuestions.remove(q);
                        Platform.runLater(() -> {
                            // Fade out then remove
                            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
                            ft.setFromValue(1); ft.setToValue(0);
                            ft.setOnFinished(e -> {
                                questionsContainer.getChildren().remove(card);
                                updateStats();
                            });
                            ft.play();
                            showSuccess("Question #" + q.getId() + " supprimée avec succès");
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showError("Erreur: " + ex.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    public void handleRefresh() {
        loadQuestions();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur"); alert.setHeaderText(null);
        alert.setContentText(msg); alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Label lbl = new Label("✅ " + msg);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setStyle(
            "-fx-text-fill: #22c55e; -fx-font-size: 13px; -fx-font-weight: 600;" +
            "-fx-background-color: rgba(34,197,94,0.1); -fx-padding: 12 20;" +
            "-fx-background-radius: 8; -fx-border-color: rgba(34,197,94,0.3);" +
            "-fx-border-width: 1; -fx-border-radius: 8;"
        );
        questionsContainer.getChildren().add(0, lbl);
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> questionsContainer.getChildren().remove(lbl));
        }).start();
    }
}
