package com.tabibnet.calendar.controller;

import com.tabibnet.calendar.DataStore;
import com.tabibnet.calendar.model.Appointment;
import com.tabibnet.calendar.model.AiRecommendation;
import com.tabibnet.calendar.model.CalendarSetting;
import com.tabibnet.calendar.model.Indisponibilite;
import com.tabibnet.calendar.model.TempsTravail;
import com.tabibnet.calendar.model.TimeSlot;
import com.tabibnet.calendar.service.CalendarService;
import com.tabibnet.calendar.service.RecommendationManager;
import com.tabibnet.calendar.service.RecommendationManager.DayIntelligence;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CalendarViewController — fully enhanced doctor calendar controller.
 *
 * Features:
 *  • Weekly grid with 7 day columns
 *  • Appointments displayed as colored status cards per day
 *  • Weather indicator in each day header (icon + risky warning)
 *  • Holiday detection — blocked day visual with holiday name badge
 *  • AI Recommendation panel at the bottom
 *  • Patient preference history panel in the sidebar
 *  • Async loading of API data to keep UI responsive
 */
public class PatientCalendarViewController {

    // ── FXML Bindings ──────────────────────────────────────────────────────────
    @FXML private Label  weekLabel;
    @FXML private HBox   weekGrid;
    @FXML private Label  weatherStripLabel;
    @FXML private VBox   aiRecommendationPanel;
    @FXML private Label  aiRecommendationText;
    @FXML private Label  aiAttendanceLabel;
    @FXML private Label  aiBestSlotLabel;
    @FXML private Label  aiStatusLabel;
    @FXML private VBox   patientHistoryPanel;

    // ── Services ───────────────────────────────────────────────────────────────
    private final CalendarService         calendarService     = new CalendarService();
    private final RecommendationManager   recommendationMgr   = new RecommendationManager();
    private final ExecutorService         asyncPool           = Executors.newSingleThreadExecutor();

    // ── State ──────────────────────────────────────────────────────────────────
    private LocalDate currentWeekStart;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter WEEK_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ──────────────────────────────────────────────────────────────────────────
    // INITIALIZATION
    // ──────────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        currentWeekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Start async API prefetch for the current week
        asyncPool.submit(this::prefetchIntelligenceData);

