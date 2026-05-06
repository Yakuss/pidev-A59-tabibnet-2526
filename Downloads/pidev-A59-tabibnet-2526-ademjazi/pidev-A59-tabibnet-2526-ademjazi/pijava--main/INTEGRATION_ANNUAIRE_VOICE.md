# 🎤 Intégration Recherche Vocale dans AnnuaireController

## 📋 2 Options d'Intégration

### Option 1: Recherche Vocale (WebView) 🎤
Utilise WebView pour afficher l'interface HTML avec microphone

### Option 2: Recherche Textuelle RAG 💬
Champ de recherche en langage naturel (sans voix)

---

## 🎤 Option 1: Recherche Vocale avec WebView

### Étape 1: Ajouter le Bouton dans AnnuaireView.fxml

```xml
<!-- Dans le header, après le bouton Refresh -->
<Button text="🎤 Recherche Vocale" 
        onAction="#openVoiceSearch"
        prefHeight="42"
        style="-fx-background-color: linear-gradient(to right, #f093fb, #f5576c); 
               -fx-text-fill: white; 
               -fx-font-weight: 800; 
               -fx-font-size: 14px; 
               -fx-background-radius: 12; 
               -fx-cursor: hand; 
               -fx-padding: 0 24; 
               -fx-effect: dropshadow(gaussian, rgba(245,87,108,0.5), 12, 0, 0, 4);"/>
```

### Étape 2: Ajouter la Méthode dans AnnuaireController.java

```java
import com.pidev.controllers.VoiceSearchDialog;
import com.pidev.services.RAGSearchService;
import javafx.stage.Stage;

// Dans AnnuaireController.java

/**
 * Open voice search dialog
 */
@FXML
public void openVoiceSearch() {
    try {
        // Get current stage
        Stage stage = (Stage) searchNameField.getScene().getWindow();
        
        // Create and show voice search dialog
        VoiceSearchDialog dialog = new VoiceSearchDialog(stage, this::performRAGSearch);
        dialog.show();
        
    } catch (Exception e) {
        showError("Erreur lors de l'ouverture de la recherche vocale: " + e.getMessage());
        e.printStackTrace();
    }
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
                    
                    // Show AI response
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Résultats de la recherche");
                    alert.setHeaderText("🤖 Assistant IA");
                    alert.setContentText(aiResponse + "\n\n" + doctors.size() + " médecin(s) trouvé(s)");
                    alert.showAndWait();
                    
                } else if (ragService.isGreeting(response)) {
                    // Just a greeting
                    String greeting = ragService.getResponseSentence(response);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Assistant IA");
                    alert.setHeaderText("👋 Bonjour!");
                    alert.setContentText(greeting);
                    alert.showAndWait();
                    
                } else if (ragService.isInsufficientContext(response)) {
                    // No results
                    String message = ragService.getResponseSentence(response);
                    showError(message);
                }
                
                showLoading(false);
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

## 💬 Option 2: Recherche Textuelle RAG (Plus Simple)

### Étape 1: Ajouter le Champ dans AnnuaireView.fxml

```xml
<!-- Après les filtres existants -->
<VBox spacing="12" style="-fx-padding: 20 24;">
    <Label text="🤖 Recherche en Langage Naturel" 
           style="-fx-text-fill: #cbd5e1; -fx-font-weight: 700; -fx-font-size: 12px;"/>
    
    <HBox spacing="10">
        <TextField fx:id="naturalSearchField" 
                   promptText="Ex: Je cherche un cardiologue à Tunis..."
                   HBox.hgrow="ALWAYS"
                   style="-fx-background-color: #1a1f2e; 
                          -fx-text-fill: #f1f5f9; 
                          -fx-border-color: #2d3548; 
                          -fx-border-radius: 10; 
                          -fx-background-radius: 10; 
                          -fx-padding: 12 16; 
                          -fx-prompt-text-fill: #64748b; 
                          -fx-font-size: 14px;"/>
        
        <Button text="🔍 Rechercher IA" 
                onAction="#handleNaturalSearch"
                style="-fx-background-color: linear-gradient(to right, #8b5cf6, #a855f7); 
                       -fx-text-fill: white; 
                       -fx-font-weight: 800; 
                       -fx-font-size: 14px; 
                       -fx-background-radius: 10; 
                       -fx-cursor: hand; 
                       -fx-padding: 12 24;"/>
    </HBox>
    
    <Label text="💡 Posez votre question en français, arabe ou anglais" 
           style="-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-style: italic;"/>
</VBox>
```

### Étape 2: Ajouter dans AnnuaireController.java

```java
@FXML private TextField naturalSearchField;

