package com.pidev.utils;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Text-To-Speech service using Windows SAPI via PowerShell.
 * No external dependencies required — works on any Windows machine.
 *
 * Usage:
 *   TTSService.getInstance().speak("Bonjour le monde");
 *   TTSService.getInstance().stop();
 */
public class TTSService {

    private static TTSService instance;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tts-thread");
        t.setDaemon(true);
        return t;
    });

    private Future<?> currentTask;
    private Process currentProcess;
    private boolean speaking = false;

    // Listener so UI can react to start/stop
    private Runnable onStart;
    private Runnable onStop;

    private TTSService() {}

    public static TTSService getInstance() {
        if (instance == null) instance = new TTSService();
        return instance;
    }

    public boolean isSpeaking() { return speaking; }

    public void setOnStart(Runnable r) { this.onStart = r; }
    public void setOnStop(Runnable r)  { this.onStop  = r; }

    /**
     * Speak the given text. Stops any ongoing speech first.
     * Runs on a background thread — safe to call from JavaFX thread.
     */
    public void speak(String text) {
        stop(); // cancel any current speech

        currentTask = executor.submit(() -> {
            try {
                speaking = true;
                if (onStart != null) Platform.runLater(onStart);

                // Sanitize: escape single quotes for PowerShell
                String safe = text
                        .replace("'", " ")
                        .replace("\"", " ")
                        .replace("`", " ")
                        .replace("\n", " ")
                        .replace("\r", " ");

                // Limit length to avoid very long reads
                if (safe.length() > 800) safe = safe.substring(0, 800) + "...";

                String script =
                        "Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$s.Rate = 1; " +
                        "$s.Speak('" + safe + "');";

                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe", "-NoProfile", "-NonInteractive",
                        "-WindowStyle", "Hidden", "-Command", script
                );
                pb.redirectErrorStream(true);
                currentProcess = pb.start();
                currentProcess.waitFor();

            } catch (InterruptedException ignored) {
                // stopped intentionally
            } catch (Exception e) {
                System.err.println("TTS error: " + e.getMessage());
            } finally {
                speaking = false;
                currentProcess = null;
                if (onStop != null) Platform.runLater(onStop);
            }
        });
    }

    /**
     * Stop any ongoing speech immediately.
     */
    public void stop() {
        if (currentProcess != null) {
            currentProcess.destroyForcibly();
            currentProcess = null;
        }
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        speaking = false;
        if (onStop != null) Platform.runLater(onStop);
    }

    /**
     * Toggle: if speaking → stop, else → speak the given text.
     */
    public void toggle(String text) {
        if (speaking) stop();
        else speak(text);
    }
}
