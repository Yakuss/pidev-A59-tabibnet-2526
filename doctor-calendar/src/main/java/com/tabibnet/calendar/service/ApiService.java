package com.tabibnet.calendar.service;

import com.tabibnet.calendar.model.AiRecommendation;
import com.tabibnet.calendar.model.AiRecommendation.SuggestedSlot;
import com.tabibnet.calendar.model.Holiday;
import com.tabibnet.calendar.model.WeatherInfo;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ApiService — central HTTP client for the Symfony Intelligence API.
 *
 * Endpoints consumed:
 *   GET {BASE_URL}/api/intelligence/weather?location=&date=YYYY-MM-DD
 *   GET {BASE_URL}/api/intelligence/holidays?year=YYYY
 *   GET {BASE_URL}/api/intelligence/recommendation/{patientId}/{doctorId}
 *
 * All methods implement a graceful fallback so the app remains usable even
 * when the Symfony server is offline.
 *
 * JSON parsing is done with lightweight manual string extraction to avoid
 * adding a third-party dependency (org.json / Gson) to the module.
 */
public class ApiService {

    private static final Logger LOG = Logger.getLogger(ApiService.class.getName());

    /** Base URL of the running Symfony application. */
    private static final String BASE_URL = "http://localhost:8000";

    /** Shared HTTP client — reused across calls for connection pooling. */
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WEATHER
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fetches weather forecast for a given location and date.
     * Returns a WeatherInfo with isRisky=false and description "N/A" on failure.
     *
     * @param location city name (e.g. "Tunis")
     * @param date     target date
     */
    public WeatherInfo fetchWeather(String location, LocalDate date) {
        // Build URL
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String encodedLoc = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = BASE_URL + "/api/intelligence/weather"
                + "?location=" + encodedLoc
                + "&date=" + dateStr;

        try {
            String body = get(url);
            if (body == null) return fallbackWeather(date, location);

            // Manual JSON extraction
            boolean isRisky   = extractBool(body, "isRisky");
            String condition  = extractString(body, "condition");
            String desc       = extractString(body, "description");

            return new WeatherInfo(date, location, condition, isRisky, desc);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchWeather failed for " + location + "/" + date, e);
            return fallbackWeather(date, location);
        }
    }

    private WeatherInfo fallbackWeather(LocalDate date, String location) {
        return new WeatherInfo(date, location, "unknown", false,
                "Données météo indisponibles.");
    }

    /**
     * Fetches weather for a full week starting from the given date.
     * Returns a map of DateString -> WeatherInfo.
     */
    public java.util.Map<String, WeatherInfo> fetchWeekWeather(String location, LocalDate startDate) {
        String url = BASE_URL + "/api/intelligence/week-weather?location=" + location 
                   + "&start_date=" + startDate.toString();
        
        try {
            String body = get(url);
            if (body == null) return java.util.Collections.emptyMap();

            // Expected format: {"YYYY-MM-DD": {"condition":"...", "isRisky":true, ...}, ...}
            java.util.Map<String, WeatherInfo> results = new java.util.HashMap<>();
            
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"(\\d{4}-\\d{2}-\\d{2})\":\\s*\\{");
            java.util.regex.Matcher matcher = pattern.matcher(body);

            while (matcher.find()) {
                String dateKey = matcher.group(1);
                int start = matcher.end();
                int end = body.indexOf("}", start);
                if (end == -1) continue;
                
                String chunk = body.substring(start, end);
                WeatherInfo info = new WeatherInfo();
                info.setDate(LocalDate.parse(dateKey));
                info.setCondition(extractString(chunk, "condition"));
                info.setDescription(extractString(chunk, "description"));
                info.setRisky(chunk.contains("\"isRisky\":true"));
                results.put(dateKey, info);
            }
            return results;

        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchWeekWeather failed", e);
            return java.util.Collections.emptyMap();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HOLIDAYS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fetches Tunisian national holidays for the given year.
     * Returns an empty list on failure.
     *
     * @param year  4-digit year
     */
    public List<Holiday> fetchHolidays(int year) {
        String url = BASE_URL + "/api/intelligence/holidays?year=" + year;

        try {
            String body = get(url);
            if (body == null) return fallbackHolidays();

            return parseHolidayArray(body);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchHolidays failed for year " + year, e);
            return fallbackHolidays();
        }
    }

    /** Returns a hardcoded minimal list of Tunisian holidays as last-resort fallback. */
    private List<Holiday> fallbackHolidays() {
        int year = LocalDate.now().getYear();
        List<Holiday> list = new ArrayList<>();
        list.add(new Holiday(LocalDate.of(year, 1,  1),  "Jour de l'An",             "New Year's Day"));
        list.add(new Holiday(LocalDate.of(year, 3, 20),  "Fête de l'Indépendance",   "Independence Day"));
        list.add(new Holiday(LocalDate.of(year, 4,  9),  "Journée des Martyrs",      "Martyr's Day"));
        list.add(new Holiday(LocalDate.of(year, 5,  1),  "Fête du Travail",          "Labour Day"));
        list.add(new Holiday(LocalDate.of(year, 7, 25),  "Fête de la République",    "Republic Day"));
        list.add(new Holiday(LocalDate.of(year, 8, 13),  "Fête de la Femme",         "Women's Day"));
        list.add(new Holiday(LocalDate.of(year, 10, 15), "Fête de l'Évacuation",     "Evacuation Day"));
        return list;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AI RECOMMENDATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fetches an AI-powered appointment recommendation.
     * Returns a fallback AiRecommendation (isFallback=true) on failure.
     *
     * @param patientId Symfony Patient entity ID
     * @param doctorId  Symfony Medecin entity ID
     */
    public AiRecommendation fetchRecommendation(int patientId, int doctorId) {
        String url = BASE_URL + "/api/intelligence/recommendation/" + patientId + "/" + doctorId;

        try {
            String body = get(url);
            if (body == null) return fallbackRecommendation(patientId, doctorId);

            AiRecommendation rec = new AiRecommendation();
            rec.setPatientId(patientId);
            rec.setDoctorId(doctorId);
            rec.setPatientName(extractString(body, "patientName"));
            rec.setDoctorName(extractString(body,  "doctorName"));
            rec.setRecommendation(extractString(body, "recommendation"));
            rec.setAttendanceProbability(extractString(body, "attendance_probability"));

            // Parse suggested_slots array: [{"date":"YYYY-MM-DD","time":"HH:MM"},...]
            rec.setSuggestedSlots(parseSuggestedSlots(body));

            return rec;

        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchRecommendation failed p=" + patientId + " d=" + doctorId, e);
            return fallbackRecommendation(patientId, doctorId);
        }
    }

    private AiRecommendation fallbackRecommendation(int patientId, int doctorId) {
        AiRecommendation rec = new AiRecommendation();
        rec.setPatientId(patientId);
        rec.setDoctorId(doctorId);
        rec.setRecommendation(
                "Recommandation IA indisponible — serveur hors ligne. "
                + "Veuillez choisir un créneau disponible manuellement.");
        rec.setAttendanceProbability(null);
        rec.setSuggestedSlots(List.of());
        rec.setFallback(true);
        return rec;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HTTP HELPER
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Performs a synchronous GET request and returns the response body as a String.
     * Returns null when the server is unreachable or returns a non-2xx status.
     */
    private String get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp.body();
            }

            LOG.warning("API returned HTTP " + resp.statusCode() + " for: " + url);
            return null;

        } catch (Exception e) {
            if (e instanceof java.net.ConnectException || (e.getCause() != null && e.getCause() instanceof java.net.ConnectException)) {
                LOG.warning("API server unreachable (localhost:8000). Using fallback data.");
            } else {
                LOG.log(Level.WARNING, "HTTP GET failed: " + url, e);
            }
            return null;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MINIMAL JSON PARSING HELPERS (no external library needed)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Extracts a string value for "key":"value" from a JSON body.
     * Returns "" if not found.
     */
    String extractString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            pattern = key + "\":\"";
            start = json.indexOf(pattern);
        }
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return unescapeUnicode(json.substring(start, end));
    }

