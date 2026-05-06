package com.pidev.models;

import java.time.LocalDate;

/**
 * WeatherInfo — represents a weather forecast for a specific date/location.
 */
public class WeatherInfo {

    private LocalDate date;
    private String location;
    /** "clear" | "bad" */
    private String condition;
    private boolean risky;
    private String description;
    /** Optional emoji/text icon: ☀️ 🌧️ ⛈️ ❄️ 🌫️ */
    private String icon;

    public WeatherInfo() {}

    public WeatherInfo(LocalDate date, String location,
                       String condition, boolean risky, String description) {
        this.date        = date;
        this.location    = location;
        this.condition   = condition;
        this.risky       = risky;
        this.description = description;
        this.icon        = resolveIcon(condition, description);
    }

    /** Pick a simple text icon based on condition / description keywords. */
    private String resolveIcon(String cond, String desc) {
        if (cond == null || "clear".equalsIgnoreCase(cond)) return "☀";
        String d = (desc == null ? "" : desc.toLowerCase());
        if (d.contains("thunder") || d.contains("orage"))   return "⛈";
        if (d.contains("snow")    || d.contains("neige"))   return "❄";
        if (d.contains("fog")     || d.contains("brouill")) return "🌫";
        return "🌧";   // default bad weather
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public LocalDate getDate()              { return date; }
    public void setDate(LocalDate date)     { this.date = date; }

    public String getLocation()             { return location; }
    public void setLocation(String loc)     { this.location = loc; }

    public String getCondition()            { return condition; }
    public void setCondition(String c)      { this.condition = c; this.icon = resolveIcon(c, description); }

    public boolean isRisky()               { return risky; }
    public void setRisky(boolean r)        { this.risky = r; }

    public String getDescription()          { return description; }
    public void setDescription(String d)    { this.description = d; }

    public String getIcon()                 { return icon; }
    public void setIcon(String icon)        { this.icon = icon; }

    @Override
    public String toString() {
        return String.format("Weather[%s %s — %s%s]",
                date, icon, description, risky ? " ⚠ RISQUE" : "");
    }
}
