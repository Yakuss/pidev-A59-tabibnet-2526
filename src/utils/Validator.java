package utils;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import entity.RendezVous;

public class Validator {

    // Check if a string is empty or null
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Validate time format HH:mm
    public static boolean isValidTime(String heure) {
        if (isEmpty(heure)) return false;
        return heure.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    // Validate date format dd/MM/yyyy
    public static boolean isValidDate(String date) {
        if (isEmpty(date)) return false;
        return date.matches(
                "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/[0-9]{4}$"
        );
    }

    // Blocage des dates passées
    public static boolean isPastDate(String dateStr) {
        if (!isValidDate(dateStr)) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return date.isBefore(LocalDate.now()); // Vérifie si la date est hier ou plus ancienne
        } catch (Exception e) {
            return false;
        }
    }

    // Validate note between 1 and 5
    public static boolean isValidNote(int note) {
        return note >= 1 && note <= 5;
    }

    // UNIQUENESS TEST — very important for full marks!
    public static boolean isDuplicate(List<RendezVous> list,
                                      String nom, String date, String heure, int excludeId) {
        for (RendezVous r : list) {
            if (r.getId() == excludeId) continue; // skip when editing
            if (r.getNomPatient().equalsIgnoreCase(nom)
                    && r.getDate().equals(date)
                    && r.getHeure().equals(heure)) {
                return true; // duplicate found!
            }
        }
        return false;
    }
}