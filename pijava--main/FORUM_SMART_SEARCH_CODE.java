// ═══════════════════════════════════════════════════════════════════════════
// 🔍 SMART SEARCH - Autocomplétion comme Facebook
// ═══════════════════════════════════════════════════════════════════════════
// À ajouter dans ForumController.java

// ═══════════════════════════════════════════════════════════════════════════
// 1️⃣ CHAMPS À AJOUTER EN HAUT DE LA CLASSE
// ═══════════════════════════════════════════════════════════════════════════

// Search history and suggestions
private List<String> searchHistory = new ArrayList<>();
private static final int MAX_HISTORY = 10;
private VBox searchSuggestionsBox;
private boolean suggestionsVisible = false;

// ═══════════════════════════════════════════════════════════════════════════
// 2️⃣ MODIFIER LA MÉTHODE initialize()
// ═══════════════════════════════════════════════════════════════════════════

@FXML
public void initialize() {
    loadSpecialites();
    loadQuestions();
    updateStats();

    // ═══════════════════════════════════════════════════════════════════════
    // 🔍 SMART SEARCH SETUP
    // ═══════════════════════════════════════════════════════════════════════
    
    // Create suggestions box
    createSearchSuggestionsBox();
    
    // Show history when clicking on search field
    searchField.setOnMouseClicked(e -> {
        if (!suggestionsVisible) {
            showSearchHistory();
        }
    });
    
    // Show suggestions while typing
    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal != null && !newVal.trim().isEmpty()) {
            showSmartSuggestions(newVal);
        } else {
            showSearchHistory();
        }
        filterQuestions();
    });
    
    // Hide suggestions when clicking outside
    searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (!isNowFocused) {
            // Delay to allow clicking on suggestions
            Platform.runLater(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {}
                hideSearchSuggestions();
            });
        }
    });

    showStatus("Forum communautaire chargé", "#22c55e");
}

// ═══════════════════════════════════════════════════════════════════════════
// 3️⃣ CRÉER LA BOX DE SUGGESTIONS
// ═══════════════════════════════════════════════════════════════════════════

private void createSearchSuggestionsBox() {
    searchSuggestionsBox = new VBox(2);
    searchSuggestionsBox.setStyle(
        "-fx-background-color: #1c2133;" +
        "-fx-background-radius: 0 0 8 8;" +
        "-fx-border-color: #5b6ef5;" +
        "-fx-border-width: 0 2 2 2;" +
        "-fx-border-radius: 0 0 8 8;" +
        "-fx-padding: 8;" +
        "-fx-max-height: 300;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
    );
    searchSuggestionsBox.setVisible(false);
    searchSuggestionsBox.setManaged(false);
    
    // Add to parent (need to get parent of searchField)
    Platform.runLater(() -> {
        if (searchField.getParent() instanceof Pane) {
            Pane parent = (Pane) searchField.getParent();
            
            // Position below search field
            searchSuggestionsBox.layoutXProperty().bind(searchField.layoutXProperty());
            searchSuggestionsBox.layoutYProperty().bind(
                searchField.layoutYProperty().add(searchField.heightProperty())
            );
            searchSuggestionsBox.prefWidthProperty().bind(searchField.widthProperty());
            
            parent.getChildren().add(searchSuggestionsBox);
        }
    });
}

// ═══════════════════════════════════════════════════════════════════════════
// 4️⃣ AFFICHER L'HISTORIQUE DE RECHERCHE
// ═══════════════════════════════════════════════════════════════════════════

private void showSearchHistory() {
    searchSuggestionsBox.getChildren().clear();
    
    if (searchHistory.isEmpty()) {
        // Show popular searches or recent questions
        Label header = new Label("🔥 Recherches populaires");
        header.setStyle(
            "-fx-text-fill: #94a3b8;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 4 8;"
        );
        searchSuggestionsBox.getChildren().add(header);
        
        // Add some popular search terms from questions
        List<String> popularTerms = getPopularSearchTerms();
        for (String term : popularTerms) {
            HBox item = createSuggestionItem("🔍", term, false);
            searchSuggestionsBox.getChildren().add(item);
        }
    } else {
        // Show search history
        Label header = new Label("🕐 Historique de recherche");
        header.setStyle(
            "-fx-text-fill: #94a3b8;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 4 8;"
        );
        searchSuggestionsBox.getChildren().add(header);
        
        for (String query : searchHistory) {
            HBox item = createSuggestionItem("🕐", query, true);
            searchSuggestionsBox.getChildren().add(item);
        }
    }
    
    searchSuggestionsBox.setVisible(true);
    searchSuggestionsBox.setManaged(true);
    suggestionsVisible = true;
}

// ═══════════════════════════════════════════════════════════════════════════
// 5️⃣ AFFICHER LES SUGGESTIONS INTELLIGENTES
// ═══════════════════════════════════════════════════════════════════════════

