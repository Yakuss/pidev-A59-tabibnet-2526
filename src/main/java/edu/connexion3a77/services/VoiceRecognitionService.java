package edu.connexion3a77.services;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VoiceRecognitionService {

    private static final String MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip";
    private static final String MODEL_DIR_NAME = "vosk-model-small-fr-0.22";
    private static final String MODELS_PATH = "models";

    private Model model;
    private boolean isListening = false;
    private TargetDataLine microphone;

    public interface TranscriptionListener {
        void onTranscription(String text);
        void onError(String error);
        void onStatus(String status);
    }

    public VoiceRecognitionService() {
        // Le modèle sera chargé lors du premier appel à startListening
    }

    private void initModel(TranscriptionListener listener) throws Exception {
        if (model != null) return;

        Path modelPath = Paths.get(MODELS_PATH, MODEL_DIR_NAME);
        if (!Files.exists(modelPath)) {
            listener.onStatus("Téléchargement du modèle vocal (~40 Mo)... Veuillez patienter.");
            downloadAndExtractModel();
        }

        listener.onStatus("Chargement du modèle...");
        // Log level is default
        model = new Model(modelPath.toString());
        listener.onStatus("Prêt");
    }

    private void downloadAndExtractModel() throws Exception {
        Files.createDirectories(Paths.get(MODELS_PATH));
        Path zipPath = Paths.get(MODELS_PATH, "model.zip");

        // Téléchargement
        try (InputStream in = new URL(MODEL_URL).openStream()) {
            Files.copy(in, zipPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // Extraction
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(MODELS_PATH, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }

        // Nettoyage du zip
        Files.deleteIfExists(zipPath);
    }

    public void startListening(TranscriptionListener listener) {
        if (isListening) return;

        new Thread(() -> {
            try {
                initModel(listener);

                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    listener.onError("Microphone non supporté.");
                    return;
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();
                isListening = true;
                listener.onStatus("Écoute en cours...");

                try (Recognizer recognizer = new Recognizer(model, 16000)) {
                    int bytesRead;
                    byte[] b = new byte[4096];

                    while (isListening) {
                        bytesRead = microphone.read(b, 0, b.length);
                        if (bytesRead > 0) {
                            if (recognizer.acceptWaveForm(b, bytesRead)) {
                                String result = recognizer.getResult();
                                String text = extractTextFromResult(result);
                                if (!text.isEmpty()) {
                                    listener.onTranscription(text);
                                }
                            } else {
                                // recognizer.getPartialResult() can be used for real-time partial text
                                // String partial = extractTextFromResult(recognizer.getPartialResult());
                                // We wait for full words for better accuracy.
                            }
                        }
                    }
                    
                    // Fin de l'écoute, on récupère le résultat final
                    String finalResult = recognizer.getFinalResult();
                    String text = extractTextFromResult(finalResult);
                    if (!text.isEmpty()) {
                        listener.onTranscription(text);
                    }
                }
            } catch (Exception e) {
                listener.onError("Erreur vocale : " + e.getMessage());
                isListening = false;
            } finally {
                stopMicrophone();
                listener.onStatus("Arrêté");
            }
        }).start();
    }

    public void stopListening() {
        isListening = false;
    }

    private void stopMicrophone() {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }
    }

    private String extractTextFromResult(String jsonResult) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();
            String text = "";
            if (jsonObject.has("text")) {
                text = jsonObject.get("text").getAsString().trim();
            } else if (jsonObject.has("partial")) {
                text = jsonObject.get("partial").getAsString().trim();
            }
            
            if (!text.isEmpty()) {
                return postProcessText(text);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return "";
    }

    /**
     * Rend la reconnaissance "intelligente" en corrigeant les erreurs d'encodage
     * et les mots mal compris par l'IA (comme 'pédé' au lieu de 'pdf').
     */
    private String postProcessText(String text) {
        // 1. Correction d'encodage (UTF-8 mal lu sur Windows)
        try {
            if (text.contains("Ã") || text.contains("")) {
                text = new String(text.getBytes("windows-1252"), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        // 2. Corrections intelligentes des mots (Expressions régulières)
        // Remplacer "pédé", "pé dé", "pd", "p d f" par "pdf"
        text = text.replaceAll("(?i)\\b(pédé|pé dé|p d f|pd f|pédè|pé dé ef)\\b", "pdf");
        
        // Autres corrections médicales courantes si besoin
        text = text.replaceAll("(?i)\\b(tabib net|tabi net|tabibnet)\\b", "TabibNet");
        text = text.replaceAll("(?i)\\b(ordonnance|ordonnances)\\b", "ordonnance");

        // Majuscule au début
        if (text.length() > 0) {
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        return text;
    }
}
