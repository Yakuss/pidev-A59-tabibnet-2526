package com.pidev.services;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Vosk Speech-to-Text Service for JavaFX Desktop Application
 * Provides offline speech recognition in multiple languages
 * 
 * Download models from: https://alphacephei.com/vosk/models
 * Recommended models:
 * - French: vosk-model-small-fr-0.22 (39 MB)
 * - Arabic: vosk-model-ar-0.22-linto (66 MB)
 * - English: vosk-model-small-en-us-0.15 (40 MB)
 */
public class VoskSpeechService {

    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private boolean isListening = false;
    private Thread listeningThread;
    
    // Callbacks
    private Consumer<String> onPartialResult;
    private Consumer<String> onFinalResult;
    private Consumer<String> onError;
    
    // Audio format
    private static final float SAMPLE_RATE = 16000.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    /**
     * Initialize Vosk with a model
     * @param modelPath Path to Vosk model directory (e.g., "models/vosk-model-small-fr-0.22")
     */
    public VoskSpeechService(String modelPath) throws IOException {
        // Set Vosk log level to suppress logs
        LibVosk.setLogLevel(org.vosk.LogLevel.WARNINGS); // Only show warnings and errors
        
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new IOException("Model not found at: " + modelPath + 
                "\n\nDownload models from: https://alphacephei.com/vosk/models" +
                "\nRecommended:\n" +
                "- French: vosk-model-small-fr-0.22 (39 MB)\n" +
                "- Arabic: vosk-model-ar-0.22-linto (66 MB)\n" +
                "- English: vosk-model-small-en-us-0.15 (40 MB)");
        }
        
        this.model = new Model(modelPath);
        this.recognizer = new Recognizer(model, SAMPLE_RATE);
    }

    /**
     * Set callback for partial results (real-time transcription)
     */
    public void setOnPartialResult(Consumer<String> callback) {
        this.onPartialResult = callback;
    }

    /**
     * Set callback for final results (complete sentence)
     */
    public void setOnFinalResult(Consumer<String> callback) {
        this.onFinalResult = callback;
    }

    /**
     * Set callback for errors
     */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }

    /**
     * Start listening to microphone
     */
    public void startListening() throws LineUnavailableException {
        if (isListening) {
            return;
        }

        // Setup audio format
        AudioFormat format = new AudioFormat(
            SAMPLE_RATE, 
            SAMPLE_SIZE_IN_BITS, 
            CHANNELS, 
            SIGNED, 
            BIG_ENDIAN
        );

        // Get microphone
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported");
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        isListening = true;

        // Start listening thread
        listeningThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            
            try {
                while (isListening && !Thread.currentThread().isInterrupted()) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    
                    if (bytesRead > 0 && isListening) {
                        try {
                            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                                // Final result (end of sentence)
                                String result = recognizer.getResult();
                                String text = extractText(result);
                                
                                if (!text.isEmpty() && onFinalResult != null) {
                                    onFinalResult.accept(text);
                                }
                            } else {
                                // Partial result (real-time)
                                String partialResult = recognizer.getPartialResult();
                                String text = extractPartialText(partialResult);
                                
                                if (!text.isEmpty() && onPartialResult != null) {
                                    onPartialResult.accept(text);
                                }
                            }
                        } catch (Exception e) {
                            // Stop if recognizer has issues
                            if (isListening && onError != null) {
                                onError.accept("Recognition error: " + e.getMessage());
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                if (isListening && onError != null) {
                    onError.accept("Error during recognition: " + e.getMessage());
                }
            }
        });
        
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    /**
     * Stop listening
     */
    public void stopListening() {
        if (!isListening) {
            return;
        }

        isListening = false;

        // Stop thread first
        if (listeningThread != null && listeningThread.isAlive()) {
            listeningThread.interrupt();
            try {
                listeningThread.join(1000); // Wait max 1 second
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        // Stop microphone
        if (microphone != null && microphone.isOpen()) {
            try {
                microphone.stop();
                microphone.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }

        // Get final result only if recognizer is still valid
        if (recognizer != null) {
            try {
                String finalResult = recognizer.getFinalResult();
                String text = extractText(finalResult);
                
                if (!text.isEmpty() && onFinalResult != null) {
                    onFinalResult.accept(text);
                }
            } catch (Exception e) {
                // Ignore if recognizer is already closed
            }
        }
    }

    /**
     * Check if currently listening
     */
    public boolean isListening() {
        return isListening;
    }

    /**
     * Extract text from Vosk JSON result
     */
    private String extractText(String jsonResult) {
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("text")) {
                return json.getString("text").trim();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return "";
    }

    /**
     * Extract text from Vosk partial JSON result
     */
    private String extractPartialText(String jsonResult) {
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("partial")) {
                return json.getString("partial").trim();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return "";
    }

    /**
     * Transcribe audio file
     * @param audioFile Audio file (WAV format, 16kHz, mono)
     * @return Transcribed text
     */
    public String transcribeFile(File audioFile) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        
        // Convert to required format if needed
        AudioFormat targetFormat = new AudioFormat(
            SAMPLE_RATE, 
            SAMPLE_SIZE_IN_BITS, 
            CHANNELS, 
            SIGNED, 
            BIG_ENDIAN
        );
        
        AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
        
        byte[] buffer = new byte[4096];
        int bytesRead;
        StringBuilder result = new StringBuilder();
        
        while ((bytesRead = convertedStream.read(buffer)) != -1) {
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String text = extractText(recognizer.getResult());
                if (!text.isEmpty()) {
                    result.append(text).append(" ");
                }
            }
        }
        
        // Get final result
        String finalText = extractText(recognizer.getFinalResult());
        if (!finalText.isEmpty()) {
            result.append(finalText);
        }
        
        convertedStream.close();
        audioStream.close();
        
        return result.toString().trim();
    }

    /**
     * Clean up resources
     */
    public void dispose() {
        stopListening();
        
        // Close recognizer
        if (recognizer != null) {
            try {
                recognizer.close();
                recognizer = null;
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        
        // Close model
        if (model != null) {
            try {
                model.close();
                model = null;
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Get available microphones
     */
    public static String[] getAvailableMicrophones() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        java.util.List<String> microphones = new java.util.ArrayList<>();
        
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLines = mixer.getTargetLineInfo();
            
            if (targetLines.length > 0) {
                microphones.add(mixerInfo.getName());
            }
        }
        
        return microphones.toArray(new String[0]);
    }

    /**
     * Check if microphone is available
     */
    public static boolean isMicrophoneAvailable() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        return AudioSystem.isLineSupported(info);
    }
}
