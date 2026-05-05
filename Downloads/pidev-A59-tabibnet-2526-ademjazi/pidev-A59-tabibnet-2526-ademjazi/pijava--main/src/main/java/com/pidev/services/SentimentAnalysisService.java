package com.pidev.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service for AI Sentiment Analysis using Flask API
 * Analyzes feedback comments and ratings to calculate sentiment scores
 */
public class SentimentAnalysisService {

    private static final String API_BASE_URL = "http://localhost:5000";
    private static final int TIMEOUT = 10000; // 10 seconds

    /**
     * Check if sentiment API is running
     */
    public boolean isApiHealthy() {
        try {
            URL url = new URL(API_BASE_URL + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("❌ Sentiment API health check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Analyze single feedback
     * @param comment The feedback comment
     * @param rating The rating (1-5)
     * @return JSON response with sentiment analysis
     */
    public JSONObject analyzeSingleFeedback(String comment, int rating) throws Exception {
        URL url = new URL(API_BASE_URL + "/analyze");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        // Build request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("comment", comment != null ? comment : "");
        requestBody.put("rating", rating);

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
            throw new Exception("Sentiment analysis failed: " + responseCode);
        }
    }

    /**
     * Calculate doctor's average sentiment score from all feedbacks
     * @param feedbacks List of feedback objects with comment and rating
     * @return Average sentiment score (0-5)
     */
    public double calculateDoctorSentimentScore(List<FeedbackData> feedbacks) throws Exception {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }

        URL url = new URL(API_BASE_URL + "/doctor-sentiment-score");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        // Build request body
        JSONObject requestBody = new JSONObject();
        JSONArray feedbacksArray = new JSONArray();
        
        for (FeedbackData fb : feedbacks) {
            JSONObject fbJson = new JSONObject();
            fbJson.put("comment", fb.getComment() != null ? fb.getComment() : "");
            fbJson.put("rating", fb.getRating());
            feedbacksArray.put(fbJson);
        }
        
        requestBody.put("feedbacks", feedbacksArray);

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
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (jsonResponse.getBoolean("success")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.has("average_sentiment_score") && !data.isNull("average_sentiment_score")) {
                    return data.getDouble("average_sentiment_score");
                }
            }
            return 0.0;
        } else {
            throw new Exception("Doctor sentiment score calculation failed: " + responseCode);
        }
    }

    /**
     * Simple data class to hold feedback data
     */
    public static class FeedbackData {
        private String comment;
        private int rating;

        public FeedbackData(String comment, int rating) {
            this.comment = comment;
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public int getRating() {
            return rating;
        }
    }

    /**
     * Extract sentiment score from analysis result
     */
    public double extractFinalScore(JSONObject analysisResult) {
        if (analysisResult.has("data")) {
            JSONObject data = analysisResult.getJSONObject("data");
            if (data.has("final_score")) {
                return data.getDouble("final_score");
            }
        }
        return 0.0;
    }

    /**
     * Extract sentiment label from analysis result
     */
    public String extractSentimentLabel(JSONObject analysisResult) {
        if (analysisResult.has("data")) {
            JSONObject data = analysisResult.getJSONObject("data");
            if (data.has("sentiment_label")) {
                return data.getString("sentiment_label");
            }
        }
        return "neutral";
    }
}
