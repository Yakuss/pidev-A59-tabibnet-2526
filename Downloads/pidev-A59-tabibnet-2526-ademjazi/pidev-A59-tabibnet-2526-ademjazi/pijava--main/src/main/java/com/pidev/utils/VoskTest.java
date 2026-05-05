package com.pidev.utils;

import com.pidev.services.VoskSpeechService;

/**
 * Simple test class to verify Vosk installation
 * Run this to test if Vosk is working correctly
 */
public class VoskTest {

    public static void main(String[] args) {
        System.out.println("🎤 Vosk Speech Recognition Test");
        System.out.println("================================\n");
        
        // Check if microphone is available
        System.out.println("1. Checking microphone availability...");
        if (VoskSpeechService.isMicrophoneAvailable()) {
            System.out.println("   ✅ Microphone is available!\n");
        } else {
            System.out.println("   ❌ Microphone not found!\n");
            System.out.println("   Please connect a microphone and try again.");
            return;
        }
        
        // List available microphones
        System.out.println("2. Available microphones:");
        String[] microphones = VoskSpeechService.getAvailableMicrophones();
        if (microphones.length > 0) {
            for (int i = 0; i < microphones.length; i++) {
                System.out.println("   " + (i + 1) + ". " + microphones[i]);
            }
            System.out.println();
        } else {
            System.out.println("   No microphones found.\n");
        }
        
        // Test Vosk model loading
        System.out.println("3. Testing Vosk model...");
        String modelPath = "models/vosk-model-small-fr-0.22";
        
        try {
            VoskSpeechService vosk = new VoskSpeechService(modelPath);
            System.out.println("   ✅ Vosk model loaded successfully!\n");
            
            // Setup callbacks
            vosk.setOnPartialResult(text -> {
                System.out.println("   [Partial] " + text);
            });
            
            vosk.setOnFinalResult(text -> {
                System.out.println("   [Final] ✅ " + text);
            });
            
            vosk.setOnError(error -> {
                System.err.println("   [Error] ❌ " + error);
            });
            
            // Start listening
            System.out.println("4. Starting speech recognition...");
            System.out.println("   🎤 Speak now! (10 seconds)");
            System.out.println("   Try saying: \"Je cherche un cardiologue à Tunis\"\n");
            
            vosk.startListening();
            
            // Listen for 10 seconds
            Thread.sleep(10000);
            
            // Stop listening
            System.out.println("\n5. Stopping...");
            vosk.stopListening();
            vosk.dispose();
            
            System.out.println("\n✅ Test completed successfully!");
            System.out.println("\nIf you saw your speech transcribed above, Vosk is working!");
            System.out.println("You can now use the voice search in the Annuaire.");
            
        } catch (java.io.IOException e) {
            System.err.println("   ❌ Model not found!");
            System.err.println("\n   Please download the Vosk model:");
            System.err.println("   1. Go to: https://alphacephei.com/vosk/models");
            System.err.println("   2. Download: vosk-model-small-fr-0.22.zip (39 MB)");
            System.err.println("   3. Extract to: pijava--main/models/");
            System.err.println("\n   Expected path: " + modelPath);
            
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
