# 🎤 Guide Complet - Intégration Vosk Speech-to-Text

## 🌟 Pourquoi Vosk?

✅ **Offline** - Fonctionne sans internet  
✅ **Gratuit** - Pas d'API key  
✅ **Multilingue** - FR, AR, EN et 20+ langues  
✅ **Léger** - Modèles de 39-66 MB  
✅ **Rapide** - Reconnaissance en temps réel  
✅ **Java natif** - Pas de WebView  
✅ **Desktop-friendly** - Parfait pour JavaFX  

---

## 📦 Étape 1: Ajouter Vosk au Projet

### 1.1 Ajouter la Dépendance Maven

Ajoute dans `pom.xml`:

```xml
<dependencies>
    <!-- Vosk Speech Recognition -->
    <dependency>
        <groupId>com.alphacephei</groupId>
        <artifactId>vosk</artifactId>
        <version>0.3.45</version>
    </dependency>
    
    <!-- JSON (si pas déjà présent) -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20231013</version>
    </dependency>
</dependencies>
```

### 1.2 Rebuild le Projet

```
Build → Rebuild Project
```

---

## 📥 Étape 2: Télécharger les Modèles

### 2.1 Créer le Dossier

```bash
cd pijava--main
mkdir models
cd models
```

### 2.2 Télécharger les Modèles

#### Français (Recommandé - 39 MB)
```bash
# Télécharger
wget https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip

# Extraire
unzip vosk-model-small-fr-0.22.zip
```

#### Arabe (66 MB)
```bash
# Télécharger
wget https://alphacephei.com/vosk/models/vosk-model-ar-0.22-linto-1.1.0.zip

# Extraire
unzip vosk-model-ar-0.22-linto-1.1.0.zip
```

#### Anglais (40 MB)
```bash
# Télécharger
wget https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip

# Extraire
unzip vosk-model-small-en-us-0.15.zip
```

### 2.3 Structure Finale

```
pijava--main/
├── models/
│   ├── vosk-model-small-fr-0.22/
│   │   ├── am/
│   │   ├── conf/
│   │   ├── graph/
│   │   └── ...
│   ├── vosk-model-ar-0.22-linto-1.1.0/
│   └── vosk-model-small-en-us-0.15/
├── src/
└── pom.xml
```

**Alternative:** Télécharge manuellement depuis:  
https://alphacephei.com/vosk/models

---

## 🔧 Étape 3: Intégration dans AnnuaireController

### 3.1 Ajouter le Bouton dans FXML

```xml
<!-- Dans AnnuaireView.fxml, dans le header -->
<Button text="🎤 Recherche Vocale" 
        onAction="#openVoskVoiceSearch"
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

### 3.2 Ajouter la Méthode dans Controller

```java
import com.pidev.controllers.VoskVoiceSearchDialog;
import com.pidev.services.RAGSearchService;
import javafx.stage.Stage;

/**
 * Open Vosk voice search dialog
 */
