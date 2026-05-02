package com.pidev.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Service for AI integration using OpenRouter
 * Features: Translation, Image Analysis, Text Generation
 * Supports: Gemini, GPT-4 Vision, Claude, and more models
 */
public class GeminiAIService {
    
    // ⚠️ OpenRouter API Key


    
    // Model to use - using a more reliable model
    private static final String MODEL = "openai/gpt-4o-mini"; // Reliable and cheap
    // Alternative models you can try:
    // "meta-llama/llama-3.2-11b-vision-instruct:free" - Llama with vision (free)
    // "google/gemini-2.0-flash-exp:free" - Gemini 2.0 (free but may have issues)
    // "anthropic/claude-3-haiku" - Fast Claude (paid)
    // "openai/gpt-4o-mini" - GPT-4 mini (paid but cheap)
    // "anthropic/claude-3-haiku" - Fast Claude (paid)
    
    private final HttpClient client = HttpClient.newHttpClient();
    
    /**
     * Send a text prompt to AI
     */
    public String askAI(String prompt) throws Exception {
        JSONObject requestBody = new JSONObject()
            .put("model", MODEL)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
                )
            );
        
        return sendRequest(requestBody);
    }
    
    /**
     * Translate text between Arabic and French
     * @param text Text to translate
     * @param fromLang Source language ("ar" or "fr")
     * @param toLang Target language ("ar" or "fr")
     */
    public String translate(String text, String fromLang, String toLang) throws Exception {
        String sourceLang = fromLang.equals("ar") ? "العربية" : "الفرنسية";
        String targetLang = toLang.equals("ar") ? "العربية" : "الفرنسية";
        
        String prompt = String.format(
            "ترجم النص التالي من %s إلى %s. أعطني الترجمة فقط بدون أي شرح:\n\n%s",
            sourceLang, targetLang, text
        );
        
        return askAI(prompt);
    }
    
    /**
     * Auto-detect language and translate
     * If Arabic -> translate to French
     * If French -> translate to Arabic
     */
    public String autoTranslate(String text) throws Exception {
        // Detect if text contains Arabic characters
        boolean isArabic = text.matches(".*[\\u0600-\\u06FF].*");
        
        if (isArabic) {
            return translate(text, "ar", "fr");
        } else {
            return translate(text, "fr", "ar");
        }
    }
    
    /**
     * Analyze an image and generate title and description
     * @param imageFile Image file to analyze
     * @return JSON object with "title" and "description" fields
     */
    public JSONObject analyzeImage(File imageFile) throws Exception {
        // Read image and convert to base64
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        // Detect MIME type
        String mimeType = Files.probeContentType(imageFile.toPath());
        if (mimeType == null) {
            mimeType = "image/jpeg"; // default
        }
        
        // Create prompt for medical image analysis
        String prompt = "حلل هذه الصورة الطبية وأعطني:\n" +
                       "1. عنوان مختصر (10 كلمات كحد أقصى)\n" +
                       "2. وصف تفصيلي (50 كلمة كحد أقصى)\n\n" +
                       "أجب بتنسيق JSON فقط:\n" +
                       "{\"title\": \"العنوان هنا\", \"description\": \"الوصف هنا\"}";
        
        // Build request with image (OpenRouter format)
        JSONObject requestBody = new JSONObject()
            .put("model", MODEL)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", new JSONArray()
                        .put(new JSONObject()
                            .put("type", "text")
                            .put("text", prompt)
                        )
                        .put(new JSONObject()
                            .put("type", "image_url")
                            .put("image_url", new JSONObject()
                                .put("url", "data:" + mimeType + ";base64," + base64Image)
                            )
                        )
                    )
                )
            );
        
        String response = sendRequest(requestBody);
        
        // Parse JSON response
        try {
            // Try to extract JSON from response
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = response.substring(jsonStart, jsonEnd);
                return new JSONObject(jsonStr);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse JSON from AI response: " + e.getMessage());
        }
        
        // Fallback: create default response
        return new JSONObject()
            .put("title", "صورة طبية")
            .put("description", response.substring(0, Math.min(200, response.length())));
    }
    
    /**
     * Generate title and description from text
     * Useful when user writes description first
     */
    public JSONObject generateTitleFromDescription(String description) throws Exception {
        String prompt = "من هذا الوصف الطبي، أنشئ:\n" +
                       "1. عنوان مختصر (10 كلمات كحد أقصى)\n" +
                       "2. وصف محسّن ومنظم (50 كلمة كحد أقصى)\n\n" +
                       "الوصف: " + description + "\n\n" +
                       "أجب بتنسيق JSON فقط:\n" +
                       "{\"title\": \"العنوان هنا\", \"description\": \"الوصف المحسّن هنا\"}";
        
        String response = askAI(prompt);
        
        try {
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = response.substring(jsonStart, jsonEnd);
                return new JSONObject(jsonStr);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
        
        // Fallback
        return new JSONObject()
            .put("title", description.substring(0, Math.min(50, description.length())))
            .put("description", description);
    }
    
    /**
     * Send HTTP request to OpenRouter API
     */
    private String sendRequest(JSONObject requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OPENROUTER_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
            .header("HTTP-Referer", "http://localhost:8080") // Optional: your app URL
            .header("X-Title", "PiDev Medical Forum") // Optional: your app name
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("API Error: " + response.statusCode() + " - " + response.body());
        }
        
        // Extract text from OpenRouter response
        JSONObject jsonResponse = new JSONObject(response.body());
        return jsonResponse
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content");
    }
    
    /**
     * Check if API key is configured
     */
    public boolean isConfigured() {
        return OPENROUTER_API_KEY != null && 
               !OPENROUTER_API_KEY.equals("YOUR_API_KEY_HERE") &&
               !OPENROUTER_API_KEY.isEmpty() &&
               OPENROUTER_API_KEY.startsWith("sk-or-");
    }
}
