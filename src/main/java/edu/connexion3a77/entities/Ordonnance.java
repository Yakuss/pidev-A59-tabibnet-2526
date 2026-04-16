package edu.connexion3a77.entities;

import java.util.Date;

public class Ordonnance {
    private int id;
    private Date dateOrdonnance;
    private String diagnosis;
    private String medicament;
    private String posologie;
    private String notes;
    private String instructions;
    private Date createdAt;
    private Date updatedAt;

    private Patient patient;
    private Medecin medecin;
    private Appointment appointment;
    private Document document;

    public Ordonnance() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Ordonnance(Date dateOrdonnance, String diagnosis, String medicament, String posologie, String notes, String instructions, Patient patient, Medecin medecin, Appointment appointment) {
        this();
        this.dateOrdonnance = dateOrdonnance;
        this.diagnosis = diagnosis;
        this.medicament = medicament;
        this.posologie = posologie;
        this.notes = notes;
        this.instructions = instructions;
        this.patient = patient;
        this.medecin = medecin;
        this.appointment = appointment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateOrdonnance() {
        return dateOrdonnance;
    }

    public void setDateOrdonnance(Date dateOrdonnance) {
        this.dateOrdonnance = dateOrdonnance;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMedicament() {
        return medicament;
    }

    public void setMedicament(String medicament) {
        this.medicament = medicament;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