@FXML
public void openVoskVoiceSearch() {
    try {
        // Get current stage
        Stage stage = (Stage) searchNameField.getScene().getWindow();
        
        // Path to Vosk model (French by default)
        String modelPath = "models/vosk-model-small-fr-0.22";
        
        // Create and show dialog
        VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
            stage, 
            modelPath, 
            this::performRAGSearch
        );
        dialog.show();
        
    } catch (Exception e) {
        showError("Erreur lors de l'ouverture de la recherche vocale: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * Perform RAG search with transcribed text
 */
private void performRAGSearch(String query) {
    showLoading(true);
    hideError();
    
    new Thread(() -> {
        try {
            RAGSearchService ragService = new RAGSearchService();
            JSONObject response = ragService.query(query, 8);
            
            Platform.runLater(() -> {
                if (ragService.isSuccess(response)) {
                    String aiResponse = ragService.getResponseSentence(response);
                    List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
                    
                    doctorList.setAll(doctors);
                    doctorListView.setItems(doctorList);
                    updateStatusLabel();
                    
                    if (statusLabel != null) {
                        statusLabel.setText("🤖 " + aiResponse);
                        statusLabel.setStyle("-fx-text-fill: #a78bfa;");
                    }
                    
                } else if (ragService.isGreeting(response)) {
                    showError("👋 " + ragService.getResponseSentence(response));
                    
                } else if (ragService.isInsufficientContext(response)) {
                    showError("❌ " + ragService.getResponseSentence(response));
                }
                
                showLoading(false);
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                showLoading(false);
                showError("Erreur de recherche: " + e.getMessage());
            });
        }
    }).start();
}
```

---

## 🧪 Étape 4: Test

### 4.1 Test Simple

```java
public static void main(String[] args) {
    try {
        VoskSpeechService vosk = new VoskSpeechService("models/vosk-model-small-fr-0.22");
        
        vosk.setOnFinalResult(text -> {
            System.out.println("✅ Transcription: " + text);
        });
        
        System.out.println("🎤 Parlez maintenant...");
        vosk.startListening();
        
        Thread.sleep(10000); // 10 secondes
        
        vosk.stopListening();
        vosk.dispose();
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 4.2 Test Complet

1. **Rebuild** le projet
2. **Run** l'application
3. **Ouvrir** l'Annuaire
4. **Cliquer** sur "🎤 Recherche Vocale"
5. **Parler**: "Je cherche un cardiologue à Tunis"
6. **Voir** les résultats!

---

## 🎨 Interface Vosk

```
┌─────────────────────────────────────────┐
│      🎤 Recherche Vocale                │
│      Parlez pour rechercher un médecin  │
├─────────────────────────────────────────┤
│                                         │
│   Langue: [Français ▼]                  │
│                                         │
│            ┌─────────┐                  │
│            │   🎤    │  ← Cliquez       │
│            └─────────┘                  │
│                                         │
│   Cliquez sur le microphone pour        │
│   commencer                             │
│                                         │
│   ┌───────────────────────────────────┐ │
│   │ 📝 Transcription:                 │ │
│   │                                   │ │
│   │ Je cherche un cardiologue à Tunis│ │
│   │                                   │ │
│   └───────────────────────────────────┘ │
│                                         │
│   ┌──────────┐ ┌─────────┐ ┌─────────┐│
│   │🔍 Rechercher│ │🔄 Effacer│ │❌ Fermer││
│   └──────────┘ └─────────┘ └─────────┘│
│                                         │
│   💡 Exemples:                          │
│   • Je cherche un cardiologue à Tunis   │
│   • أريد طبيب أسنان في صفاقس            │
│   • I need a pediatrician in Ariana     │
└─────────────────────────────────────────┘
```

---

## 🌍 Support Multilingue

### Changer de Langue

```java
// Français
VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
    stage, 
    "models/vosk-model-small-fr-0.22", 
    this::performRAGSearch
);

// Arabe
VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
    stage, 
    "models/vosk-model-ar-0.22-linto-1.1.0", 
    this::performRAGSearch
);

// Anglais
VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
    stage, 
    "models/vosk-model-small-en-us-0.15", 
    this::performRAGSearch
);
```

### Sélection Dynamique

```java
@FXML
public void openVoskVoiceSearch() {
    // Demander la langue
    ChoiceDialog<String> langDialog = new ChoiceDialog<>("Français", 
        "Français", "العربية", "English");
    langDialog.setTitle("Langue");
    langDialog.setHeaderText("Choisissez la langue de reconnaissance");
    
    Optional<String> result = langDialog.showAndWait();
    
    if (result.isPresent()) {
        String modelPath;
        switch (result.get()) {
            case "العربية":
                modelPath = "models/vosk-model-ar-0.22-linto-1.1.0";
                break;
            case "English":
                modelPath = "models/vosk-model-small-en-us-0.15";
                break;
            default:
                modelPath = "models/vosk-model-small-fr-0.22";
        }
        
        Stage stage = (Stage) searchNameField.getScene().getWindow();
        VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
            stage, modelPath, this::performRAGSearch
        );
        dialog.show();
    }
}
```

---

## 📊 Comparaison des Solutions

### Vosk (Recommandé pour Desktop)
```
Avantages:
✅ Offline (pas d'internet)
✅ Gratuit (pas d'API key)
✅ Rapide (temps réel)
✅ Multilingue (20+ langues)
✅ Léger (39-66 MB)
✅ Pure JavaFX (pas de WebView)
✅ Privacy (données locales)

Inconvénients:
❌ Nécessite téléchargement modèles
❌ Précision moyenne (vs cloud)
❌ Taille modèles (39-66 MB)
```

### Web Speech API (WebView)
```
Avantages:
✅ Très précis
✅ Pas de modèles à télécharger
✅ Support navigateur

Inconvénients:
❌ Nécessite internet
❌ Nécessite WebView
❌ Dépend du navigateur
❌ Privacy concerns
```

### OpenAI Whisper API
```
Avantages:
✅ Très précis
✅ Multilingue excellent

Inconvénients:
❌ Nécessite internet
❌ Payant (API key)
❌ Latence réseau
❌ Privacy concerns
```

---

## 🔧 Dépannage

### Erreur: Model not found
```
Solution: Vérifier que le dossier models/ existe et contient les modèles
```

### Erreur: Microphone not supported
```
Solution: Vérifier que le microphone est connecté et autorisé
```

### Erreur: LineUnavailableException
```
Solution: Fermer les autres applications utilisant le microphone
```

### Transcription vide
```
Solution: 
1. Parler plus fort
2. Vérifier le niveau du microphone
3. Réduire le bruit ambiant
```

---

## 📚 Ressources

- **Site Vosk**: https://alphacephei.com/vosk/
- **Modèles**: https://alphacephei.com/vosk/models
- **Documentation**: https://alphacephei.com/vosk/install
- **GitHub**: https://github.com/alphacep/vosk-api

---

## ✅ Checklist d'Intégration

- [ ] Ajouter dépendance Vosk dans pom.xml
- [ ] Télécharger modèle français (39 MB)
- [ ] Créer dossier models/
- [ ] Extraire le modèle
- [ ] Ajouter bouton dans FXML
- [ ] Ajouter méthode openVoskVoiceSearch()
- [ ] Ajouter méthode performRAGSearch()
- [ ] Rebuild le projet
- [ ] Tester avec microphone

---

**Vosk est la meilleure solution pour une application desktop JavaFX!** 🎤✨

**Offline, Gratuit, Rapide, et Pure Java!**
