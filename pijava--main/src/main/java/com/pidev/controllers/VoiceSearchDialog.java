package com.pidev.controllers;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.File;
import java.util.function.Consumer;

/**
 * Dialog for voice search using WebView with HTML5 Speech Recognition
 * This embeds the voice-search.html interface in a JavaFX window
 */
public class VoiceSearchDialog {

    private Stage stage;
    private WebView webView;
    private WebEngine webEngine;
    private Consumer<String> onSearchCallback;

    /**
     * Create and show voice search dialog
     * @param owner Parent stage
     * @param onSearch Callback when search is triggered with the transcribed text
     */
    public VoiceSearchDialog(Stage owner, Consumer<String> onSearch) {
        this.onSearchCallback = onSearch;
        
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("🎤 Recherche Vocale");
        stage.setWidth(600);
        stage.setHeight(750);

        // Create WebView
        webView = new WebView();
        webEngine = webView.getEngine();
        
        // Enable JavaScript
        webEngine.setJavaScriptEnabled(true);
        
        // Load the HTML file
        File htmlFile = new File("src/main/resources/html/voice-search.html");
        if (htmlFile.exists()) {
            webEngine.load(htmlFile.toURI().toString());
        } else {
            System.err.println("❌ voice-search.html not found at: " + htmlFile.getAbsolutePath());
        }

        // Setup JavaScript bridge when page loads
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                setupJavaScriptBridge();
            }
        });

        // Bottom buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-background-color: #f8f9fa;");
        
        Button closeButton = new Button("❌ Fermer");
        closeButton.setStyle(
            "-fx-background-color: #e9ecef;" +
            "-fx-text-fill: #666;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> stage.close());
        
        buttonBox.getChildren().add(closeButton);

        // Layout
        BorderPane root = new BorderPane();
        root.setCenter(webView);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    /**
     * Setup JavaScript bridge to receive messages from the HTML page
     */
    private void setupJavaScriptBridge() {
        try {
            // Inject Java bridge object into JavaScript
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApp", new JavaScriptBridge());
            
            // Inject JavaScript to send search queries to Java
            String script = 
                "window.originalSearchHandler = window.searchButton.onclick;" +
                "window.searchButton.onclick = function() {" +
                "    var query = document.getElementById('transcriptText').textContent.trim();" +
                "    if (query && window.javaApp) {" +
                "        window.javaApp.onSearch(query);" +
                "    }" +
                "};";
            
            webEngine.executeScript(script);
            
            System.out.println("✅ JavaScript bridge setup complete");
        } catch (Exception e) {
            System.err.println("❌ Failed to setup JavaScript bridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show the dialog
     */
    public void show() {
        stage.show();
    }

    /**
     * Close the dialog
     */
    public void close() {
        stage.close();
    }

    /**
     * JavaScript Bridge class - accessible from JavaScript
     */
    public class JavaScriptBridge {
        
        /**
         * Called from JavaScript when user clicks search
         * @param query The transcribed text
         */
        public void onSearch(String query) {
            System.out.println("🎤 Voice search query: " + query);
            
            // Run on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                if (onSearchCallback != null) {
                    onSearchCallback.accept(query);
                }
                stage.close();
            });
        }
        
        /**
         * Called from JavaScript for logging
         * @param message Log message
         */
        public void log(String message) {
            System.out.println("📝 JS Log: " + message);
        }
        
        /**
         * Called from JavaScript for errors
         * @param error Error message
         */
        public void error(String error) {
            System.err.println("❌ JS Error: " + error);
        }
    }
}
