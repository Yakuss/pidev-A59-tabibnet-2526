// ═══════════════════════════════════════════════════════════════════════════
// 🔍 SMART SEARCH - VERSION QUI FONCTIONNE
// ═══════════════════════════════════════════════════════════════════════════
// À ajouter dans ForumController.java

// ═══════════════════════════════════════════════════════════════════════════
// 1️⃣ IMPORTS À AJOUTER EN HAUT DU FICHIER
// ═══════════════════════════════════════════════════════════════════════════

import javafx.stage.Popup;
import javafx.scene.control.ScrollPane;
import java.util.*;
import java.util.stream.Collectors;

// ═══════════════════════════════════════════════════════════════════════════
// 2️⃣ CHAMPS À AJOUTER EN HAUT DE LA CLASSE
// ═══════════════════════════════════════════════════════════════════════════

// Search suggestions popup
private Popup searchPopup;
private VBox searchSuggestionsBox;
private List<String> searchHistory = new ArrayList<>();
private static final int MAX_HISTORY = 10;

// ═══════════════════════════════════════════════════════════════════════════
// 3️⃣ AJOUTER À LA FIN DE initialize()
// ═══════════════════════════════════════════════════════════════════════════

// Dans initialize(), APRÈS showStatus(...), ajouter :

// Setup smart search
setupSmartSearch();

// ═══════════════════════════════════════════════════════════════════════════
// 4️⃣ MÉTHODE setupSmartSearch() - À AJOUTER DANS LA CLASSE
// ═══════════════════════════════════════════════════════════════════════════

private void setupSmartSearch() {
    // Create popup
    searchPopup = new Popup();
    searchPopup.setAutoHide(true);
    
    // Create suggestions box
    searchSuggestionsBox = new VBox(2);
    searchSuggestionsBox.setStyle(
        "-fx-background-color: #1c2133;" +
        "-fx-background-radius: 8;" +
        "-fx-border-color: #5b6ef5;" +
        "-fx-border-width: 2;" +
        "-fx-border-radius: 8;" +
        "-fx-padding: 8;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 5);"
    );
    searchSuggestionsBox.setPrefWidth(280);
    searchSuggestionsBox.setMaxHeight(300);
    
    // Wrap in ScrollPane
    ScrollPane scrollPane = new ScrollPane(searchSuggestionsBox);
    scrollPane.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-background: transparent;" +
        "-fx-border-color: transparent;"
    );
    scrollPane.setFitToWidth(true);
    scrollPane.setMaxHeight(300);
    scrollPane.setPrefWidth(280);
    
    searchPopup.getContent().add(scrollPane);
    
    // Show popup on focus
    searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            showSearchSuggestions();
        }
    });
    
    // Update suggestions while typing
    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (searchField.isFocused()) {
            if (newVal != null && !newVal.trim().isEmpty()) {
                showSmartSuggestions(newVal);
            } else {
                showSearchHistory();
            }
        }
    });
}

// ═══════════════════════════════════════════════════════════════════════════
// 5️⃣ AFFICHER LES SUGGESTIONS
// ═══════════════════════════════════════════════════════════════════════════

