package edu.connexion3a77.entities;

import java.util.Date;

public class Rapport {
    private int id;
    private String consultationReason;
    private String diagnosis;
    private String observations;
    private String recommendations;
    private String treatments;
    private Date createdAt;
    private Date updatedAt;

    private Patient patient;
    private Medecin medecin;
    private Appointment appointment;
    private Document document;

    public Rapport() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Rapport(String consultationReason, String diagnosis, String observations, String recommendations, String treatments, Patient patient, Medecin medecin, Appointment appointment) {
        this();
        this.consultationReason = consultationReason;
        this.diagnosis = diagnosis;
        this.observations = observations;
        this.recommendations = recommendations;
        this.treatments = treatments;
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

    public String getConsultationReason() {
        return consultationReason;
    }

    public void setConsultationReason(String consultationReason) {
        this.consultationReason = consultationReason;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getTreatments() {
        return treatments;
    }

    public void setTreatments(String treatments) {
        this.treatments = treatments;
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
