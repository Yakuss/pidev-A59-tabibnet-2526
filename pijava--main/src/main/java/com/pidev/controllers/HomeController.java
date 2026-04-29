package com.pidev.controllers;

import com.pidev.models.Appointment;
import com.pidev.models.Question;
import com.pidev.models.Specialite;
import com.pidev.services.AppointmentService;
import com.pidev.services.PatientService;
import com.pidev.services.MedecinService;
import com.pidev.services.QuestionService;
import com.pidev.services.SpecialiteService;
import com.pidev.utils.UserSession;
import com.pidev.models.BaseUser;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HomeController — Landing page shown after login.
 * Displays live stats, recent forum questions, next appointment, and specialties.
 */
public class HomeController {

    // ── Hero stats ──────────────────────────────────────────────────────────
    @FXML private Label lblWelcomeUser;
    @FXML private Label lblStatPatients;
    @FXML private Label lblStatMedecins;
    @FXML private Label lblStatQuestions;
    @FXML private Label lblStatAppointments;

    // ── Quick access card counters ───────────────────────────────────────────
    @FXML private Label lblForumCount;
    @FXML private Label lblAppointmentCount;

    // ── Dynamic content boxes ────────────────────────────────────────────────
    @FXML private VBox recentQuestionsBox;
    @FXML private VBox nextAppointmentBox;
    @FXML private VBox specialitiesBox;

    // ── Hover-tracked cards ──────────────────────────────────────────────────
    @FXML private VBox cardForum;
    @FXML private VBox cardAppointments;
    @FXML private VBox cardProfile;

    // ── Services ─────────────────────────────────────────────────────────────
    private final PatientService     patientService     = new PatientService();
    private final MedecinService     medecinService     = new MedecinService();
    private final QuestionService    questionService    = new QuestionService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final SpecialiteService  specialiteService  = new SpecialiteService();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

    // ── Color palette for speciality badges ──────────────────────────────────
    private static final String[] BADGE_COLORS = {
            "#5b6ef5", "#22c55e", "#f59e0b", "#f43f5e",
            "#06b6d4", "#8b5cf6", "#ec4899", "#14b8a6"
    };

    // ════════════════════════════════════════════════════════════════════════
    //  INIT
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        setupCardHovers();
        loadWelcomeMessage();
        loadStats();
        loadRecentQuestions();
        loadNextAppointment();
        loadSpecialities();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  WELCOME
    // ════════════════════════════════════════════════════════════════════════