private void showSearchSuggestions() {
    if (searchField.getText() == null || searchField.getText().trim().isEmpty()) {
        showSearchHistory();
    } else {
        showSmartSuggestions(searchField.getText());
    }
    
    // Position popup below search field
    javafx.geometry.Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
    if (bounds != null) {
        searchPopup.show(searchField, bounds.getMinX(), bounds.getMaxY() + 2);
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 6️⃣ AFFICHER L'HISTORIQUE
// ═══════════════════════════════════════════════════════════════════════════

private void showSearchHistory() {
    searchSuggestionsBox.getChildren().clear();
    
    if (searchHistory.isEmpty()) {
        // Show popular searches
        Label header = new Label("🔥 Recherches populaires");
        header.setStyle(
            "-fx-text-fill: #94a3b8;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 4 8 8 8;"
        );
        searchSuggestionsBox.getChildren().add(header);
        
        List<String> popular = getPopularSearchTerms();
        for (String term : popular) {
            searchSuggestionsBox.getChildren().add(createSuggestionItem("🔍", term, false));
        }
    } else {
        // Show history
        Label header = new Label("🕐 Historique de recherche");
        header.setStyle(
            "-fx-text-fill: #94a3b8;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 4 8 8 8;"
        );
        searchSuggestionsBox.getChildren().add(header);
        
        for (String query : searchHistory) {
            searchSuggestionsBox.getChildren().add(createSuggestionItem("🕐", query, true));
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 7️⃣ AFFICHER LES SUGGESTIONS INTELLIGENTES
// ═══════════════════════════════════════════════════════════════════════════

private void showSmartSuggestions(String query) {
    searchSuggestionsBox.getChildren().clear();
    
    String lowerQuery = query.toLowerCase().trim();
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
            if (!suggestions.contains(q.getTitre()) && suggestions.size() < 8) {
                suggestions.add(q.getTitre());
            }
        }
    }
    
    // 3. Search in specialties
    if (specialites != null) {
        for (Specialite s : specialites) {
            if (s.getNom() != null && s.getNom().toLowerCase().contains(lowerQuery)) {
                String specName = "📋 " + s.getNom();
                if (!suggestions.contains(specName) && suggestions.size() < 8) {
                    suggestions.add(specName);
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
            "-fx-padding: 4 8 8 8;"
        );
        searchSuggestionsBox.getChildren().add(header);
        
        for (String suggestion : suggestions) {
            searchSuggestionsBox.getChildren().add(createSuggestionItem("🔍", suggestion, false));
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// 8️⃣ CRÉER UN ITEM DE SUGGESTION
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
    textLabel.setMaxWidth(200);
    HBox.setHgrow(textLabel, Priority.ALWAYS);
    
    item.getChildren().addAll(iconLabel, textLabel);
    
    // Delete button for history
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
            e.consume();
        });
        item.getChildren().add(deleteBtn);
    }
    
    // Hover effect
    item.setOnMouseEntered(e -> 
        item.setStyle(
            "-fx-background-color: rgba(91,110,245,0.2);" +
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
    
    // Click to use
    item.setOnMouseClicked(e -> {
        String cleanText = text.replace("📋 ", "").trim();
        searchField.setText(cleanText);
        addToSearchHistory(cleanText);
        searchPopup.hide();
        filterQuestions();
    });
    
    return item;
}

// ═══════════════════════════════════════════════════════════════════════════
// 9️⃣ MÉTHODES UTILITAIRES
// ═══════════════════════════════════════════════════════════════════════════

private void addToSearchHistory(String query) {
    if (query == null || query.trim().isEmpty()) return;
    
    query = query.trim();
    searchHistory.remove(query);
    searchHistory.add(0, query);
    
    if (searchHistory.size() > MAX_HISTORY) {
        searchHistory = new ArrayList<>(searchHistory.subList(0, MAX_HISTORY));
    }
}

private List<String> getPopularSearchTerms() {
    List<String> popular = new ArrayList<>();
    Map<String, Integer> wordCount = new HashMap<>();
    
    for (Question q : allQuestions) {
        if (q.getTitre() != null) {
            String[] words = q.getTitre().toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.length() > 3) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }
    }
    
    popular = wordCount.entrySet().stream()
        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
        .limit(5)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    
    return popular;
}

// ═══════════════════════════════════════════════════════════════════════════
// ✅ RÉSUMÉ DE L'INSTALLATION
// ═══════════════════════════════════════════════════════════════════════════

/*
ÉTAPES D'INSTALLATION :

1. Copier les IMPORTS (section 1️⃣) en haut du fichier

2. Copier les CHAMPS (section 2️⃣) en haut de la classe ForumController

3. Ajouter setupSmartSearch(); à la fin de initialize() (section 3️⃣)

4. Copier TOUTES LES MÉTHODES (sections 4️⃣ à 9️⃣) dans la classe

5. Rebuild le projet

6. Tester :
   - Cliquer sur le champ de recherche → Affiche historique/populaires
   - Taper "dia" → Affiche suggestions
   - Cliquer sur une suggestion → Applique la recherche
*/
