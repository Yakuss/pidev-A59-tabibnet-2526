package com.pidev.models;

import java.time.LocalDateTime;

/**
 * Feedback entity - rating and comment for appointments.
 */
public class Feedback {
    private int id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private Double sentimentScore;
    private int patientId;
    private int medecinId;
    private int appointmentId;

    // Display helpers
    private String patientName;
    private String medecinName;

    public Feedback() {}

    public Feedback(int id, int rating, String comment, LocalDateTime createdAt,
                    Double sentimentScore, int patientId, int medecinId, int appointmentId) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.sentimentScore = sentimentScore;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.appointmentId = appointmentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getMedecinName() { return medecinName; }
    public void setMedecinName(String medecinName) { this.medecinName = medecinName; }

    @Override
    public String toString() {
        return "Feedback #" + id + " - Rating: " + rating + "/5";
    }
}
