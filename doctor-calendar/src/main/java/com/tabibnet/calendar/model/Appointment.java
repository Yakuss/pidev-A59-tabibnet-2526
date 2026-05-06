package com.tabibnet.calendar.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment model — mirrors the Symfony Appointment entity.
 * Loaded from the local MySQL DB via DataStore and also returned
 * by the /api/calendar/config endpoint.
 */
public class Appointment {

    private int id;
    private int patientId;
    private String patientName;
    private LocalDate date;
    private LocalTime startTime;
    private int durationMinutes;
    /** scheduled | pending | completed | cancelled | missed */
    private String status;
    private String department;
    private String message;

    public Appointment() {}

    public Appointment(int id, int patientId, String patientName,
                       LocalDate date, LocalTime startTime,
                       int durationMinutes, String status) {
        this.id              = id;
        this.patientId       = patientId;
        this.patientName     = patientName;
        this.date            = date;
        this.startTime       = startTime;
        this.durationMinutes = durationMinutes;
        this.status          = status;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getPatientId()               { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName()                    { return patientName; }
    public void setPatientName(String patientName)    { this.patientName = patientName; }

    public LocalDate getDate()              { return date; }
    public void setDate(LocalDate date)     { this.date = date; }

    public LocalTime getStartTime()         { return startTime; }
    public void setStartTime(LocalTime t)   { this.startTime = t; }

    public int getDurationMinutes()         { return durationMinutes; }
    public void setDurationMinutes(int d)   { this.durationMinutes = d; }

    public String getStatus()               { return status; }
    public void setStatus(String status)    { this.status = status; }

    public String getDepartment()           { return department; }
    public void setDepartment(String dept)  { this.department = dept; }

    public String getMessage()              { return message; }
    public void setMessage(String msg)      { this.message = msg; }

    /** Returns a CSS style class name based on status for calendar coloring. */
    public String getStatusStyleClass() {
        return switch (status == null ? "pending" : status.toLowerCase()) {
            case "scheduled" -> "appt-scheduled";
            case "completed" -> "appt-completed";
            case "cancelled" -> "appt-cancelled";
            case "missed"    -> "appt-missed";
            default          -> "appt-pending";
        };
    }

    /** Preference period (morning / afternoon / evening) derived from startTime. */
    public String getPreferencePeriod() {
        if (startTime == null) return "unknown";
        int hour = startTime.getHour();
        if (hour < 12) return "morning";
        if (hour < 17) return "afternoon";
        return "evening";
    }

    @Override
    public String toString() {
        return String.format("[Appt#%d] %s %s → %s (%s)",
                id, patientName, date, startTime, status);
    }
}