        // Render calendar (may show partial data until async completes)
        refreshCalendar();
    }

    /**
     * Fetches weather and holiday data asynchronously, then re-renders.
     */
    private void prefetchIntelligenceData() {
        recommendationMgr.loadHolidays(); // Load holidays in background
        recommendationMgr.loadWeekWeather(currentWeekStart);
        Platform.runLater(this::refreshCalendar);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ──────────────────────────────────────────────────────────────────────────

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

    // ──────────────────────────────────────────────────────────────────────────
    // CALENDAR RENDERING
    // ──────────────────────────────────────────────────────────────────────────

    private void refreshCalendar() {
        weekLabel.setText("Semaine du " + currentWeekStart.format(WEEK_FMT));
        weekGrid.getChildren().clear();

        DataStore store = DataStore.getInstance();
        CalendarSetting      setting          = store.getCalendarSetting();
        List<Indisponibilite> indisponibilites = store.getIndisponibilites();
        List<Appointment>    allAppointments  = store.getAppointments();

        // Weather strip — show summary for the week
        updateWeatherStrip();

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            TempsTravail tt = store.getTempsTravailForDate(date);

            List<Appointment> dayAppts = store.getAppointmentsForDate(date);
            DayIntelligence   intel    = recommendationMgr.getDayIntelligence(date);

            VBox dayColumn = createDayColumn(date, tt, setting, indisponibilites, dayAppts, intel);
            HBox.setHgrow(dayColumn, Priority.ALWAYS);
            weekGrid.getChildren().add(dayColumn);
        }

        // Build patient history summary in sidebar panel
        buildPatientHistoryPanel(allAppointments);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DAY COLUMN
    // ──────────────────────────────────────────────────────────────────────────

    private VBox createDayColumn(LocalDate date, TempsTravail tt, CalendarSetting setting,
                                 List<Indisponibilite> indisponibilites,
                                 List<Appointment> dayAppointments,
                                 DayIntelligence intel) {

        VBox col = new VBox(0);
        col.setAlignment(Pos.TOP_CENTER);
        col.getStyleClass().add("day-column");

        boolean isPast    = date.isBefore(LocalDate.now());
        boolean isToday   = date.equals(LocalDate.now());
        boolean isOff     = indisponibilites.stream().anyMatch(ind -> ind.getDate().equals(date));
        boolean isHoliday = intel.isHoliday();
        boolean isRisky   = intel.isRiskyWeather();

        if (isToday) col.getStyleClass().add("today-column");

        // ── Header ─────────────────────────────────────────────────────────────
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("day-header");

        Label dayName = new Label(date.getDayOfWeek().getDisplayName(
                java.time.format.TextStyle.SHORT, java.util.Locale.FRENCH).toUpperCase());
        dayName.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + (isToday ? "#099aa7" : "#64748b") + ";");
        
        Label dateLabel = new Label(date.format(DATE_FMT));
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        header.getChildren().addAll(dayName, dateLabel);
        col.getChildren().add(header);

        // ── Content ────────────────────────────────────────────────────────────
        VBox content = new VBox(8);
        content.setPadding(new Insets(12, 8, 12, 8));
        content.setAlignment(Pos.TOP_CENTER);

        // Weather/Holiday Badges
        HBox badgeBox = new HBox(4);
        badgeBox.setAlignment(Pos.CENTER);

        if (!intel.weather.getCondition().equals("unknown")) {
            Label weatherIcon = new Label(intel.weather.getIcon());
            weatherIcon.setStyle("-fx-font-size: 16px;");
            Tooltip.install(weatherIcon, new Tooltip(intel.weather.getDescription()));
            badgeBox.getChildren().add(weatherIcon);
            
            if (isRisky) {
                Label risk = new Label("⚠");
                risk.getStyleClass().add("risk-badge");
                Tooltip.install(risk, new Tooltip("Risque météo identifié"));
                badgeBox.getChildren().add(risk);
            }
        }

        if (isHoliday) {
            Label hBadge = new Label("🎌");
            hBadge.getStyleClass().add("holiday-badge");
            Tooltip.install(hBadge, new Tooltip(intel.holiday.getDisplayName()));
            badgeBox.getChildren().add(hBadge);
        }

        if (!badgeBox.getChildren().isEmpty()) {
            content.getChildren().add(badgeBox);
        }

        if (isOff || isHoliday) {
            String blockReason = isHoliday ? intel.holiday.getDisplayName() : "CONGÉ MÉDECIN";
            Label offLabel = new Label(blockReason);
            offLabel.setWrapText(true);
            offLabel.setAlignment(Pos.CENTER);
            offLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 800; -fx-font-size: 10px; -fx-padding: 30 5; -fx-text-alignment: center;");
            content.getChildren().add(offLabel);
        } else {
            // ── Working day ────────────────────────────────────────────────────

            // Available time slots
            List<TimeSlot> slots = calendarService.generateSlots(date, tt, setting, indisponibilites, dayAppointments);
            if (!slots.isEmpty()) {
                Label slotsHint = new Label("CRÉNEAUX DISPONIBLES");
                slotsHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-font-weight: 700; -fx-padding: 10 0 5 0;");
                content.getChildren().add(slotsHint);

                for (TimeSlot slot : slots) {
                    if (!slot.isAvailable()) continue; // Only show free slots
                    Label sl = new Label(slot.toString());
                    sl.getStyleClass().add("slot-label");
                    sl.setCursor(Cursor.HAND);
                    sl.setOnMouseClicked(e -> handleSlotBooking(date, slot.getStartTime()));
                    content.getChildren().add(sl);
                }
            } else if (!isPast) {
                Label noSlots = new Label("COMPLET");
                noSlots.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-padding: 20 0;");
                content.getChildren().add(noSlots);
            }
        }

        col.getChildren().add(content);
        return col;
    }

    private void handleSlotBooking(LocalDate date, LocalTime time) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation de réservation");

        DialogPane pane = dialog.getDialogPane();
        String css = getClass().getResource("/css/style.css").toExternalForm();
        pane.getStylesheets().add(css);
        pane.setStyle("-fx-background-color: white;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        container.setPrefWidth(400);

        Label icon = new Label("✨");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Réserver ce créneau ?");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #f0fbfc; -fx-padding: 20; -fx-background-radius: 15; -fx-border-color: #099aa7; -fx-border-radius: 15; -fx-border-width: 1.5;");

        Label dateLbl = new Label(date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")));
        Label timeLbl = new Label(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        dateLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
        timeLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #099aa7;");
        infoBox.getChildren().addAll(dateLbl, timeLbl);

        Label hint = new Label("Ce rendez-vous sera ajouté instantanément à votre calendrier.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px; -fx-text-alignment: center;");

        container.getChildren().addAll(icon, title, infoBox, hint);
        pane.setContent(container);

        ButtonType btnConfirm = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Retour", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(btnConfirm, btnCancel);

        Button confirmBtn = (Button) pane.lookupButton(btnConfirm);
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setPrefWidth(140);

        Button cancelBtn = (Button) pane.lookupButton(btnCancel);
        cancelBtn.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #64748b;");
        cancelBtn.setPrefWidth(100);

        dialog.showAndWait().ifPresent(type -> {
            if (type == btnConfirm) {
                DataStore.getInstance().createAppointment(date, time, "Patient Test", "Consultation IA", "Rendez-vous réservé via Dashboard Intelligence.");
                refreshCalendar();
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // APPOINTMENT CARD
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a compact card for a single appointment, color-coded by status.
     */
    private VBox buildAppointmentCard(Appointment appt) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(4, 6, 4, 6));
        card.setStyle(buildCardStyle(appt.getStatus()));
        card.setAlignment(Pos.CENTER_LEFT);

        String timeStr = appt.getStartTime() != null
                ? appt.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "??:??";

        Label timeLbl = new Label("⏰ " + timeStr);
        timeLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #1f2f31;");

        Label nameLbl = new Label(appt.getPatientName() != null ? appt.getPatientName() : "Patient inconnu");
        nameLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #4a5568;");
        nameLbl.setWrapText(true);

        Label statusLbl = new Label(translateStatus(appt.getStatus()));
        statusLbl.setStyle(buildStatusBadgeStyle(appt.getStatus()));

        card.getChildren().addAll(timeLbl, nameLbl, statusLbl);

        // Tooltip with details
        Tooltip tip = new Tooltip(
                "Patient: " + appt.getPatientName() + "\n"
                + "Heure: " + timeStr + "\n"
                + "Département: " + (appt.getDepartment() != null ? appt.getDepartment() : "N/A") + "\n"
                + "Statut: " + translateStatus(appt.getStatus()) + "\n"
                + "Préférence: " + appt.getPreferencePeriod()
        );
        Tooltip.install(card, tip);

        return card;
    }

    private String buildCardStyle(String status) {
        String bg = switch (status == null ? "pending" : status.toLowerCase()) {
            case "scheduled" -> "#e8f5e9";
            case "completed" -> "#e3f2fd";
            case "cancelled" -> "#fce4ec";
            case "missed"    -> "#fff3e0";
            default          -> "#f5f5f5";
        };
        return String.format(
                "-fx-background-color: %s; -fx-background-radius: 6; "
                + "-fx-border-color: %s; -fx-border-radius: 6; -fx-border-width: 1;",
                bg, bg.replace("e8", "b").replace("e3", "b").replace("fc", "f").replace("ff", "f").replace("f5", "e"));
    }

    private String buildStatusBadgeStyle(String status) {
        String color = switch (status == null ? "pending" : status.toLowerCase()) {
            case "scheduled" -> "#2e7d32";
            case "completed" -> "#1565c0";
            case "cancelled" -> "#c62828";
            case "missed"    -> "#e65100";
            default          -> "#616161";
        };
        return String.format(
                "-fx-font-size: 8px; -fx-text-fill: %s; -fx-font-weight: bold; -fx-padding: 1 3;", color);
    }

    private String translateStatus(String status) {
        if (status == null) return "En attente";
        return switch (status.toLowerCase()) {
            case "scheduled" -> "Planifié";
            case "completed" -> "Terminé";
            case "cancelled" -> "Annulé";
            case "missed"    -> "Manqué";
            case "pending"   -> "En attente";
            default          -> status;
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WEATHER STRIP
    // ──────────────────────────────────────────────────────────────────────────

    private void updateWeatherStrip() {
        if (weatherStripLabel == null) return;

        // Check if any day this week has risky weather
        boolean anyRisky = false;
        StringBuilder sb = new StringBuilder("Météo semaine: ");
        for (int i = 0; i < 7; i++) {
            LocalDate d = currentWeekStart.plusDays(i);
            var w = recommendationMgr.getWeatherForDate(d);
            sb.append(w.getIcon()).append(" ");
            if (w.isRisky()) anyRisky = true;
        }
        weatherStripLabel.setText(sb.toString().trim());
        weatherStripLabel.setStyle(anyRisky
                ? "-fx-text-fill: #e74c3c; -fx-font-size: 12px;"
                : "-fx-text-fill: #099aa7; -fx-font-size: 12px;");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AI RECOMMENDATION PANEL
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Called when the doctor clicks "Obtenir Recommandation IA" button.
     * Fetches recommendation for the FIRST patient in the appointment list.
     */
    @FXML
    private void fetchAiRecommendation() {
        List<Appointment> appointments = DataStore.getInstance().getAppointments();
        if (appointments.isEmpty()) {
            showAiStatus("Aucun rendez-vous trouvé.", false);
            return;
        }
        // Use first patient found as default
        runRecommendationForPatient(appointments.get(0).getPatientId());
    }

    private void runRecommendationForPatient(int patientId) {
        showAiStatus("Analyse des préférences IA en cours...", true);
        int doctorId = 1; // DOCTOR_ID

        asyncPool.submit(() -> {
            AiRecommendation rec = recommendationMgr.getRecommendation(patientId, doctorId);
            Platform.runLater(() -> displayAiRecommendation(rec));
        });
    }

    private void displayAiRecommendation(AiRecommendation rec) {
        if (aiRecommendationPanel == null) return;

        aiRecommendationPanel.setVisible(true);
        aiRecommendationPanel.setManaged(true);

        if (aiRecommendationText != null)
            aiRecommendationText.setText(rec.getRecommendation() != null
                    ? rec.getRecommendation() : "Aucune recommandation disponible.");

        if (aiAttendanceLabel != null)
            aiAttendanceLabel.setText(rec.getAttendanceProbability() != null
                    ? "Probabilité de présence: " + rec.getAttendanceProbability()
                    : "Probabilité non disponible");

        AiRecommendation.SuggestedSlot best = rec.getBestSlot();
        if (aiBestSlotLabel != null) {
            if (best != null) {
                aiBestSlotLabel.setText(best.toString());
                aiBestSlotLabel.setCursor(Cursor.HAND);
                aiBestSlotLabel.setStyle("-fx-text-fill: #099aa7; -fx-font-weight: bold; -fx-underline: true;");
                aiBestSlotLabel.setOnMouseClicked(e -> handleSlotBooking(best.getDate(), best.getTime()));
            } else {
                aiBestSlotLabel.setText("Aucun créneau suggéré");
                aiBestSlotLabel.setCursor(Cursor.DEFAULT);
                aiBestSlotLabel.setStyle("-fx-text-fill: #94a3b8;");
                aiBestSlotLabel.setOnMouseClicked(null);
            }
        }

        if (aiStatusLabel != null)
            aiStatusLabel.setText(rec.isFallback() ? "⚠ Résultat de secours (IA hors ligne)" : "✓ Recommandation IA");

        aiStatusLabel.setStyle(rec.isFallback()
                ? "-fx-text-fill: #e67e22; -fx-font-size: 10px;"
                : "-fx-text-fill: #099aa7; -fx-font-size: 10px;");
    }

    private void showAiStatus(String message, boolean loading) {
        if (aiRecommendationPanel == null) return;
        aiRecommendationPanel.setVisible(true);
        aiRecommendationPanel.setManaged(true);
        if (aiRecommendationText != null)
            aiRecommendationText.setText(message);
        if (aiBestSlotLabel != null && loading)
            aiBestSlotLabel.setText("...");
    }

    @FXML
    private void hideAiPanel() {
        if (aiRecommendationPanel != null) {
            aiRecommendationPanel.setVisible(false);
            aiRecommendationPanel.setManaged(false);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATIENT HISTORY PANEL
    // ──────────────────────────────────────────────────────────────────────────

    private void buildPatientHistoryPanel(List<Appointment> appointments) {
        if (patientHistoryPanel == null) return;
        patientHistoryPanel.getChildren().clear();

        // patientId -> [morning_count, afternoon_count, evening_count]
        java.util.Map<Integer, int[]> prefs = new java.util.LinkedHashMap<>();
        // patientId -> name
        java.util.Map<Integer, String> names = new java.util.HashMap<>();

        for (Appointment a : appointments) {
            int id = a.getPatientId();
            String name = a.getPatientName() != null ? a.getPatientName() : "Inconnu";
            names.put(id, name);

            String period = a.getPreferencePeriod();
            prefs.computeIfAbsent(id, k -> new int[3]);
            switch (period) {
                case "morning"   -> prefs.get(id)[0]++;
                case "afternoon" -> prefs.get(id)[1]++;
                case "evening"   -> prefs.get(id)[2]++;
            }
        }

        Label header = new Label("AIDE AU PLANNING (cliquez)");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1f2f31; -fx-padding: 0 0 10 0;");
        patientHistoryPanel.getChildren().add(header);

        if (prefs.isEmpty()) {
            patientHistoryPanel.getChildren().add(styledLabel("Aucun historique.", "#a0aab0", 10));
            return;
        }

        for (var entry : prefs.entrySet()) {
            int patientId = entry.getKey();
            String name   = names.get(patientId);
            int[]  cnts   = entry.getValue();
            
            String prefStr;
            String prefColor;
            if (cnts[0] >= cnts[1] && cnts[0] >= cnts[2]) { prefStr = "☀️ Matin"; prefColor = "#099aa7"; }
            else if (cnts[1] >= cnts[2])                   { prefStr = "🌤 Après-midi"; prefColor = "#e67e22"; }
            else                                            { prefStr = "🌙 Soir"; prefColor = "#8e44ad"; }

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 8, 5, 8));
            row.setCursor(Cursor.HAND);
            row.setOnMouseClicked(e -> runRecommendationForPatient(patientId));

            Label nameLbl = styledLabel(name, "#2d3748", 11);
            nameLbl.setMinWidth(100);
            
            Label badge = new Label(prefStr);
            badge.setStyle("-fx-background-color: #f0fbfc; -fx-text-fill: " + prefColor + "; -fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");

            row.getChildren().addAll(nameLbl, badge);
            patientHistoryPanel.getChildren().add(row);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UI HELPERS
    // ──────────────────────────────────────────────────────────────────────────

    private Label styledLabel(String text, String color, int fontSize) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + fontSize + "px;");
        return lbl;
    }
}
