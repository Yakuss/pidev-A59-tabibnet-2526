package com.pidev.services;

import com.pidev.models.*;
import com.pidev.models.AiRecommendation.SuggestedSlot;

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
 */
public class ApiService {

    private static final Logger LOG = Logger.getLogger(ApiService.class.getName());
    private static final String BASE_URL = "http://localhost:8000";
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public WeatherInfo fetchWeather(String location, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String encodedLoc = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = BASE_URL + "/api/intelligence/weather?location=" + encodedLoc + "&date=" + dateStr;
        try {
            String body = get(url);
            if (body == null) return fallbackWeather(date, location);
            boolean isRisky   = extractBool(body, "isRisky");
            String condition  = extractString(body, "condition");
            String desc       = extractString(body, "description");
            return new WeatherInfo(date, location, condition, isRisky, desc);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchWeather failed", e);
            return fallbackWeather(date, location);
        }
    }

    private WeatherInfo fallbackWeather(LocalDate date, String location) {
        return new WeatherInfo(date, location, "unknown", false, "Données météo indisponibles.");
    }

    public List<Holiday> fetchHolidays(int year) {
        String url = BASE_URL + "/api/intelligence/holidays?year=" + year;
        try {
            String body = get(url);
            if (body == null) return fallbackHolidays();
            return parseHolidayArray(body);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchHolidays failed", e);
            return fallbackHolidays();
        }
    }

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
            rec.setSuggestedSlots(parseSuggestedSlots(body));
            return rec;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "fetchRecommendation failed", e);
            return fallbackRecommendation(patientId, doctorId);
        }
    }

    private AiRecommendation fallbackRecommendation(int patientId, int doctorId) {
        AiRecommendation rec = new AiRecommendation();
        rec.setPatientId(patientId);
        rec.setDoctorId(doctorId);
        rec.setRecommendation("Recommandation IA indisponible — serveur hors ligne.");
        rec.setFallback(true);
        return rec;
    }

    private String get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) return resp.body();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractString(String json, String key) {
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
                } catch (Exception e) {}
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private boolean extractBool(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return false;
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return false;
        String remainder = json.substring(colon + 1).stripLeading();
        return remainder.startsWith("true");
    }

    private List<Holiday> parseHolidayArray(String body) {
        List<Holiday> list = new ArrayList<>();
        int arrStart = body.indexOf("[");
        int arrEnd   = body.lastIndexOf("]");
        if (arrStart == -1 || arrEnd == -1) return list;
        String arr = body.substring(arrStart + 1, arrEnd);
        String[] objects = arr.split("\\},\\s*\\{");
        for (String obj : objects) {
            String dateStr = extractString("{" + obj + "}", "date");
            String local   = extractString("{" + obj + "}", "localName");
            String name    = extractString("{" + obj + "}", "name");
            if (!dateStr.isBlank()) {
                try {
                    list.add(new Holiday(LocalDate.parse(dateStr), local, name));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    private List<SuggestedSlot> parseSuggestedSlots(String body) {
        List<SuggestedSlot> list = new ArrayList<>();
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
                    if (timeStr.length() == 5) timeStr += ":00";
                    list.add(new SuggestedSlot(d, LocalTime.parse(timeStr)));
                } catch (Exception ignored) {}
            }
        }
        return list;
    }
}
