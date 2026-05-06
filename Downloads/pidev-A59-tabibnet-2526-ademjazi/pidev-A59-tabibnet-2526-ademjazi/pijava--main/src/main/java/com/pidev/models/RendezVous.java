package com.pidev.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Rendezvous (Appointment) Model
 * Updated to include fields needed for the Calendar and Intelligence modules.
 */
public class RendezVous {
    private int id;
    private int patientId;
    private int medecinId;
    private String date;      // dd/MM/yyyy
    private String heure;     // HH:mm
    private String statut;    // "En attente", "Terminé", "Annulé", "scheduled", "pending", "completed", "cancelled", "missed"
    
    // New fields for calendar compatibility
    private int durationMinutes = 30;
    private String department;
    private String message;
    
    // Legacy fields for backward compatibility
    private String nomPatient;
    private String medecin;

    public RendezVous() {}

    public RendezVous(int id, int patientId, int medecinId, String date, String heure, String statut) {
        this.id = id;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.date = date;
        this.heure = heure;
        this.statut = statut != null ? statut : "En attente";
    }

    public RendezVous(int id, int patientId, int medecinId, String date, String heure) {
        this(id, patientId, medecinId, date, heure, "En attente");
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getMedecin() { return medecin; }
    public void setMedecin(String medecin) { this.medecin = medecin; }

    // Helper methods for Calendar compatibility
    public LocalDate getLocalDate() {
        if (date == null || date.isEmpty()) return null;
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(date); // Fallback for yyyy-MM-dd
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public void setLocalDate(LocalDate localDate) {
        if (localDate != null) {
            this.date = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public LocalTime getLocalTime() {
        if (heure == null || heure.isEmpty()) return null;
        try {
            return LocalTime.parse(heure);
        } catch (Exception e) {
            return null;
        }
    }

    public void setLocalTime(LocalTime localTime) {
        if (localTime != null) {
            this.heure = localTime.toString();
        }
    }

    /** Returns a CSS style class name based on status for calendar coloring. */
    public String getStatusStyleClass() {
        String s = (statut == null ? "pending" : statut.toLowerCase());
        return switch (s) {
            case "scheduled", "confirmé" -> "appt-scheduled";
            case "completed", "terminé" -> "appt-completed";
            case "cancelled", "annulé" -> "appt-cancelled";
            case "missed" -> "appt-missed";
            default -> "appt-pending";
        };
    }

    @Override
    public String toString() {
        return "RDV#" + id + " - Patient:" + patientId + " avec Médecin:" + medecinId
                + " le " + date + " à " + heure + " [" + statut + "]";
    }
}