package com.pidev.controllers;

import com.pidev.models.*;
import com.pidev.services.CalendarDataService;
import com.pidev.services.RendezVousService;
import com.pidev.services.RecommendationManager;
import com.pidev.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class CalendarViewController {

    @FXML
    private Label weekLabel;

    @FXML
    private HBox weekGrid;

    // Settings Fields
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField slotDurationField;
    @FXML private TextField pauseStartField;
    @FXML private TextField pauseEndField;
    @FXML private Label statusLabel;
    @FXML private VBox settingsOverlay;

    private CalendarDataService calendarService;
    private RendezVousService rdvService;
    private RecommendationManager recommendationManager;
    private LocalDate currentWeekStart;
    private int doctorId;

    @FXML
    public void initialize() {
        calendarService = new CalendarDataService();
        rdvService = new RendezVousService();
        recommendationManager = new RecommendationManager();
        recommendationManager.loadHolidays();

        BaseUser user = UserSession.getInstance().getUser();
        if (user != null) {
            this.doctorId = user.getId();
        }

        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        loadSettings();
        refreshCalendar();
    }

    private void loadSettings() {
        try {
            TempsTravail tt = calendarService.getTempsTravail(doctorId);
            CalendarSetting setting = calendarService.getCalendarSetting(doctorId);

            if (tt != null) {
                startTimeField.setText(formatTime(tt.getStartTime()));
                endTimeField.setText(formatTime(tt.getEndTime()));
            }

            if (setting != null) {
                slotDurationField.setText(String.valueOf(setting.getSlotDuration()));
                pauseStartField.setText(setting.getPauseStart() != null ? formatTime(setting.getPauseStart()) : "");
                pauseEndField.setText(setting.getPauseEnd() != null ? formatTime(setting.getPauseEnd()) : "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatTime(java.time.LocalTime time) {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    @FXML
    private void saveSettings() {
        statusLabel.setText("");
        try {
            String startStr = startTimeField.getText().trim();
            String endStr = endTimeField.getText().trim();
            String durStr = slotDurationField.getText().trim();
            
            if (startStr.isEmpty() || endStr.isEmpty() || durStr.isEmpty()) {
                showError("Veuillez remplir les champs obligatoires");
                return;
            }

            java.time.LocalTime start = parseTime(startStr);
            java.time.LocalTime end = parseTime(endStr);
            int duration = Integer.parseInt(durStr);

            if (start == null || end == null) {
                showError("Format d'heure invalide (HH:mm)");
                return;
            }

            java.time.LocalTime pauseStart = null;
            java.time.LocalTime pauseEnd = null;
            if (!pauseStartField.getText().trim().isEmpty()) {
                pauseStart = parseTime(pauseStartField.getText().trim());
                if (pauseStart == null) {
                    showError("Format pause début invalide");
                    return;
                }
            }
            if (!pauseEndField.getText().trim().isEmpty()) {
                pauseEnd = parseTime(pauseEndField.getText().trim());
                if (pauseEnd == null) {
                    showError("Format pause fin invalide");
                    return;
                }
            }

            TempsTravail tt = new TempsTravail(0, "Monday", start, end, doctorId, null);
            CalendarSetting setting = new CalendarSetting(0, duration, pauseStart, pauseEnd, doctorId);

            calendarService.updateSettings(tt, setting);

            statusLabel.setText("✅ Paramètres enregistrés !");
            statusLabel.setStyle("-fx-text-fill: #10b981;");
            refreshCalendar();
        } catch (NumberFormatException e) {
            showError("La durée doit être un nombre");
        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement");
            e.printStackTrace();
        }
    }

    private java.time.LocalTime parseTime(String timeStr) {
        try {
            if (timeStr.length() == 4 && !timeStr.contains(":")) {
                // Handle 0800 format
                return java.time.LocalTime.of(Integer.parseInt(timeStr.substring(0, 2)), Integer.parseInt(timeStr.substring(2)));
            }
            if (timeStr.contains(":")) {
                String[] parts = timeStr.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                return java.time.LocalTime.of(h, m);
            }
            return java.time.LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(String msg) {
        statusLabel.setText("❌ " + msg);
        statusLabel.setStyle("-fx-text-fill: #f43f5e;");
    }

    @FXML
    private void toggleSettings() {
        if (settingsOverlay != null) {
            boolean isVisible = settingsOverlay.isVisible();
            settingsOverlay.setVisible(!isVisible);
            settingsOverlay.setManaged(!isVisible);
            if (!isVisible) {
                loadSettings(); // Refresh settings when opening
            }
        }
    }

    @FXML
    private void previousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        refreshCalendar();
    }

    @FXML
    private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        refreshCalendar();
    }

    private void refreshCalendar() {
        weekLabel.setText("Semaine du " + currentWeekStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        weekGrid.getChildren().clear();

        try {
            TempsTravail tt = calendarService.getTempsTravail(doctorId);
            CalendarSetting setting = calendarService.getCalendarSetting(doctorId);
            List<Indisponibilite> indisponibilites = calendarService.getIndisponibilites(doctorId);
            List<RendezVous> allAppointments = rdvService.getByMedecinId(doctorId);

            for (int i = 0; i < 7; i++) {
                LocalDate date = currentWeekStart.plusDays(i);
                VBox dayColumn = createDayColumn(date, tt, setting, indisponibilites, allAppointments);
                HBox.setHgrow(dayColumn, Priority.ALWAYS);
                weekGrid.getChildren().add(dayColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createDayColumn(LocalDate date, TempsTravail tt, CalendarSetting setting,
                                List<Indisponibilite> indisponibilites, List<RendezVous> appointments) {
        VBox col = new VBox();
        col.setSpacing(10);
        col.setStyle("-fx-border-color: #252d42; -fx-border-width: 0 1px 0 0; -fx-padding: 0 5px 0 5px;");
        col.setAlignment(Pos.TOP_CENTER);

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            col.setStyle("-fx-border-color: transparent; -fx-padding: 0 5px 0 5px;");
        }

        Label dayName = new Label(date.getDayOfWeek().name());
        dayName.setStyle("-fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM")));
        dateLabel.setStyle("-fx-text-fill: #94a3b8;");

        // Intelligence Label
        RecommendationManager.DayIntelligence intel = recommendationManager.getDayIntelligence(date);
        Label intelLabel = new Label(intel.getAlertLabel());
        intelLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 10px;");
        intelLabel.setWrapText(true);

        col.getChildren().addAll(dayName, dateLabel, intelLabel);

        boolean isOff = indisponibilites.stream().anyMatch(ind -> ind.getDate().equals(date));
        boolean isPast = date.isBefore(LocalDate.now());

        if (isOff) {
            Label offLabel = new Label("Jour de congé");
            offLabel.setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold; -fx-padding: 20px 0;");

            Button cancelOffBtn = new Button("Annuler congé");
            cancelOffBtn.getStyleClass().add("btn-secondary");
            if (isPast) {
                cancelOffBtn.setDisable(true);
            } else {
                cancelOffBtn.setOnAction(e -> {
                    try {
                        calendarService.removeIndisponibilite(doctorId, date);
                        refreshCalendar();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }
            col.getChildren().addAll(offLabel, cancelOffBtn);
        } else {
            Button takeOffBtn = new Button("Prendre Congé");
            takeOffBtn.getStyleClass().add("btn-primary");
            takeOffBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");

            boolean hasAppointments = appointments.stream().anyMatch(r -> r.getLocalDate().equals(date));

            if (isPast) {
                takeOffBtn.setDisable(true);
                takeOffBtn.setText("Date passée");
                takeOffBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #64748b; -fx-font-size: 11px;");
            } else if (hasAppointments) {
                takeOffBtn.setDisable(true);
                takeOffBtn.setText("RDV existants");
                takeOffBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #f43f5e; -fx-font-size: 11px;");
            } else {
                takeOffBtn.setOnAction(e -> {
                    try {
                        calendarService.addIndisponibilite(doctorId, date, false);
                        refreshCalendar();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }
            col.getChildren().add(takeOffBtn);

            ListView<String> slotsView = new ListView<>();
            slotsView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0;");
            VBox.setVgrow(slotsView, Priority.ALWAYS);

            List<TimeSlot> slots = calendarService.generateSlots(date, tt, setting, indisponibilites, appointments);
            ObservableList<String> items = FXCollections.observableArrayList();
            
            if (isPast) {
                // For past dates, only show booked slots (confirmed appointments)
                boolean found = false;
                for (RendezVous r : appointments) {
                    if (r.getLocalDate().equals(date)) {
                        items.add(r.getLocalTime().toString() + " (Rendez-vous)");
                        found = true;
                    }
                }
                if (!found) items.add("Aucune activité");
            } else {
                if (slots.isEmpty()) {
                    items.add("Aucun créneau");
                } else {
                    for (TimeSlot s : slots) {
                        items.add(s.toString() + (s.isAvailable() ? "" : " (Occupé)"));
                    }
                }
            }
            slotsView.setItems(items);
            col.getChildren().add(slotsView);
        }

        return col;
    }
}
