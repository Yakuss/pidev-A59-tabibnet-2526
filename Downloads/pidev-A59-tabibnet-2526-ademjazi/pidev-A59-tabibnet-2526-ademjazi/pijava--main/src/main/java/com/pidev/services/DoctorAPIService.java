package com.pidev.services;

import com.pidev.models.DoctorAPI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to interact with Flask API for doctor directory
 * API running on localhost:5000
 */
public class DoctorAPIService {

    private static final String API_BASE_URL = "http://localhost:5000";
    private static final int TIMEOUT = 10000; // 10 seconds

    /**
     * Check API status
     */
    public JSONObject getAPIStatus() throws Exception {
        URL url = new URL(API_BASE_URL + "/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new JSONObject(response.toString());
        } else {
            throw new Exception("API returned status code: " + responseCode);
        }
    }

    /**
     * Search doctors with pagination
     * @param name Doctor name (optional, fuzzy search)
     * @param specialty Specialty filter (optional)
     * @param governorate Governorate filter (optional)
     * @param page Page number (default: 1)
     * @param size Page size (default: 20, max: 100)
     * @return JSON response with doctors list and pagination info
     */
    public JSONObject searchDoctors(String name, String specialty, String governorate, int page, int size) throws Exception {
        URL url = new URL(API_BASE_URL + "/search/doctorsList?page=" + page + "&size=" + size);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        // Build request body
        JSONObject requestBody = new JSONObject();
        if (name != null && !name.trim().isEmpty()) {
            requestBody.put("name", name.trim());
        }
        if (specialty != null && !specialty.trim().isEmpty()) {
            requestBody.put("specialty", specialty.trim());
        }
        if (governorate != null && !governorate.trim().isEmpty()) {
            requestBody.put("governorate", governorate.trim());
        }

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new JSONObject(response.toString());
        } else {
            throw new Exception("API returned status code: " + responseCode);
        }
    }

    /**
     * Check if a doctor exists matching criteria
     * @param name Doctor name (optional)
     * @param specialty Specialty filter (optional)
     * @param governorate Governorate filter (optional)
     * @return true if doctor exists, false otherwise
     */
    public boolean checkDoctorExists(String name, String specialty, String governorate) throws Exception {
        URL url = new URL(API_BASE_URL + "/search/doctors");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        // Build request body
        JSONObject requestBody = new JSONObject();
        if (name != null && !name.trim().isEmpty()) {
            requestBody.put("name", name.trim());
        }
        if (specialty != null && !specialty.trim().isEmpty()) {
            requestBody.put("specialty", specialty.trim());
        }
        if (governorate != null && !governorate.trim().isEmpty()) {
            requestBody.put("governorate", governorate.trim());
        }

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getBoolean("result");
        } else {
            throw new Exception("API returned status code: " + responseCode);
        }
    }

    /**
     * Parse doctors from JSON response
     */
    public List<DoctorAPI> parseDoctorsFromResponse(JSONObject response) {
        List<DoctorAPI> doctors = new ArrayList<>();
        
        if (response.has("doctors")) {
            JSONArray doctorsArray = response.getJSONArray("doctors");
            
            for (int i = 0; i < doctorsArray.length(); i++) {
                JSONObject doctorJson = doctorsArray.getJSONObject(i);
                DoctorAPI doctor = new DoctorAPI();
                
                // Parse name - YOUR API USES "Nom & Prénom"
                if (doctorJson.has("Nom & Prénom")) {
                    doctor.setName(doctorJson.getString("Nom & Prénom"));
                } else if (doctorJson.has("name")) {
                    doctor.setName(doctorJson.getString("name"));
                } else if (doctorJson.has("nom")) {
                    doctor.setName(doctorJson.getString("nom"));
                }
                
                // Parse specialty - YOUR API USES "Spécialité"
                if (doctorJson.has("Spécialité")) {
                    doctor.setSpecialty(doctorJson.getString("Spécialité"));
                } else if (doctorJson.has("specialty")) {
                    doctor.setSpecialty(doctorJson.getString("specialty"));
                } else if (doctorJson.has("specialite")) {
                    doctor.setSpecialty(doctorJson.getString("specialite"));
                }
                
                // Parse governorate - YOUR API USES "Governorate"
                if (doctorJson.has("Governorate")) {
                    doctor.setGovernorate(doctorJson.getString("Governorate"));
                } else if (doctorJson.has("governorate")) {
                    doctor.setGovernorate(doctorJson.getString("governorate"));
                } else if (doctorJson.has("gouvernorat")) {
                    doctor.setGovernorate(doctorJson.getString("gouvernorat"));
                }
                
                // Parse address - YOUR API USES "Adresse Professionnelle"
                if (doctorJson.has("Adresse Professionnelle")) {
                    doctor.setAddress(doctorJson.getString("Adresse Professionnelle"));
                } else if (doctorJson.has("address")) {
                    doctor.setAddress(doctorJson.getString("address"));
                } else if (doctorJson.has("adresse")) {
                    doctor.setAddress(doctorJson.getString("adresse"));
                }
                
                // Parse phone - YOUR API USES "Téléphone"
                if (doctorJson.has("Téléphone")) {
                    doctor.setPhone(doctorJson.getString("Téléphone"));
                } else if (doctorJson.has("phone")) {
                    doctor.setPhone(doctorJson.getString("phone"));
                } else if (doctorJson.has("telephone")) {
                    doctor.setPhone(doctorJson.getString("telephone"));
                }
                
                // Parse mode - YOUR API USES "Mode Exercice"
                if (doctorJson.has("Mode Exercice")) {
                    doctor.setMode(doctorJson.getString("Mode Exercice"));
                } else if (doctorJson.has("mode")) {
                    doctor.setMode(doctorJson.getString("mode"));
                }
                
                // Parse email (if available)
                if (doctorJson.has("email")) {
                    doctor.setEmail(doctorJson.getString("email"));
                } else if (doctorJson.has("Email")) {
                    doctor.setEmail(doctorJson.getString("Email"));
                } else if (doctorJson.has("mail")) {
                    doctor.setEmail(doctorJson.getString("mail"));
                }
                
                // Parse qualification (if available)
                if (doctorJson.has("qualification")) {
                    doctor.setQualification(doctorJson.getString("qualification"));
                } else if (doctorJson.has("diplome")) {
                    doctor.setQualification(doctorJson.getString("diplome"));
                }
                
                // Parse experience (if available)
                if (doctorJson.has("experience")) {
                    doctor.setExperience(doctorJson.getString("experience"));
                } else if (doctorJson.has("Experience")) {
                    doctor.setExperience(doctorJson.getString("Experience"));
                }
                
                // Parse languages (if available)
                if (doctorJson.has("languages")) {
                    doctor.setLanguages(doctorJson.getString("languages"));
                } else if (doctorJson.has("langues")) {
                    doctor.setLanguages(doctorJson.getString("langues"));
                }
                
                doctors.add(doctor);
            }
        }
        
        return doctors;
    }

    /**
     * Get pagination info from response
     */
    public PaginationInfo getPaginationInfo(JSONObject response) {
        PaginationInfo info = new PaginationInfo();
        
        if (response.has("currentPage")) info.currentPage = response.getInt("currentPage");
        if (response.has("pageSize")) info.pageSize = response.getInt("pageSize");
        if (response.has("totalItems")) info.totalItems = response.getInt("totalItems");
        if (response.has("totalPages")) info.totalPages = response.getInt("totalPages");
        
        return info;
    }

    /**
     * Inner class to hold pagination information
     */
    public static class PaginationInfo {
        public int currentPage = 1;
        public int pageSize = 20;
        public int totalItems = 0;
        public int totalPages = 0;

        public boolean hasNextPage() {
            return currentPage < totalPages;
        }

        public boolean hasPreviousPage() {
            return currentPage > 1;
        }

        @Override
        public String toString() {
            return String.format("Page %d/%d (%d items total)", currentPage, totalPages, totalItems);
        }
    }
}
