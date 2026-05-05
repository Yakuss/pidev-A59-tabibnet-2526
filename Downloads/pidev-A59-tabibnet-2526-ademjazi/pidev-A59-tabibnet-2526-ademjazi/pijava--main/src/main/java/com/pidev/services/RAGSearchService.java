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
 * Service for RAG (Retrieval-Augmented Generation) AI-powered doctor search
 * Uses natural language queries to find doctors
 */
public class RAGSearchService {

    private static final String API_BASE_URL = "http://localhost:5000";
    private static final int TIMEOUT = 15000; // 15 seconds for AI processing

    /**
     * Check RAG service health
     */
    public JSONObject getHealth() throws Exception {
        URL url = new URL(API_BASE_URL + "/health");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return new JSONObject(response.toString());
        } else {
            throw new Exception("Health check failed: " + responseCode);
        }
    }

    /**
     * Get database statistics
     */
    public JSONObject getStats() throws Exception {
        URL url = new URL(API_BASE_URL + "/stats");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return new JSONObject(response.toString());
        } else {
            throw new Exception("Stats request failed: " + responseCode);
        }
    }

    /**
     * Query doctors using natural language
     * @param question Natural language question (French, Arabic, or English)
     * @param topK Number of results to return (default: 8)
     * @return JSON response with doctors and AI-generated response
     */
    public JSONObject query(String question, int topK) throws Exception {
        URL url = new URL(API_BASE_URL + "/query");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        // Build request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("question", question);
        requestBody.put("top_k", topK);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return new JSONObject(response.toString());
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("Query failed: " + responseCode + " - " + errorResponse.toString());
        }
    }

    /**
     * Parse doctors from RAG query response
     */
    public List<DoctorAPI> parseDoctorsFromRAGResponse(JSONObject response) {
        List<DoctorAPI> doctors = new ArrayList<>();
        
        if (response.has("doctors")) {
            JSONArray doctorsArray = response.getJSONArray("doctors");
            
            for (int i = 0; i < doctorsArray.length(); i++) {
                JSONObject doctorJson = doctorsArray.getJSONObject(i);
                DoctorAPI doctor = new DoctorAPI();
                
                // Parse fields from RAG response - handle null values
                if (doctorJson.has("fullName") && !doctorJson.isNull("fullName")) {
                    doctor.setName(doctorJson.getString("fullName"));
                }
                
                if (doctorJson.has("specialite") && !doctorJson.isNull("specialite")) {
                    doctor.setSpecialty(doctorJson.getString("specialite"));
                }
                
                if (doctorJson.has("governorate") && !doctorJson.isNull("governorate")) {
                    doctor.setGovernorate(doctorJson.getString("governorate"));
                }
                
                if (doctorJson.has("adresse") && !doctorJson.isNull("adresse")) {
                    doctor.setAddress(doctorJson.getString("adresse"));
                }
                
                if (doctorJson.has("telephone") && !doctorJson.isNull("telephone")) {
                    doctor.setPhone(doctorJson.getString("telephone"));
                } else {
                    doctor.setPhone("Non disponible");
                }
                
                if (doctorJson.has("email") && !doctorJson.isNull("email")) {
                    doctor.setEmail(doctorJson.getString("email"));
                }
                
                doctor.setMode("Médecin de Libre Pratique");
                
                doctors.add(doctor);
            }
        }
        
        return doctors;
    }

    /**
     * Get AI response sentence from query result
     */
    public String getResponseSentence(JSONObject response) {
        if (response.has("responseSentence")) {
            return response.getString("responseSentence");
        }
        if (response.has("message")) {
            return response.getString("message");
        }
        return "";
    }

    /**
     * Get query status
     */
    public String getStatus(JSONObject response) {
        if (response.has("status")) {
            return response.getString("status");
        }
        return "unknown";
    }

    /**
     * Check if query was successful
     */
    public boolean isSuccess(JSONObject response) {
        String status = getStatus(response);
        return "success".equals(status);
    }

    /**
     * Check if query was a greeting
     */
    public boolean isGreeting(JSONObject response) {
        String status = getStatus(response);
        return "greeting".equals(status);
    }

    /**
     * Check if query had insufficient context
     */
    public boolean isInsufficientContext(JSONObject response) {
        String status = getStatus(response);
        return "insufficient_context".equals(status);
    }

    /**
     * Get similarity scores
     */
    public List<Double> getScores(JSONObject response) {
        List<Double> scores = new ArrayList<>();
        if (response.has("scores")) {
            JSONArray scoresArray = response.getJSONArray("scores");
            for (int i = 0; i < scoresArray.length(); i++) {
                scores.add(scoresArray.getDouble(i));
            }
        }
        return scores;
    }

    /**
     * Get notes/warnings
     */
    public List<String> getNotes(JSONObject response) {
        List<String> notes = new ArrayList<>();
        if (response.has("notes")) {
            JSONArray notesArray = response.getJSONArray("notes");
            for (int i = 0; i < notesArray.length(); i++) {
                notes.add(notesArray.getString(i));
            }
        }
        return notes;
    }
}
