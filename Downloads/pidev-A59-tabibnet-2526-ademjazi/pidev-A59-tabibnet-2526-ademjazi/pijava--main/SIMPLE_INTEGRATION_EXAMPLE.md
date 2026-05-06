# 🚀 Exemple Simple d'Intégration

## Option Recommandée: Recherche Textuelle RAG (Sans Voix)

C'est la solution la plus simple pour une application desktop JavaFX!

---

## 📝 Étape 1: Modifier AnnuaireView.fxml

Ajoute ce code dans la section des filtres (après les ComboBox):

```xml
<!-- Recherche en Langage Naturel -->
<VBox spacing="10" style="-fx-padding: 20 24; -fx-background-color: rgba(139,92,246,0.08); -fx-background-radius: 12; -fx-border-color: rgba(139,92,246,0.2); -fx-border-radius: 12; -fx-border-width: 1;">
    <HBox spacing="8" alignment="CENTER_LEFT">
        <Label text="🤖" style="-fx-font-size: 18px;"/>
        <Label text="Recherche Intelligente" style="-fx-text-fill: #a78bfa; -fx-font-weight: 700; -fx-font-size: 14px;"/>
    </HBox>
    
    <Label text="Posez votre question en langage naturel (français, arabe ou anglais)" style="-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-wrap-text: true;"/>
    
    <HBox spacing="10">
        <TextField fx:id="naturalSearchField" promptText="Ex: Je cherche un cardiologue à Tunis..." HBox.hgrow="ALWAYS" style="-fx-background-color: #1a1f2e; -fx-text-fill: #f1f5f9; -fx-border-color: #2d3548; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 16; -fx-prompt-text-fill: #64748b; -fx-font-size: 14px;"/>
        
        <Button text="🔍 Rechercher" onAction="#handleNaturalSearch" style="-fx-background-color: linear-gradient(to right, #8b5cf6, #a855f7); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 12 24;"/>
    </HBox>
    
    <VBox spacing="4">
        <Label text="💡 Exemples:" style="-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: 700;"/>
        <Label text="• Je cherche un cardiologue à Tunis" style="-fx-text-fill: #64748b; -fx-font-size: 10px;"/>
        <Label text="• أريد طبيب أسنان في صفاقس" style="-fx-text-fill: #64748b; -fx-font-size: 10px;"/>
        <Label text="• I need a pediatrician in Ariana" style="-fx-text-fill: #64748b; -fx-font-size: 10px;"/>
    </VBox>
</VBox>
```

---

## 💻 Étape 2: Modifier AnnuaireController.java

### 2.1 Ajouter les Imports

```java
import com.pidev.services.RAGSearchService;
import org.json.JSONObject;
import javafx.application.Platform;
```

### 2.2 Ajouter le Champ

```java
@FXML private TextField naturalSearchField;
```

### 2.3 Ajouter les Méthodes

```java
/**
 * Handle natural language search using RAG
 */
@FXML
public void handleNaturalSearch() {
    String query = naturalSearchField.getText().trim();
    
    if (query.isEmpty()) {
        showError("Veuillez entrer une question");
        return;
    }
    
    performRAGSearch(query);
}

/**
 * Perform RAG search with natural language query
 * @param query Natural language question
 */
private void performRAGSearch(String query) {
    showLoading(true);
    hideError();
    
    new Thread(() -> {
        try {
            RAGSearchService ragService = new RAGSearchService();
            
            // Send query to RAG API
            JSONObject response = ragService.query(query, 8);
            
            Platform.runLater(() -> {
                if (ragService.isSuccess(response)) {
                    // Get AI response
                    String aiResponse = ragService.getResponseSentence(response);
                    
                    // Get doctors
                    List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
                    
                    // Update UI
                    doctorList.setAll(doctors);
                    doctorListView.setItems(doctorList);
                    updateStatusLabel();
                    
                    // Show AI response in status
                    if (statusLabel != null) {
                        statusLabel.setText("🤖 " + aiResponse);
                        statusLabel.setStyle("-fx-text-fill: #a78bfa;");
                    }
                    
                    showLoading(false);
                    
                } else if (ragService.isGreeting(response)) {
                    // Just a greeting
                    String greeting = ragService.getResponseSentence(response);
                    showError("👋 " + greeting);
                    showLoading(false);
                    
                } else if (ragService.isInsufficientContext(response)) {
                    // No results
                    String message = ragService.getResponseSentence(response);
                    showError("❌ " + message);
                    showLoading(false);
                }
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                showLoading(false);
                showError("Erreur de recherche: " + e.getMessage());
            });
            e.printStackTrace();
        }
    }).start();
}
```

---

## 🧪 Étape 3: Tester

### 1. Démarrer l'API Flask
```bash
cd flask-api
python app.py
```

### 2. Rebuild le Projet
```
Build → Rebuild Project
```

### 3. Run l'Application
```
▶️ Run
```

### 4. Tester la Recherche
1. Ouvre l'Annuaire
2. Tape: "Je cherche un cardiologue à Tunis"
3. Clique "🔍 Rechercher"
4. Vois les résultats!

---

## 📊 Résultat Attendu

```
┌─────────────────────────────────────────────────┐
│ 🤖 Recherche Intelligente                       │
├─────────────────────────────────────────────────┤
│ Posez votre question en langage naturel...     │
│                                                 │
│ ┌─────────────────────────────┐  ┌──────────┐ │
│ │ Je cherche un cardiologue...│  │🔍 Rechercher│
│ └─────────────────────────────┘  └──────────┘ │
│                                                 │
│ 💡 Exemples:                                    │
│ • Je cherche un cardiologue à Tunis            │
│ • أريد طبيب أسنان في صفاقس                     │
└─────────────────────────────────────────────────┘

↓ Après recherche ↓

┌─────────────────────────────────────────────────┐
│ 🤖 Selon les informations disponibles, voici   │
│    les cardiologues à Tunis:                    │
├─────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────┐│
│ │ AH  Dr. Ahmed Ben Ali                       ││
│ │     Cardiology                              ││
│ │     📍 Tunis  📞 71 123 456                 ││
│ └─────────────────────────────────────────────┘│
│ ┌─────────────────────────────────────────────┐│
│ │ FT  Dr. Fatma Trabelsi                      ││
│ │     Cardiology                              ││
│ │     📍 Tunis  📞 71 234 567                 ││
│ └─────────────────────────────────────────────┘│
└─────────────────────────────────────────────────┘
```

---

## ✅ Avantages de Cette Solution

1. **Simple** - Pas de WebView, pas de microphone
2. **Puissant** - Utilise l'IA pour comprendre
3. **Multilingue** - FR, AR, EN
4. **Rapide** - Résultats en 2-3 secondes
5. **Desktop-friendly** - Pure JavaFX

---

## 🎯 Prochaines Étapes (Optionnel)

Si tu veux ajouter la recherche vocale plus tard:

1. Ajoute un bouton "🎤 Vocal" à côté de "🔍 Rechercher"
2. Utilise `VoiceSearchDialog` que j'ai créé
3. Ça ouvrira une fenêtre avec le microphone

---

**C'est tout! Simple et efficace pour une app desktop!** 🚀