    private String unescapeUnicode(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                try {
                    String hex = input.substring(i + 2, i + 6);
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                    continue;
                } catch (Exception e) {
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    /**
     * Extracts a boolean value for "key":true/false from a JSON body.
     */
    boolean extractBool(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return false;
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return false;
        String remainder = json.substring(colon + 1).stripLeading();
        return remainder.startsWith("true");
    }

    /**
     * Parses the holidays array from the holidays endpoint response body.
     * Expected shape inside body: "holidays":[{"date":"...","localName":"...","name":"..."}]
     */
    private List<Holiday> parseHolidayArray(String body) {
        List<Holiday> list = new ArrayList<>();
        int arrStart = body.indexOf("[");
        int arrEnd   = body.lastIndexOf("]");
        if (arrStart == -1 || arrEnd == -1) return list;

        String arr = body.substring(arrStart + 1, arrEnd);
        // Split on '},{' to get individual objects
        String[] objects = arr.split("\\},\\s*\\{");
        for (String obj : objects) {
            String dateStr = extractString("{" + obj + "}", "date");
            String local   = extractString("{" + obj + "}", "localName");
            String name    = extractString("{" + obj + "}", "name");
            if (!dateStr.isBlank()) {
                try {
                    LocalDate d = LocalDate.parse(dateStr);
                    list.add(new Holiday(d, local, name));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    /**
     * Parses the suggested_slots array from the recommendation response.
     * Expected shape: "suggested_slots":[{"date":"YYYY-MM-DD","time":"HH:MM"},...]
     */
    private List<SuggestedSlot> parseSuggestedSlots(String body) {
        List<SuggestedSlot> list = new ArrayList<>();

        // Find the suggested_slots array
        int keyIdx = body.indexOf("\"suggested_slots\"");
        if (keyIdx == -1) return list;

        int arrStart = body.indexOf("[", keyIdx);
        int arrEnd   = body.indexOf("]", arrStart);
        if (arrStart == -1 || arrEnd == -1) return list;

        String arr = body.substring(arrStart + 1, arrEnd);
        String[] objects = arr.split("\\},\\s*\\{");
        for (String obj : objects) {
            String dateStr = extractString("{" + obj + "}", "date");
            String timeStr = extractString("{" + obj + "}", "time");
            if (!dateStr.isBlank() && !timeStr.isBlank()) {
                try {
                    LocalDate d = LocalDate.parse(dateStr);
                    // Handle both "HH:MM" and "HH:MM:SS"
                    if (timeStr.length() == 5) timeStr += ":00";
                    LocalTime t = LocalTime.parse(timeStr);
                    list.add(new SuggestedSlot(d, t));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }
}
