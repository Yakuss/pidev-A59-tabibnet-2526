package com.pidev.models;

import java.time.LocalDateTime;

public class Ordonnance {
    private int id;
    private LocalDateTime dateOrdonnance;
    private String diagnosis;
    private String medicament;
    private String posologie;
    private String instructions;
    private String notes;
    private int patientId;
    private int medecinId;
    private Integer appointmentId;
    private Integer documentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Associations
    private Patient patient;
    private Document document;

    public Ordonnance() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDateOrdonnance() { return dateOrdonnance; }
    public void setDateOrdonnance(LocalDateTime dateOrdonnance) { this.dateOrdonnance = dateOrdonnance; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getMedicament() { return medicament; }
    public void setMedicament(String medicament) { this.medicament = medicament; }
    public String getPosologie() { return posologie; }
    public void setPosologie(String posologie) { this.posologie = posologie; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public Integer getDocumentId() { return documentId; }
    public void setDocumentId(Integer documentId) { this.documentId = documentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
}
