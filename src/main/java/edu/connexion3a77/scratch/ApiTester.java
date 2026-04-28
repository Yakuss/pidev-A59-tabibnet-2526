package edu.connexion3a77.scratch;

import okhttp3.*;
import java.io.IOException;

public class ApiTester {
    public static void main(String[] args) throws IOException {
        String apiKey = "AIzaSyA1XTtfiQ42WAsk-poVNezyuYQNJ1WCOw0";
        OkHttpClient client = new OkHttpClient();

        // Test 1: List models
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Status: " + response.code());
            System.out.println("Response: " + response.body().string());
        }
    }
}
