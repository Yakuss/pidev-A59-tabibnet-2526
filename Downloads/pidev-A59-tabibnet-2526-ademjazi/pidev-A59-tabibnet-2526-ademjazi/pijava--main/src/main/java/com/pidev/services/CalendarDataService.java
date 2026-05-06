package com.pidev.services;

import com.pidev.models.*;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Doctor Calendar settings (working hours, unavailability, slots).
 */
public class CalendarDataService {

    private Connection getConnection() {
        return DataSource.getInstance().getConnection();
    }

    public TempsTravail getTempsTravail(int doctorId) throws SQLException {
        String sql = "SELECT * FROM temps_travail WHERE doctor_id = ? LIMIT 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new TempsTravail(
                        rs.getInt("id"),
                        rs.getString("day_of_week"),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getInt("doctor_id"),
                        rs.getDate("specific_date") != null ? rs.getDate("specific_date").toLocalDate() : null);
            }
        }
        // Default fallback
        return new TempsTravail(0, "Monday", LocalTime.of(8, 0), LocalTime.of(17, 0), doctorId, null);
    }

    public CalendarSetting getCalendarSetting(int doctorId) throws SQLException {
        String sql = "SELECT * FROM calendar_setting WHERE doctor_id = ? LIMIT 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Time pStart = rs.getTime("pause_start");
                Time pEnd = rs.getTime("pause_end");
                return new CalendarSetting(
                        rs.getInt("id"),
                        rs.getInt("slot_duration"),
                        pStart != null ? pStart.toLocalTime() : null,
                        pEnd != null ? pEnd.toLocalTime() : null,
                        rs.getInt("doctor_id"));
            }
        }
        // Default fallback
        return new CalendarSetting(0, 30, LocalTime.of(12, 0), LocalTime.of(13, 0), doctorId);
    }

    public List<Indisponibilite> getIndisponibilites(int doctorId) throws SQLException {
        List<Indisponibilite> list = new ArrayList<>();
        String sql = "SELECT * FROM indisponibilite WHERE doctor_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Indisponibilite(
                        rs.getInt("id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getInt("doctor_id"),
                        rs.getBoolean("is_emergency")));
            }
        }
        return list;
    }

    public void addIndisponibilite(int doctorId, LocalDate date, boolean isEmergency) throws SQLException {
        String sql = "INSERT INTO indisponibilite (date, doctor_id, is_emergency) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setInt(2, doctorId);
            stmt.setBoolean(3, isEmergency);
            stmt.executeUpdate();
        }
    }

    public void removeIndisponibilite(int doctorId, LocalDate date) throws SQLException {
        String sql = "DELETE FROM indisponibilite WHERE date = ? AND doctor_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setInt(2, doctorId);
            stmt.executeUpdate();
        }
    }

    public void updateSettings(TempsTravail tt, CalendarSetting setting) throws SQLException {
        Connection conn = getConnection();
        // Update TempsTravail
        String checkTt = "SELECT id FROM temps_travail WHERE doctor_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkTt)) {
            ps.setInt(1, tt.getDoctorId());
            if (ps.executeQuery().next()) {
                String update = "UPDATE temps_travail SET start_time=?, end_time=? WHERE doctor_id=?";
                try (PreparedStatement up = conn.prepareStatement(update)) {
                    up.setTime(1, Time.valueOf(tt.getStartTime()));
                    up.setTime(2, Time.valueOf(tt.getEndTime()));
                    up.setInt(3, tt.getDoctorId());
                    up.executeUpdate();
                }
            } else {
                String insert = "INSERT INTO temps_travail (day_of_week, start_time, end_time, doctor_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setString(1, tt.getDayOfWeek());
                    ins.setTime(2, Time.valueOf(tt.getStartTime()));
                    ins.setTime(3, Time.valueOf(tt.getEndTime()));
                    ins.setInt(4, tt.getDoctorId());
                    ins.executeUpdate();
                }
            }
        }

        // Update CalendarSetting
        String checkCs = "SELECT id FROM calendar_setting WHERE doctor_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkCs)) {
            ps.setInt(1, setting.getDoctorId());
            if (ps.executeQuery().next()) {
                String update = "UPDATE calendar_setting SET slot_duration=?, pause_start=?, pause_end=? WHERE doctor_id=?";
                try (PreparedStatement up = conn.prepareStatement(update)) {
                    up.setInt(1, setting.getSlotDuration());
                    up.setTime(2, setting.getPauseStart() != null ? Time.valueOf(setting.getPauseStart()) : null);
                    up.setTime(3, setting.getPauseEnd() != null ? Time.valueOf(setting.getPauseEnd()) : null);
                    up.setInt(4, setting.getDoctorId());
                    up.executeUpdate();
                }
            } else {
                String insert = "INSERT INTO calendar_setting (slot_duration, pause_start, pause_end, doctor_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setInt(1, setting.getSlotDuration());
                    ins.setTime(2, setting.getPauseStart() != null ? Time.valueOf(setting.getPauseStart()) : null);
                    ins.setTime(3, setting.getPauseEnd() != null ? Time.valueOf(setting.getPauseEnd()) : null);
                    ins.setInt(4, setting.getDoctorId());
                    ins.executeUpdate();
                }
            }
        }
    }

    public List<TimeSlot> generateSlots(LocalDate date, TempsTravail tempsTravail, CalendarSetting setting,
                                       List<Indisponibilite> indisponibilites, List<RendezVous> appointments) {
        List<TimeSlot> slots = new ArrayList<>();

        if (indisponibilites != null) {
            for (Indisponibilite i : indisponibilites) {
                if (i.getDate().equals(date)) return slots;
            }
        }

        if (tempsTravail == null || setting == null) return slots;

        LocalTime currentStartTime = tempsTravail.getStartTime();
        LocalTime endTime = tempsTravail.getEndTime();
        int duration = setting.getSlotDuration();
        LocalTime pauseStart = setting.getPauseStart();
        LocalTime pauseEnd = setting.getPauseEnd();

        while (currentStartTime.plusMinutes(duration).compareTo(endTime) <= 0) {
            LocalTime slotEndTime = currentStartTime.plusMinutes(duration);

            boolean isPause = false;
            if (pauseStart != null && pauseEnd != null) {
                if ((currentStartTime.compareTo(pauseStart) >= 0 && currentStartTime.compareTo(pauseEnd) < 0) ||
                    (slotEndTime.compareTo(pauseStart) > 0 && slotEndTime.compareTo(pauseEnd) <= 0)) {
                    isPause = true;
                }
            }

            boolean isBooked = false;
            if (appointments != null) {
                for (RendezVous r : appointments) {
                    if (date.equals(r.getLocalDate()) && currentStartTime.equals(r.getLocalTime())) {
                        isBooked = true;
                        break;
                    }
                }
            }

            if (!isPause) {
                slots.add(new TimeSlot(currentStartTime, slotEndTime, !isBooked));
            }
            currentStartTime = slotEndTime;
        }
        return slots;
    }
}
