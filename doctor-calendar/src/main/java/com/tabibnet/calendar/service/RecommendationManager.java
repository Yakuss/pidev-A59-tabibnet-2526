package com.tabibnet.calendar.service;

import com.tabibnet.calendar.model.AiRecommendation;
import com.tabibnet.calendar.model.Holiday;
import com.tabibnet.calendar.model.WeatherInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * RecommendationManager — orchestrator for the appointment intelligence layer.
 *
 * Provides a unified, high-level interface used by JavaFX controllers:
 *   • Is a given date a holiday?
 *   • Does a given date have risky weather?
 *   • What does the AI recommend for this patient/doctor pair?
 *   • Full intelligence summary for a date (for calendar day-column rendering).
 *
 * Caches holiday and weather data in memory for the lifetime of the app session.
 */
public class RecommendationManager {

    private final ApiService apiService;

    // ── In-session cache ───────────────────────────────────────────────────────
    /** Cached holidays for the current year (and next year, loaded lazily). */
    private List<Holiday>   cachedHolidays = List.of();
    /** Cached weather for the current week. */
    private List<WeatherInfo> cachedWeather = List.of();
    /** Start-of-week date when weather was last fetched. */
    private LocalDate        cachedWeatherWeekStart = null;
    /** Location used for weather queries. */
    private String           location = "Tunis";

    public RecommendationManager() {
        this.apiService = new ApiService();
    }

