package com.tabibnet.calendar.model;

import java.time.LocalDate;

/**
 * Holiday — represents a Tunisian national holiday (jour férié).
 * Populated from GET /api/intelligence/holidays via ApiService.
 */
public class Holiday {

    private LocalDate date;
    /** Translated local name (French/Arabic) */
    private String localName;
    /** Official English name */
    private String name;

    public Holiday() {}

    public Holiday(LocalDate date, String localName, String name) {
        this.date      = date;
        this.localName = localName;
        this.name      = name;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public LocalDate getDate()            { return date; }
    public void setDate(LocalDate date)   { this.date = date; }

    public String getLocalName()          { return localName; }
    public void setLocalName(String n)    { this.localName = n; }

    public String getName()               { return name; }
    public void setName(String n)         { this.name = n; }

    /** Display label for UI (prefer localName, fall back to name). */
    public String getDisplayName() {
        return (localName != null && !localName.isBlank()) ? localName : name;
    }

    @Override
    public String toString() {
        return String.format("Holiday[%s — %s]", date, getDisplayName());
    }
}
