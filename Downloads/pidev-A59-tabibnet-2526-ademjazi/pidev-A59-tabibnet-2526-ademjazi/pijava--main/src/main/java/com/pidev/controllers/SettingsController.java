package com.pidev.controllers;

import com.pidev.models.CalendarSetting;
import com.pidev.models.TempsTravail;
import com.pidev.services.CalendarDataService;
import com.pidev.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class SettingsController {

    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField slotDurationField;
    @FXML private TextField pauseStartField;
    @FXML private TextField pauseEndField;
    @FXML private Label statusLabel;

    private final CalendarDataService calendarService = new CalendarDataService();
    private int doctorId;

    @FXML
    public void initialize() {
        if (UserSession.getInstance().getUser() != null) {
            this.doctorId = UserSession.getInstance().getUser().getId();
        }

        try {
            TempsTravail tt = calendarService.getTempsTravail(doctorId);
            CalendarSetting setting = calendarService.getCalendarSetting(doctorId);

            if (tt != null) {
                startTimeField.setText(tt.getStartTime().toString());
                endTimeField.setText(tt.getEndTime().toString());
            }

            if (setting != null) {
                slotDurationField.setText(String.valueOf(setting.getSlotDuration()));
                pauseStartField.setText(setting.getPauseStart() != null ? setting.getPauseStart().toString() : "");
                pauseEndField.setText(setting.getPauseEnd() != null ? setting.getPauseEnd().toString() : "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveSettings() {
        try {
            LocalTime start = LocalTime.parse(startTimeField.getText());
            LocalTime end = LocalTime.parse(endTimeField.getText());
            int duration = Integer.parseInt(slotDurationField.getText());

            LocalTime pauseStart = null;
            LocalTime pauseEnd = null;
            if (!pauseStartField.getText().isEmpty()) {
                pauseStart = LocalTime.parse(pauseStartField.getText());
            }
            if (!pauseEndField.getText().isEmpty()) {
                pauseEnd = LocalTime.parse(pauseEndField.getText());
            }

            TempsTravail tt = new TempsTravail(0, "Monday", start, end, doctorId, null);
            CalendarSetting setting = new CalendarSetting(0, duration, pauseStart, pauseEnd, doctorId);

            calendarService.updateSettings(tt, setting);

            statusLabel.setText("Paramètres enregistrés avec succès !");
            statusLabel.setStyle("-fx-text-fill: #10b981;");
        } catch (DateTimeParseException | NumberFormatException | SQLException e) {
            statusLabel.setText("Erreur : Format invalide. Utilisez HH:mm");
            statusLabel.setStyle("-fx-text-fill: #f43f5e;");
        }
    }
}
