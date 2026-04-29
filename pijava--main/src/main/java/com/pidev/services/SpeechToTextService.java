package com.pidev.services;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service for Speech-to-Text functionality
 * Supports multiple providers: Web Speech API (browser-based) and OpenAI Whisper API
 */
public class SpeechToTextService {

    // OpenAI Whisper API configuration (optional, for server-side processing)
    private static final String OPENAI_API_KEY = "YOUR_OPENAI_API_KEY"; // Replace with your key
    private static final String WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions";
    
    // GitHub Models API (alternative, free)
    private static final String GITHUB_TOKEN = "YOUR_GITHUB_TOKEN"; // Replace with your token
    private static final String GITHUB_WHISPER_URL = "https://models.inference.ai.azure.com/audio/transcriptions";
    
    private static final int TIMEOUT = 30000; // 30 seconds for audio processing

    /**
     * Check if OpenAI API is configured
     */
    public boolean isOpenAIConfigured() {
        return OPENAI_API_KEY != null && 
               !OPENAI_API_KEY.isEmpty() && 
               !OPENAI_API_KEY.equals("YOUR_OPENAI_API_KEY");
    }

    /**
     * Check if GitHub Models API is configured
     */
    public boolean isGitHubConfigured() {
        return GITHUB_TOKEN != null && 
               !GITHUB_TOKEN.isEmpty() && 
               !GITHUB_TOKEN.equals("YOUR_GITHUB_TOKEN");
    }

    /**
     * Transcribe audio file using OpenAI Whisper API
     * @param audioFile Audio file (mp3, mp4, mpeg, mpga, m4a, wav, webm)
     * @param language Language code (optional, e.g., "fr", "ar", "en")
     * @return Transcribed text
     */
    public String transcribeWithOpenAI(File audioFile, String language) throws Exception {
        if (!isOpenAIConfigured()) {
            throw new Exception("OpenAI API key not configured");
        }

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        URL url = new URL(WHISPER_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
            
            // Add file
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                  .append(audioFile.getName()).append("\"\r\n");
            writer.append("Content-Type: audio/mpeg\r\n\r\n");
            writer.flush();
            
            try (FileInputStream fis = new FileInputStream(audioFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            os.flush();
            writer.append("\r\n");
            
            // Add model
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
            writer.append("whisper-1\r\n");
            
            // Add language if specified
            if (language != null && !language.isEmpty()) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
                writer.append(language).append("\r\n");
            }
            
            // End
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

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
            return jsonResponse.getString("text");
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("Whisper API error: " + responseCode + " - " + errorResponse.toString());
        }
    }

    /**
     * Transcribe audio file using GitHub Models Whisper API
     * @param audioFile Audio file
     * @param language Language code (optional)
     * @return Transcribed text
     */
    public String transcribeWithGitHub(File audioFile, String language) throws Exception {
        if (!isGitHubConfigured()) {
            throw new Exception("GitHub token not configured");
        }

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        URL url = new URL(GITHUB_WHISPER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + GITHUB_TOKEN);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
            
            // Add file
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                  .append(audioFile.getName()).append("\"\r\n");
            writer.append("Content-Type: audio/mpeg\r\n\r\n");
            writer.flush();
            
            try (FileInputStream fis = new FileInputStream(audioFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            os.flush();
            writer.append("\r\n");
            
            // Add model
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
            writer.append("whisper-1\r\n");
            
            // Add language if specified
            if (language != null && !language.isEmpty()) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
                writer.append(language).append("\r\n");
            }
            
            // End
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

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
            return jsonResponse.getString("text");
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("GitHub Whisper API error: " + responseCode + " - " + errorResponse.toString());
        }
    }

    /**
     * Auto-select best available transcription service
     * @param audioFile Audio file to transcribe
     * @param language Language code (optional)
     * @return Transcribed text
     */
    public String transcribe(File audioFile, String language) throws Exception {
        // Try GitHub first (free)
        if (isGitHubConfigured()) {
            try {
                return transcribeWithGitHub(audioFile, language);
            } catch (Exception e) {
                System.err.println("GitHub transcription failed, trying OpenAI: " + e.getMessage());
            }
        }
        
        // Fallback to OpenAI
        if (isOpenAIConfigured()) {
            return transcribeWithOpenAI(audioFile, language);
        }
        
        throw new Exception("No transcription service configured. Please add OpenAI API key or GitHub token.");
    }

    /**
     * Transcribe audio from base64 encoded data
     * @param base64Audio Base64 encoded audio data
     * @param format Audio format (e.g., "webm", "wav", "mp3")
     * @param language Language code (optional)
     * @return Transcribed text
     */
    public String transcribeFromBase64(String base64Audio, String format, String language) throws Exception {
        // Decode base64 to file
        byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
        
        // Create temp file
        File tempFile = File.createTempFile("audio_", "." + format);
        tempFile.deleteOnExit();
        
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(audioBytes);
        }
        
        // Transcribe
        String result = transcribe(tempFile, language);
        
        // Clean up
        tempFile.delete();
        
        return result;
    }
}