    private void loadWelcomeMessage() {
        BaseUser user = UserSession.getInstance().getUser();
        if (user != null) {
            lblWelcomeUser.setText("Bonjour, " + user.getFirstName() + " 👋  — Votre santé, notre priorité.");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STATS
    // ════════════════════════════════════════════════════════════════════════

    private void loadStats() {
        try {
            int patients = patientService.getAll().size();
            lblStatPatients.setText(String.valueOf(patients));
        } catch (Exception e) { lblStatPatients.setText("—"); }

        try {
            int medecins = medecinService.getAll().size();
            lblStatMedecins.setText(String.valueOf(medecins));
        } catch (Exception e) { lblStatMedecins.setText("—"); }

        try {
            List<Question> questions = questionService.getAll();
            lblStatQuestions.setText(String.valueOf(questions.size()));
            lblForumCount.setText(questions.size() + " questions actives");
        } catch (Exception e) {
            lblStatQuestions.setText("—");
            lblForumCount.setText("Forum disponible");
        }

        try {
            List<Appointment> appointments = appointmentService.getAll();
            lblStatAppointments.setText(String.valueOf(appointments.size()));
            lblAppointmentCount.setText(appointments.size() + " rendez-vous");
        } catch (Exception e) {
            lblStatAppointments.setText("—");
            lblAppointmentCount.setText("Voir les rendez-vous");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RECENT QUESTIONS
    // ════════════════════════════════════════════════════════════════════════

    private void loadRecentQuestions() {
        recentQuestionsBox.getChildren().clear();
        try {
            List<Question> questions = questionService.getAll();
            // Show only the 5 most recent
            int limit = Math.min(5, questions.size());

            if (limit == 0) {
                Label empty = new Label("Aucune question pour le moment.");
                empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-style: italic;");
                recentQuestionsBox.getChildren().add(empty);
                return;
            }

            for (int i = 0; i < limit; i++) {
                Question q = questions.get(i);
                HBox row = buildQuestionRow(q);
                recentQuestionsBox.getChildren().add(row);

                // Fade-in animation
                FadeTransition ft = new FadeTransition(Duration.millis(250 + i * 60L), row);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        } catch (Exception e) {
            Label err = new Label("Impossible de charger les questions.");
            err.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 12px;");
            recentQuestionsBox.getChildren().add(err);
        }
    }

    private HBox buildQuestionRow(Question q) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-background-color: #141826;" +
                     "-fx-background-radius: 10;" +
                     "-fx-cursor: hand;");

        // Accent dot
        String color = BADGE_COLORS[q.getSpecialiteId() % BADGE_COLORS.length];
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill: " + color + ";");

        // Text content
        VBox textBox = new VBox(3);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label title = new Label(q.getTitre() != null ? q.getTitre() : "Sans titre");
        title.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 13px; -fx-font-weight: 600;");
        title.setMaxWidth(Double.MAX_VALUE);

        String meta = (q.getPatientName() != null ? q.getPatientName() : "Anonyme") +
                      "  ·  " +
                      (q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FMT) : "");
        Label metaLabel = new Label(meta);
        metaLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");

        textBox.getChildren().addAll(title, metaLabel);

        // Answer count chip
        Label chip = new Label("💬 " + q.getAnswerCount());
        chip.setStyle("-fx-background-color: rgba(91,110,245,0.1);" +
                      "-fx-text-fill: #818cf8;" +
                      "-fx-font-size: 11px; -fx-font-weight: 600;" +
                      "-fx-padding: 3 10; -fx-background-radius: 20;");

        row.getChildren().addAll(dot, textBox, chip);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #1c2133; -fx-background-radius: 10; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: #141826; -fx-background-radius: 10; -fx-cursor: hand;"));

        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NEXT APPOINTMENT
    // ════════════════════════════════════════════════════════════════════════

    private void loadNextAppointment() {
        nextAppointmentBox.getChildren().clear();
        try {
            BaseUser user = UserSession.getInstance().getUser();
            List<Appointment> all = appointmentService.getAll();

            // Find the first upcoming appointment for this user
            Appointment next = null;
            if (user != null) {
                next = all.stream()
                        .filter(a -> a.getPatientId() == user.getId())
                        .filter(a -> "pending".equalsIgnoreCase(a.getStatus())
                                  || "confirmed".equalsIgnoreCase(a.getStatus()))
                        .findFirst()
                        .orElse(null);
            }

            if (next == null) {
                VBox empty = new VBox(8);
                empty.setAlignment(Pos.CENTER);
                Label icon = new Label("📭");
                icon.setStyle("-fx-font-size: 28px;");
                Label msg = new Label("Aucun rendez-vous à venir");
                msg.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
                Button btnBook = new Button("Prendre un rendez-vous");
                btnBook.setStyle("-fx-background-color: #5b6ef5; -fx-text-fill: white;" +
                                 "-fx-font-size: 12px; -fx-font-weight: 600;" +
                                 "-fx-padding: 8 16; -fx-background-radius: 8;" +
                                 "-fx-cursor: hand; -fx-border-width: 0;");
                btnBook.setOnAction(e -> goToAppointments());
                empty.getChildren().addAll(icon, msg, btnBook);
                nextAppointmentBox.getChildren().add(empty);
                return;
            }

            // Show the appointment details
            String statusColor = "confirmed".equalsIgnoreCase(next.getStatus()) ? "#22c55e" : "#f59e0b";
            String statusText  = "confirmed".equalsIgnoreCase(next.getStatus()) ? "Confirmé" : "En attente";

            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #141826; -fx-background-radius: 10; -fx-padding: 14;");

            // Status badge
            HBox statusRow = new HBox(6);
            statusRow.setAlignment(Pos.CENTER_LEFT);
            Circle statusDot = new Circle(4);
            statusDot.setStyle("-fx-fill: " + statusColor + ";");
            Label statusLabel = new Label(statusText);
            statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: 700;");
            statusRow.getChildren().addAll(statusDot, statusLabel);

            // Doctor name
            Label doctorLabel = new Label("🩺  " + (next.getDoctorName() != null ? next.getDoctorName() : "Médecin"));
            doctorLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 14px; -fx-font-weight: 700;");

            // Department
            if (next.getDepartment() != null && !next.getDepartment().isEmpty()) {
                Label deptLabel = new Label("🏥  " + next.getDepartment());
                deptLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                card.getChildren().addAll(statusRow, doctorLabel, deptLabel);
            } else {
                card.getChildren().addAll(statusRow, doctorLabel);
            }

            // Date & time
            if (next.getDate() != null) {
                Label dateLabel = new Label("📆  " + next.getDate().format(DATE_FMT));
                dateLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 12px; -fx-font-weight: 600;");
                card.getChildren().add(dateLabel);
            }

            // Duration
            if (next.getDuration() > 0) {
                Label durLabel = new Label("⏱  " + next.getDuration() + " minutes");
                durLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");
                card.getChildren().add(durLabel);
            }

            nextAppointmentBox.getChildren().add(card);

        } catch (Exception e) {
            Label err = new Label("Impossible de charger le rendez-vous.");
            err.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 12px;");
            nextAppointmentBox.getChildren().add(err);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SPECIALITIES
    // ════════════════════════════════════════════════════════════════════════

    private void loadSpecialities() {
        specialitiesBox.getChildren().clear();
        try {
            List<Specialite> specs = specialiteService.getAll();
            int limit = Math.min(6, specs.size());

            if (limit == 0) {
                Label empty = new Label("Aucune spécialité disponible.");
                empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
                specialitiesBox.getChildren().add(empty);
                return;
            }

            for (int i = 0; i < limit; i++) {
                Specialite s = specs.get(i);
                String color = BADGE_COLORS[i % BADGE_COLORS.length];

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 12, 8, 12));
                row.setStyle("-fx-background-color: " + color + "11;" +
                             "-fx-background-radius: 8;" +
                             "-fx-border-color: " + color + "33;" +
                             "-fx-border-width: 1; -fx-border-radius: 8;");

                Circle dot = new Circle(5);
                dot.setStyle("-fx-fill: " + color + ";");

                Label name = new Label(s.getNom() != null ? s.getNom() : "—");
                name.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: 600;");

                row.getChildren().addAll(dot, name);
                specialitiesBox.getChildren().add(row);
            }

            // "See all" if more than 6
            if (specs.size() > 6) {
                Label more = new Label("+ " + (specs.size() - 6) + " autres spécialités");
                more.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-padding: 4 0 0 0;");
                specialitiesBox.getChildren().add(more);
            }

        } catch (Exception e) {
            Label err = new Label("Impossible de charger les spécialités.");
            err.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 12px;");
            specialitiesBox.getChildren().add(err);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CARD HOVER EFFECTS
    // ════════════════════════════════════════════════════════════════════════

    private void setupCardHovers() {
        applyCardHover(cardForum,
                "#252d42",
                "rgba(91,110,245,0.4)");
        applyCardHover(cardAppointments,
                "#252d42",
                "rgba(34,197,94,0.4)");
        applyCardHover(cardProfile,
                "#252d42",
                "rgba(147,51,234,0.4)");
    }

    private void applyCardHover(VBox card, String borderNormal, String borderHover) {
        String base = "-fx-background-color: #0e1220;" +
                      "-fx-background-radius: 14;" +
                      "-fx-border-color: " + borderNormal + ";" +
                      "-fx-border-width: 1;" +
                      "-fx-border-radius: 14;" +
                      "-fx-padding: 24;" +
                      "-fx-cursor: hand;";
        String hover = "-fx-background-color: #141826;" +
                       "-fx-background-radius: 14;" +
                       "-fx-border-color: " + borderHover + ";" +
                       "-fx-border-width: 1;" +
                       "-fx-border-radius: 14;" +
                       "-fx-padding: 24;" +
                       "-fx-cursor: hand;";
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e  -> card.setStyle(base));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION — delegates to MainUserController via parent lookup
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void goToForum() {
        navigateTo("/views/ForumView.fxml");
    }

    @FXML
    public void goToAppointments() {
        navigateTo("/views/AppointmentView.fxml");
    }

    @FXML
    public void goToProfile() {
        navigateTo("/views/ProfileView.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node view = loader.load();

            // Walk up to the StackPane contentArea in MainUserController
            javafx.scene.Node parent = recentQuestionsBox.getScene().getRoot();
            javafx.scene.layout.StackPane contentArea =
                    (javafx.scene.layout.StackPane) parent.lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (Exception e) {
            System.err.println("❌ HomeController navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
