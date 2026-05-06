package com.tabibnet.calendar.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AiRecommendation — result from GET /api/intelligence/recommendation/{p}/{d}.
 * Contains the AI's best-slot suggestion and patient attendance analysis.
 */
public class AiRecommendation {

    private int patientId;
    private int doctorId;
    private String patientName;
    private String doctorName;
    /** Human-readable explanation produced by the LLM. */
    private String recommendation;
    /** e.g. "87%" */
    private String attendanceProbability;
    /** List of suggested {date, time} pairs. */
    private List<SuggestedSlot> suggestedSlots = new ArrayList<>();
    /** True when this is a local-fallback result (API unavailable). */
    private boolean isFallback = false;

    public AiRecommendation() {}

    // ── Inner class: SuggestedSlot ─────────────────────────────────────────

    public static class SuggestedSlot {
        private LocalDate date;
        private LocalTime time;

        public SuggestedSlot(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }

        public LocalDate getDate()          { return date; }
        public void setDate(LocalDate d)    { this.date = d; }

        public LocalTime getTime()          { return time; }
        public void setTime(LocalTime t)    { this.time = t; }

        @Override
        public String toString() {
            return date + " à " + time;
        }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int getPatientId()                           { return patientId; }
    public void setPatientId(int id)                    { this.patientId = id; }

    public int getDoctorId()                            { return doctorId; }
    public void setDoctorId(int id)                     { this.doctorId = id; }

    public String getPatientName()                      { return patientName; }
    public void setPatientName(String n)                { this.patientName = n; }

    public String getDoctorName()                       { return doctorName; }
    public void setDoctorName(String n)                 { this.doctorName = n; }

    public String getRecommendation()                   { return recommendation; }
    public void setRecommendation(String r)             { this.recommendation = r; }

    public String getAttendanceProbability()            { return attendanceProbability; }
    public void setAttendanceProbability(String p)      { this.attendanceProbability = p; }

    public List<SuggestedSlot> getSuggestedSlots()      { return suggestedSlots; }
    public void setSuggestedSlots(List<SuggestedSlot> s){ this.suggestedSlots = s; }

    public boolean isFallback()                         { return isFallback; }
    public void setFallback(boolean f)                  { this.isFallback = f; }

    /** Convenience: returns first suggested slot, or null. */
    public SuggestedSlot getBestSlot() {
        return suggestedSlots.isEmpty() ? null : suggestedSlots.get(0);
    }

    @Override
    public String toString() {
        return String.format("AiRec[patient=%d, doctor=%d, slots=%d, prob=%s]",
                patientId, doctorId, suggestedSlots.size(), attendanceProbability);
    }
}