/**
 * Handle natural language search
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
```

---

## 🎨 Exemple Complet d'Intégration

### AnnuaireView.fxml (Header Section)

```xml
<HBox alignment="CENTER_LEFT" spacing="16">
    <VBox HBox.hgrow="ALWAYS" spacing="6">
        <Label text="🏥 Annuaire National des Médecins" styleClass="page-title"/>
        <Label text="Recherchez parmi tous les médecins de libre pratique en Tunisie" styleClass="page-subtitle"/>
    </VBox>
    
    <!-- Bouton Refresh -->
    <Button text="🔄 Actualiser" onAction="#handleRefresh" prefHeight="42"
            style="-fx-background-color: #1a1f2e; -fx-text-fill: #94a3b8; -fx-border-color: #2d3548; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-padding: 0 24; -fx-font-size: 14px; -fx-font-weight: 700;"/>
    
    <!-- Bouton Recherche Vocale -->
    <Button text="🎤 Recherche Vocale" onAction="#openVoiceSearch" prefHeight="42"
            style="-fx-background-color: linear-gradient(to right, #f093fb, #f5576c); -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 14px; -fx-background-radius: 12; -fx-cursor: hand; -fx-padding: 0 24; -fx-effect: dropshadow(gaussian, rgba(245,87,108,0.5), 12, 0, 0, 4);"/>
</HBox>
```

---

## 🧪 Test Rapide

### 1. Test Recherche RAG (Sans Interface)

```java
public static void main(String[] args) {
    RAGSearchService ragService = new RAGSearchService();
    
    try {
        // Test query
        JSONObject response = ragService.query("Je cherche un cardiologue à Tunis", 8);
        
        if (ragService.isSuccess(response)) {
            System.out.println("✅ Success!");
            System.out.println("IA: " + ragService.getResponseSentence(response));
            
            List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
            System.out.println("Trouvé: " + doctors.size() + " médecins");
            
            for (DoctorAPI doctor : doctors) {
                System.out.println("- Dr. " + doctor.getName());
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 2. Test Voice Dialog

```java
public static void main(String[] args) {
    Application.launch(TestApp.class, args);
}

public static class TestApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        VoiceSearchDialog dialog = new VoiceSearchDialog(primaryStage, query -> {
            System.out.println("🎤 Recherche: " + query);
        });
        dialog.show();
    }
}
```

---

## 📊 Comparaison des Options

### Option 1: Recherche Vocale (WebView)
```
Avantages:
✅ Recherche mains libres
✅ Interface moderne
✅ Support multilingue
✅ Transcription en temps réel

Inconvénients:
❌ Nécessite WebView
❌ Permissions microphone
❌ Navigateur compatible requis
```

### Option 2: Recherche Textuelle RAG
```
Avantages:
✅ Plus simple à implémenter
✅ Pas de WebView nécessaire
✅ Fonctionne partout
✅ Même puissance IA

Inconvénients:
❌ Pas de voix
❌ Nécessite de taper
```

---

## 🚀 Recommandation

**Pour une application desktop JavaFX:**

1. **Commencer avec Option 2** (Recherche Textuelle RAG)
   - Plus simple
   - Fonctionne immédiatement
   - Même résultats IA

2. **Ajouter Option 1** (Recherche Vocale) si besoin
   - Pour les utilisateurs avancés
   - Comme fonctionnalité bonus

---

## 📝 Checklist d'Intégration

### Pour Option 1 (Vocale):
- [ ] Ajouter bouton "🎤 Recherche Vocale" dans FXML
- [ ] Ajouter méthode `openVoiceSearch()` dans Controller
- [ ] Ajouter méthode `performRAGSearch(String query)` dans Controller
- [ ] Tester avec l'API Flask en marche

### Pour Option 2 (Textuelle):
- [ ] Ajouter TextField `naturalSearchField` dans FXML
- [ ] Ajouter bouton "🔍 Rechercher IA" dans FXML
- [ ] Ajouter méthode `handleNaturalSearch()` dans Controller
- [ ] Ajouter méthode `performRAGSearch(String query)` dans Controller
- [ ] Tester avec l'API Flask en marche

---

## ⚠️ Prérequis

1. **API Flask** doit tourner sur `localhost:5000`
   ```bash
   cd flask-api
   python app.py
   ```

2. **JavaFX WebView** (pour Option 1)
   - Inclus dans JavaFX 17+

3. **Imports nécessaires**
   ```java
   import com.pidev.services.RAGSearchService;
   import com.pidev.controllers.VoiceSearchDialog;
   import org.json.JSONObject;
   import javafx.application.Platform;
   ```

---

**Quelle option préfères-tu? Je peux t'aider à l'intégrer!** 🚀
