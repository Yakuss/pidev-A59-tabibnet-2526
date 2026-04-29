package com.pidev.utils;

import com.pidev.models.RendezVous;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Utility class for validating appointment and feedback data
 */
public class Validator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Check if a string is empty or null
     */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Validate date format (dd/MM/yyyy)
     */
    public static boolean isValidDate(String date) {
        if (isEmpty(date)) {
            return false;
        }
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate time format (HH:mm)
     */
    public static boolean isValidTime(String time) {
        if (isEmpty(time)) {
            return false;
        }
        try {
            java.time.LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate feedback note (1-5)
     */
    public static boolean isValidNote(int note) {
        return note >= 1 && note <= 5;
    }

    /**
     * Check if an appointment with same patient, doctor, date, and time already exists
     * Excludes the current appointment being edited (by ID)
     */
    public static boolean isDuplicate(List<RendezVous> appointments, 
                                      String nomPatient, String date, String heure, int excludeId) {
        if (appointments == null || appointments.isEmpty()) {
            return false;
        }

        for (RendezVous rdv : appointments) {
            // Skip the appointment being edited
            if (rdv.getId() == excludeId) {
                continue;
            }

            // Check if same patient, date, and time
            if (rdv.getNomPatient() != null && 
                rdv.getNomPatient().equalsIgnoreCase(nomPatient) &&
                rdv.getDate() != null && rdv.getDate().equals(date) &&
                rdv.getHeure() != null && rdv.getHeure().equals(heure)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate patient name (not empty, reasonable length)
     */
    public static boolean isValidPatientName(String name) {
        if (isEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 255;
    }

    /**
     * Validate doctor name (not empty, reasonable length)
     */
    public static boolean isValidDoctorName(String name) {
        if (isEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 255;
    }

    /**
     * Validate appointment status
     */
    public static boolean isValidStatus(String status) {
        if (isEmpty(status)) {
            return false;
        }
        return status.equals("En attente") || 
               status.equals("Terminé") || 
               status.equals("Annulé");
    }

    /**
     * Validate feedback comment (not empty, reasonable length)
     */
    public static boolean isValidComment(String comment) {
        if (isEmpty(comment)) {
            return false;
        }
        String trimmed = comment.trim();
        return trimmed.length() >= 5 && trimmed.length() <= 1000;
    }

    /**
     * Check if date is not in the past
     */
    public static boolean isDateNotInPast(String date) {
        if (!isValidDate(date)) {
            return false;
        }
        try {
            LocalDate appointmentDate = LocalDate.parse(date, DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            return !appointmentDate.isBefore(today);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Check if date is today or in the future
     */
    public static boolean isDateTodayOrLater(String date) {
        return isDateNotInPast(date);
    }
}
