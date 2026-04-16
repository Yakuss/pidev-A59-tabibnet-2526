package com.pidev.models;

import java.time.LocalDateTime;

/**
 * Appointment entity - links Patient and Medecin.
 */
public class Appointment {
    private int id;
    private LocalDateTime date;
    private LocalDateTime startTime;
    private int duration;
    private String status;
    private String message;
    private String department;
    private int patientId;
    private int doctorId;
    private LocalDateTime createdAt;
    private boolean reminderSent;

    // Display helpers (populated from JOINs)
    private String patientName;
    private String doctorName;

    public Appointment() {}

    public Appointment(int id, LocalDateTime date, LocalDateTime startTime, int duration,
                       String status, String message, String department,
                       int patientId, int doctorId, LocalDateTime createdAt, boolean reminderSent) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.duration = duration;
        this.status = status;
        this.message = message;
        this.department = department;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.createdAt = createdAt;
        this.reminderSent = reminderSent;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    @Override
    public String toString() {
        return "Appointment #" + id + " - " + status + " (" + date + ")";
    }
}