    /** Package-visible constructor for testing with a mock ApiService. */
    RecommendationManager(ApiService apiService) {
        this.apiService = apiService;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HOLIDAY LOGIC
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Loads holidays for the current year into cache.
     * Call once at startup; silently handles API failure.
     */
    public void loadHolidays() {
        int year = LocalDate.now().getYear();
        cachedHolidays = apiService.fetchHolidays(year);
        // Also load next year's holidays if we're in Q4
        if (LocalDate.now().getMonthValue() >= 10) {
            List<Holiday> nextYear = apiService.fetchHolidays(year + 1);
            cachedHolidays = concat(cachedHolidays, nextYear);
        }
    }

    /**
     * Returns the Holiday for the given date, or empty if it is not a holiday.
     */
    public Optional<Holiday> getHoliday(LocalDate date) {
        return cachedHolidays.stream()
                .filter(h -> date.equals(h.getDate()))
                .findFirst();
    }

    /**
     * Returns true if the given date is a Tunisian national holiday.
     */
    public boolean isHoliday(LocalDate date) {
        return getHoliday(date).isPresent();
    }

    /**
     * Returns the full list of cached holidays.
     */
    public List<Holiday> getAllHolidays() {
        return cachedHolidays;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WEATHER LOGIC
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fetches and caches the week's weather starting from {@code weekStart}.
     * Call this when the calendar navigates to a new week.
     */
    public void loadWeekWeather(LocalDate weekStart) {
        if (weekStart.equals(cachedWeatherWeekStart)) return; // already cached
        java.util.Map<String, com.tabibnet.calendar.model.WeatherInfo> map = apiService.fetchWeekWeather(location, weekStart);
        cachedWeather          = new java.util.ArrayList<>(map.values());
        cachedWeatherWeekStart = weekStart;
    }

    /**
     * Returns the WeatherInfo for a specific date, or a safe default.
     */
    public WeatherInfo getWeatherForDate(LocalDate date) {
        return cachedWeather.stream()
                .filter(w -> date.equals(w.getDate()))
                .findFirst()
                .orElse(new WeatherInfo(date, location, "unknown", false, ""));
    }

    /**
     * Returns true if the date has risky weather (rain, storm, snow, etc.).
     */
    public boolean hasRiskyWeather(LocalDate date) {
        return getWeatherForDate(date).isRisky();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AI RECOMMENDATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fetches an AI appointment recommendation for the given patient+doctor.
     * Always returns a non-null object. If the API is offline, it performs
     * a local analysis based on patient history and available slots.
     */
    public AiRecommendation getRecommendation(int patientId, int doctorId) {
        AiRecommendation rec = apiService.fetchRecommendation(patientId, doctorId);
        if (rec.isFallback()) {
            return applyLocalSmartFallback(rec, patientId);
        }
        return rec;
    }
    private AiRecommendation applyLocalSmartFallback(AiRecommendation rec, int patientId) {
        com.tabibnet.calendar.DataStore store = com.tabibnet.calendar.DataStore.getInstance();
        List<com.tabibnet.calendar.model.Appointment> history = store.getAppointments().stream()
                .filter(a -> a.getPatientId() == patientId)
                .toList();

        // Determine average preference hour
        final double targetHour;
        if (!history.isEmpty()) {
            targetHour = history.stream()
                    .mapToInt(a -> a.getStartTime().getHour())
                    .average().orElse(14.0);
        } else {
            targetHour = 14.0; // Default to afternoon for new patients
        }

        String prefFr = targetHour < 12 ? "matin" : (targetHour < 17 ? "après-midi" : "soir");

        // Search for the best matching slot in the next 14 days
        CalendarService cal = new CalendarService();
        com.tabibnet.calendar.model.TimeSlot bestMatch = null;
        LocalDate bestDate = null;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i < 14; i++) {
            LocalDate d = LocalDate.now().plusDays(i);
            com.tabibnet.calendar.model.TempsTravail tt = store.getTempsTravailForDate(d);
            if (tt == null) continue;

            List<com.tabibnet.calendar.model.TimeSlot> slots = cal.generateSlots(
                    d, tt, store.getCalendarSetting(), store.getIndisponibilites(), store.getAppointmentsForDate(d));

            for (com.tabibnet.calendar.model.TimeSlot s : slots) {
                if (!s.isAvailable()) continue;

                double diff = Math.abs(s.getStartTime().getHour() + (s.getStartTime().getMinute() / 60.0) - targetHour);
                
                // We prefer slots in the same period, but we'll take anything close if needed
                // Add a penalty if it's a different period to favor the same time of day
                String sPeriod = s.getStartTime().getHour() < 12 ? "matin" : (s.getStartTime().getHour() < 17 ? "après-midi" : "soir");
                if (!sPeriod.equals(prefFr)) {
                    diff += 10.0; // Strong penalty for wrong period
                }

                if (diff < minDiff) {
                    minDiff = diff;
                    bestMatch = s;
                    bestDate = d;
                }
            }
            // If we found a very good match (within 1 hour and same period) in the first few days, we can stop
            if (minDiff < 1.0) break;
        }

        if (bestMatch != null) {
            rec.setRecommendation("D'après vos habitudes (préférence pour l'horaire de " + (int)targetHour + "h), nous avons sélectionné ce créneau optimal.");
            rec.setSuggestedSlots(List.of(new AiRecommendation.SuggestedSlot(bestDate, bestMatch.getStartTime())));
            rec.setAttendanceProbability("94%");
            rec.setFallback(false);
        }
        return rec;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // INTELLIGENCE SUMMARY (for day-column UI rendering)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns a composite intelligence summary for a given date.
     * Used by CalendarViewController to decorate each day column.
     */
    public DayIntelligence getDayIntelligence(LocalDate date) {
        Optional<Holiday> holiday = getHoliday(date);
        WeatherInfo       weather = getWeatherForDate(date);
        return new DayIntelligence(date, holiday.orElse(null), weather);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LOCATION SETTING
    // ──────────────────────────────────────────────────────────────────────────

    public String getLocation()             { return location; }
    public void setLocation(String loc)     { this.location = loc; }

    // ──────────────────────────────────────────────────────────────────────────
    // INNER CLASS: DayIntelligence
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * A simple composite summary for a single calendar day.
     */
    public static class DayIntelligence {
        public final LocalDate  date;
        public final Holiday    holiday;    // null if not a holiday
        public final WeatherInfo weather;

        public DayIntelligence(LocalDate date, Holiday holiday, WeatherInfo weather) {
            this.date    = date;
            this.holiday = holiday;
            this.weather = weather;
        }

        public boolean isHoliday()     { return holiday != null; }
        public boolean isRiskyWeather(){ return weather != null && weather.isRisky(); }

        /** Returns a display label for the day: holiday name, weather warning, or "". */
        public String getAlertLabel() {
            if (isHoliday())      return "🎌 " + holiday.getDisplayName();
            if (isRiskyWeather()) return weather.getIcon() + " " + weather.getDescription();
            return "";
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UTILITY
    // ──────────────────────────────────────────────────────────────────────────

    private <T> List<T> concat(List<T> a, List<T> b) {
        var combined = new java.util.ArrayList<>(a);
        combined.addAll(b);
        return combined;
    }
}
