package com.pidev.models;

/**
 * Rendezvous (Appointment) Model
 */
public class RendezVous {
    private int id;
    private int patientId;
    private int medecinId;
    private String date;      // format: dd/MM/yyyy
    private String heure;     // format: HH:mm
    private String statut;    // "En attente", "Terminé", "Annulé"

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

    // Legacy constructor for backward compatibility
    public RendezVous(int id, String nomPatient, String medecin, String date, String heure, String statut) {
        this.id = id;
        this.nomPatient = nomPatient;
        this.medecin = medecin;
        this.date = date;
        this.heure = heure;
        this.statut = statut != null ? statut : "En attente";
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

    // Legacy getters/setters for backward compatibility
    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getMedecin() { return medecin; }
    public void setMedecin(String medecin) { this.medecin = medecin; }

    @Override
    public String toString() {
        return "RDV#" + id + " - Patient:" + patientId + " avec Médecin:" + medecinId
                + " le " + date + " à " + heure + " [" + statut + "]";
    }
}