private void showSmartSuggestions(String query) {
    searchSuggestionsBox.getChildren().clear();
    
    String lowerQuery = query.toLowerCase().trim();
    
    // Get matching suggestions
    List<String> suggestions = new ArrayList<>();
    
    // 1. Search in history
    for (String historyItem : searchHistory) {
        if (historyItem.toLowerCase().contains(lowerQuery)) {
            suggestions.add(historyItem);
        }
    }
    
    // 2. Search in question titles
    for (Question q : allQuestions) {
        if (q.getTitre() != null && q.getTitre().toLowerCase().contains(lowerQuery)) {
            String title = q.getTitre();
            if (!suggestions.contains(title) && suggestions.size() < 8) {
                suggestions.add(title);
            }
        }
    }
    
    // 3. Search in specialties
    if (specialites != null) {
        for (Specialite s : specialites) {
            if (s.getNom() != null && s.getNom().toLowerCase().contains(lowerQuery)) {
                String specName = "Spécialité: " + s.getNom();
                if (!suggestions.contains(specName) && suggestions.size() < 8) {
                    suggestions.add(specName);
                }
            }
        }
    }
    
    // 4. Search in descriptions (keywords)
    Set<String> keywords = extractKeywords(lowerQuery);
    for (Question q : allQuestions) {
        if (q.getDescription() != null) {
            String desc = q.getDescription().toLowerCase();
            for (String keyword : keywords) {
                if (desc.contains(keyword) && suggestions.size() < 8) {
                    // Extract a snippet
                    int index = desc.indexOf(keyword);
                    int start = Math.max(0, index - 20);
                    int end = Math.min(desc.length(), index + keyword.length() + 20);
                    String snippet = desc.substring(start, end).trim();
                    if (!suggestions.contains(snippet)) {
                        suggestions.add("..." + snippet + "...");
                    }
                }
            }
        }
    }
    
    // Display suggestions
    if (suggestions.isEmpty()) {
        Label noResults = new Label("Aucune suggestion");
        noResults.setStyle(
            "-fx-text-fill: #64748b;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8;"
        );
        searchSuggestionsBox.getChildren().add(noResults);
    } else {
        Label header = new Label("💡 Suggestions");
        header.setStyle(
            "-fx-text-fill: #94a3b8;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 4 8;"
        );
        searchSuggestionsBox.getChildren().add(header);
        
        for (String suggestion : suggestions) {
            HBox item = createSuggestionItem("🔍", suggestion, false);
            searchSuggestionsBox.getChildren().add(item);
        }
    }
    
    searchSuggestionsBox.setVisible(true);
    searchSuggestionsBox.setManaged(true);
    suggestionsVisible = true;
}

// ═══════════════════════════════════════════════════════════════════════════
// 6️⃣ CRÉER UN ITEM DE SUGGESTION
// ═══════════════════════════════════════════════════════════════════════════

private HBox createSuggestionItem(String icon, String text, boolean isHistory) {
    HBox item = new HBox(10);
    item.setAlignment(Pos.CENTER_LEFT);
    item.setPadding(new Insets(8, 12, 8, 12));
    item.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-background-radius: 6;" +
        "-fx-cursor: hand;"
    );
    
    // Icon
    Label iconLabel = new Label(icon);
    iconLabel.setStyle("-fx-font-size: 14px;");
    
    // Text
    Label textLabel = new Label(text);
    textLabel.setStyle(
        "-fx-text-fill: #f1f5f9;" +
        "-fx-font-size: 13px;"
    );
    textLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(textLabel, Priority.ALWAYS);
    
    item.getChildren().addAll(iconLabel, textLabel);
    
    // Delete button for history items
    if (isHistory) {
        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #64748b;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 2 6;" +
            "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> {
            searchHistory.remove(text);
            showSearchHistory();
            e.consume(); // Prevent item click
        });
        item.getChildren().add(deleteBtn);
    }
    
    // Hover effect
    item.setOnMouseEntered(e -> 
        item.setStyle(
            "-fx-background-color: rgba(91,110,245,0.15);" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        )
    );
    item.setOnMouseExited(e -> 
        item.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        )
    );
    
    // Click to use suggestion
    item.setOnMouseClicked(e -> {
        searchField.setText(text.replace("Spécialité: ", "").replace("...", "").trim());
        addToSearchHistory(text);
        hideSearchSuggestions();
        filterQuestions();
    });
    
    return item;
}

// ═══════════════════════════════════════════════════════════════════════════
// 7️⃣ MÉTHODES UTILITAIRES
// ═══════════════════════════════════════════════════════════════════════════

private void hideSearchSuggestions() {
    searchSuggestionsBox.setVisible(false);
    searchSuggestionsBox.setManaged(false);
    suggestionsVisible = false;
}

private void addToSearchHistory(String query) {
    if (query == null || query.trim().isEmpty()) return;
    
    query = query.trim();
    
    // Remove if already exists
    searchHistory.remove(query);
    
    // Add to beginning
    searchHistory.add(0, query);
    
    // Limit size
    if (searchHistory.size() > MAX_HISTORY) {
        searchHistory = new ArrayList<>(searchHistory.subList(0, MAX_HISTORY));
    }
}

private List<String> getPopularSearchTerms() {
    List<String> popular = new ArrayList<>();
    
    // Get most common words from question titles
    Map<String, Integer> wordCount = new HashMap<>();
    
    for (Question q : allQuestions) {
        if (q.getTitre() != null) {
            String[] words = q.getTitre().toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.length() > 3) { // Ignore short words
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }
    }
    
    // Sort by frequency
    popular = wordCount.entrySet().stream()
        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
        .limit(5)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    
    return popular;
}

private Set<String> extractKeywords(String query) {
    Set<String> keywords = new HashSet<>();
    String[] words = query.split("\\s+");
    for (String word : words) {
        if (word.length() > 2) {
            keywords.add(word.toLowerCase());
        }
    }
    return keywords;
}

// ═══════════════════════════════════════════════════════════════════════════
// 8️⃣ IMPORTS NÉCESSAIRES
// ═══════════════════════════════════════════════════════════════════════════

/*
Ajouter ces imports en haut du fichier :

import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
*/
