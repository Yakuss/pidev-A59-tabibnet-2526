package com.pidev.controllers;

import com.pidev.models.*;
import com.pidev.services.*;
import com.pidev.utils.UserSession;
import com.pidev.utils.EmailService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientCalendarViewController {

    @FXML private Label weekLabel;
    @FXML private HBox weekGrid;
    @FXML private Label weatherStripLabel;
    @FXML private VBox aiRecommendationPanel;
    @FXML private Label aiRecommendationText;
    @FXML private Label aiAttendanceLabel;
    @FXML private Label aiBestSlotLabel;
    @FXML private Label aiStatusLabel;
    @FXML private VBox patientHistoryPanel;
    @FXML private Label doctorNameTitle;

    private final CalendarDataService calendarService = new CalendarDataService();
    private final RendezVousService rdvService = new RendezVousService();
    private final RecommendationManager recommendationMgr = new RecommendationManager();
    private final ExecutorService asyncPool = Executors.newSingleThreadExecutor();

    private LocalDate currentWeekStart;
    private int doctorId = 1; // Default doctor ID for now, should be set when selecting a doctor

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter WEEK_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // If a doctor was selected in the session, use it
        if (UserSession.getInstance().getSelectedMedecinId() != null) {
            this.doctorId = UserSession.getInstance().getSelectedMedecinId();
            loadDoctorInfo();
        }

        asyncPool.submit(this::prefetchIntelligenceData);
        refreshCalendar();
    }

    private void loadDoctorInfo() {
        try {
            Medecin m = new MedecinService().getById(doctorId);
            if (m != null) {
                if (doctorNameTitle != null) {
                    doctorNameTitle.setText("Planning de Dr. " + m.getFullName());
                }
                System.out.println("📅 Viewing calendar for Dr. " + m.getFullName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (MainUserController.getInstance() != null) {
            MainUserController.getInstance().showMedecins();
        }
    }

    private void prefetchIntelligenceData() {
        recommendationMgr.loadHolidays();
        recommendationMgr.loadWeekWeather(currentWeekStart);
        Platform.runLater(this::refreshCalendar);
    }

    @FXML
    private void previousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        asyncPool.submit(this::prefetchIntelligenceData);
        refreshCalendar();
    }

    @FXML
    private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        asyncPool.submit(this::prefetchIntelligenceData);
        refreshCalendar();
    }

    private void refreshCalendar() {
        weekLabel.setText("Semaine du " + currentWeekStart.format(WEEK_FMT));
        weekGrid.getChildren().clear();

        try {
            TempsTravail ttDefault = calendarService.getTempsTravail(doctorId);
            CalendarSetting setting = calendarService.getCalendarSetting(doctorId);
            List<Indisponibilite> indisponibilites = calendarService.getIndisponibilites(doctorId);
            List<RendezVous> allAppointments = rdvService.getByMedecinId(doctorId);

            updateWeatherStrip();

            for (int i = 0; i < 7; i++) {
                LocalDate date = currentWeekStart.plusDays(i);
                RecommendationManager.DayIntelligence intel = recommendationMgr.getDayIntelligence(date);
                VBox dayColumn = createDayColumn(date, ttDefault, setting, indisponibilites, allAppointments, intel);
                HBox.setHgrow(dayColumn, Priority.ALWAYS);
                weekGrid.getChildren().add(dayColumn);
            }

            buildPatientHistoryPanel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createDayColumn(LocalDate date, TempsTravail tt, CalendarSetting setting,
                                 List<Indisponibilite> indisponibilites,
                                 List<RendezVous> dayAppointments,
                                 RecommendationManager.DayIntelligence intel) {

        VBox col = new VBox(0);
        col.setAlignment(Pos.TOP_CENTER);
        col.setStyle("-fx-border-color: #252d42; -fx-border-width: 0 1px 0 0;");

        boolean isToday = date.equals(LocalDate.now());
        boolean isOff = indisponibilites.stream().anyMatch(ind -> ind.getDate().equals(date));
        boolean isHoliday = intel.isHoliday();
        boolean isRisky = intel.isRiskyWeather();

        if (isToday) col.setStyle(col.getStyle() + "-fx-background-color: rgba(129,140,248,0.05);");

        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-border-color: transparent transparent #252d42 transparent; -fx-border-width: 0 0 1 0;");

        Label dayName = new Label(date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.FRENCH).toUpperCase());
        dayName.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + (isToday ? "#818cf8" : "#94a3b8") + ";");
        
        Label dateLabel = new Label(date.format(DATE_FMT));
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");

        header.getChildren().addAll(dayName, dateLabel);
        col.getChildren().add(header);

        VBox content = new VBox(8);
        content.setPadding(new Insets(12, 8, 12, 8));
        content.setAlignment(Pos.TOP_CENTER);

        HBox badgeBox = new HBox(4);
        badgeBox.setAlignment(Pos.CENTER);

        if (intel.weather != null && !intel.weather.getCondition().equals("unknown")) {
            Label weatherIcon = new Label(intel.weather.getIcon());
            weatherIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9;");
            Tooltip.install(weatherIcon, new Tooltip(intel.weather.getDescription()));
            badgeBox.getChildren().add(weatherIcon);
            if (isRisky) {
                Label risk = new Label("⚠");
                risk.setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold;");
                badgeBox.getChildren().add(risk);
            }
        }

        if (isHoliday) {
            Label hBadge = new Label("🎌");
            Tooltip.install(hBadge, new Tooltip(intel.holiday.getDisplayName()));
            badgeBox.getChildren().add(hBadge);
        }

        if (!badgeBox.getChildren().isEmpty()) content.getChildren().add(badgeBox);

        boolean isPastDate = date.isBefore(LocalDate.now());

        if (isOff || isHoliday) {
            Label offLabel = new Label(isHoliday ? intel.holiday.getDisplayName() : "CONGÉ MÉDECIN");
            offLabel.setWrapText(true);
            offLabel.setAlignment(Pos.CENTER);
            offLabel.setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: 800; -fx-font-size: 10px; -fx-padding: 30 5; -fx-text-alignment: center;");
            content.getChildren().add(offLabel);
        } else if (isPastDate) {
            Label pastLabel = new Label("PASSÉ");
            pastLabel.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
            content.getChildren().add(pastLabel);
        } else {
            List<TimeSlot> slots = calendarService.generateSlots(date, tt, setting, indisponibilites, dayAppointments);
            if (!slots.isEmpty()) {
                for (TimeSlot slot : slots) {
                    Button slBtn = new Button(slot.getStartTime().toString());
                    slBtn.setMaxWidth(Double.MAX_VALUE);
                    boolean isPastTime = isToday && slot.getStartTime().isBefore(LocalTime.now());
                    
                    if (!slot.isAvailable()) {
                        slBtn.setText(slot.getStartTime().toString() + " (Occupé)");
                        slBtn.setDisable(true);
                        slBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8;");
                    } else if (isPastTime) {
                        slBtn.setDisable(true);
                        slBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8;");
                    } else {
                        slBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #f1f5f9; -fx-font-size: 11px; -fx-cursor: hand;");
                        slBtn.setOnAction(e -> handleSlotBooking(date, slot.getStartTime()));
                    }
                    content.getChildren().add(slBtn);
                }
            } else {
                Label noSlots = new Label("COMPLET");
                noSlots.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 20 0;");
                content.getChildren().add(noSlots);
            }
        }

        col.getChildren().add(content);
        return col;
    }

    private void handleSlotBooking(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now()) || (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            showAlert("Erreur", "Vous ne pouvez pas prendre un rendez-vous dans le passé.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de réservation");
        alert.setHeaderText("Réserver pour le " + date.format(WEEK_FMT) + " à " + time.toString() + " ?");
        alert.setContentText("Voulez-vous confirmer ce rendez-vous ?");

        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                BaseUser user = UserSession.getInstance().getUser();
                if (user != null) {
                    RendezVous r = new RendezVous();
                    r.setPatientId(user.getId());
                    r.setMedecinId(doctorId);
                    r.setDate(date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    r.setHeure(time.toString());
                    r.setStatut("En attente");
                    r.setDurationMinutes(30);
                    try {
                        rdvService.ajouter(r);
                        
                        try {
                            Medecin m = new MedecinService().getById(doctorId);
                            if (m != null) {
                                EmailService.getInstance().sendAppointmentConfirmation(
                                    user.getEmail(),
                                    user.getFullName(),
                                    "Dr. " + m.getFullName(),
                                    r.getDate(),
                                    r.getHeure(),
                                    () -> System.out.println("✅ Email sent"),
                                    (err) -> System.err.println("❌ Email fail: " + err)
                                );
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        refreshCalendar();
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Succès");
                        success.setHeaderText("Rendez-vous réservé !");
                        success.setContentText("Un email de confirmation vous a été envoyé.");
                        success.showAndWait();
                        
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateWeatherStrip() {
        if (weatherStripLabel == null) return;
        StringBuilder sb = new StringBuilder("Météo semaine: ");
        boolean anyRisky = false;
        for (int i = 0; i < 7; i++) {
            LocalDate d = currentWeekStart.plusDays(i);
            var w = recommendationMgr.getWeatherForDate(d);
            sb.append(w.getIcon()).append(" ");
            if (w.isRisky()) anyRisky = true;
        }
        weatherStripLabel.setText(sb.toString().trim());
        weatherStripLabel.setStyle(anyRisky ? "-fx-text-fill: #f43f5e;" : "-fx-text-fill: #818cf8;");
    }

    @FXML
    private void fetchAiRecommendation() {
        BaseUser user = UserSession.getInstance().getUser();
        if (user == null) return;
        
        showAiStatus("Analyse IA en cours...", true);
        asyncPool.submit(() -> {
            AiRecommendation rec = recommendationMgr.getRecommendation(user.getId(), doctorId);
            Platform.runLater(() -> displayAiRecommendation(rec));
        });
    }

    private void displayAiRecommendation(AiRecommendation rec) {
        if (aiRecommendationPanel == null) return;
        aiRecommendationPanel.setVisible(true);
        aiRecommendationPanel.setManaged(true);
        aiRecommendationText.setText(rec.getRecommendation());
        aiAttendanceLabel.setText("Confiance: " + (rec.getAttendanceProbability() != null ? rec.getAttendanceProbability() : "90%"));
        AiRecommendation.SuggestedSlot best = rec.getBestSlot();
        if (best != null) {
            aiBestSlotLabel.setText("Conseillé: " + best.toString());
            aiBestSlotLabel.setCursor(Cursor.HAND);
            aiBestSlotLabel.setOnMouseClicked(e -> handleSlotBooking(best.getDate(), best.getTime()));
        } else {
            aiBestSlotLabel.setText("Choisissez un créneau libre.");
        }
        aiStatusLabel.setText(rec.isFallback() ? "⚠ Mode secours" : "✓ Recommandation IA active");
    }

    private void showAiStatus(String message, boolean loading) {
        if (aiRecommendationPanel == null) return;
        aiRecommendationPanel.setVisible(true);
        aiRecommendationPanel.setManaged(true);
        aiRecommendationText.setText(message);
    }

    @FXML
    private void hideAiPanel() {
        if (aiRecommendationPanel != null) {
            aiRecommendationPanel.setVisible(false);
            aiRecommendationPanel.setManaged(false);
        }
    }

    private void buildPatientHistoryPanel() {
        if (patientHistoryPanel == null) return;
        patientHistoryPanel.getChildren().clear();
        Label h = new Label("VOTRE HISTORIQUE");
        h.setStyle("-fx-font-weight: bold; -fx-text-fill: #f1f5f9; -fx-font-size: 12px;");
        patientHistoryPanel.getChildren().add(h);
        
        BaseUser user = UserSession.getInstance().getUser();
        if (user == null) return;
        
        List<RendezVous> history = rdvService.getByPatientId(user.getId());
        if (history.isEmpty()) {
            patientHistoryPanel.getChildren().add(new Label("Aucun rendez-vous passé."));
        } else {
            for (RendezVous r : history) {
                Label l = new Label(r.getDate() + " - " + r.getHeure());
                l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                patientHistoryPanel.getChildren().add(l);
                if (patientHistoryPanel.getChildren().size() > 10) break;
            }
        }
    }
}
