package com.pidev.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service pour communiquer avec l'API Gemini de Google
 * Permet d'obtenir des réponses d'IA basées sur un contexte médical
 */
public class AiApiService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";
    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;

    public AiApiService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Envoie un prompt et un contexte à l'IA et retourne sa réponse
     */
    public String getAiResponse(String prompt, String context) throws IOException {
        String systemInstruction = "Assistant Médical TabibNet.\n" +
                "RÈGLES DE RÉPONSE :\n" +
                "1. Si l'utilisateur dit 'Bonjour', réponds EXACTEMENT : 'Bonjour ! Comment puis-je t’aider aujourd’hui ?'.\n" +
                "2. Pour toute autre question, réponds de manière DIRECTE, PRÉCISE et PROFESSIONNELLE.\n" +
                "3. Pas de phrases de remplissage. Va droit au but.\n" +
                "4. Utilise le contexte fourni (PDF ou Dossier) en priorité, sinon tes connaissances.\n" +
                "5. Format : Markdown.";

        String fullPrompt = systemInstruction + "\n\n--- CONTEXTE MÉDICAL ---\n" + context + 
                            "\n\n--- QUESTION ---\n" + prompt;

        JsonObject jsonBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", fullPrompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        jsonBody.add("contents", contents);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL + apiKey)
                .post(body)
                .build();

        int maxRetries = 2;
        int retryCount = 0;
        
        while (true) {
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 503 && retryCount < maxRetries) {
                    retryCount++;
                    try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException ignored) {}
                    continue;
                }
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    throw new IOException("Erreur API (" + response.code() + ") : " + errorBody);
                }

                String responseData = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);
                
                try {
                    return jsonResponse.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                } catch (Exception e) {
                    throw new IOException("Format de réponse API invalide : " + responseData);
                }
            }
        }
    }
}
