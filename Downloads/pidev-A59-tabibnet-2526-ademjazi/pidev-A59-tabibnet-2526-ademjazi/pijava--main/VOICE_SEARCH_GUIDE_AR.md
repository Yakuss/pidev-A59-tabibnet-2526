# 🎤 Guide Complet - Recherche Vocale dans l'Annuaire

## 📋 Vue d'Ensemble

Système de recherche vocale (Speech-to-Text) intégré avec l'API RAG pour rechercher des médecins en langage naturel.

---

## 🎯 Fonctionnalités

### 1. Speech-to-Text ✨
- ✅ Reconnaissance vocale en temps réel
- ✅ Support multilingue (Français, Arabe, Anglais)
- ✅ Transcription automatique
- ✅ Interface utilisateur moderne

### 2. Recherche IA (RAG) 🤖
- ✅ Compréhension du langage naturel
- ✅ Recherche sémantique intelligente
- ✅ Réponses contextuelles
- ✅ Support multilingue

### 3. Intégration Complète 🔗
- ✅ Interface HTML/JavaScript pour le microphone
- ✅ Services Java pour l'API
- ✅ Communication avec Flask API
- ✅ Affichage des résultats dans JavaFX

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    UTILISATEUR                          │
│                         ↓                               │
│              🎤 Parle dans le microphone                │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              HTML/JavaScript Interface                  │
│         (voice-search.html - Web Speech API)            │
│                         ↓                               │
│              Transcription en texte                     │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              JavaFX Application                         │
│         (AnnuaireController + RAGSearchService)         │
│                         ↓                               │
│         POST /query avec la question                    │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              Flask API (localhost:5000)                 │
│         • Analyse sémantique (embeddings)               │
│         • Recherche dans la base vectorielle            │
│         • Génération de réponse (GPT-4o)                │
│                         ↓                               │
│         Retourne: doctors + responseSentence            │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              Affichage des Résultats                    │
│         • Liste des médecins trouvés                    │
│         • Réponse IA en langage naturel                 │
│         • Scores de pertinence                          │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Fichiers Créés

### 1. Services Java

#### `SpeechToTextService.java`
Service pour la transcription audio (optionnel, pour traitement serveur)
```java
// Support OpenAI Whisper API
// Support GitHub Models Whisper API
// Transcription de fichiers audio
// Transcription depuis base64
```

#### `RAGSearchService.java`
Service pour la recherche IA via l'endpoint `/query`
```java
// query(question, topK) - Recherche en langage naturel
// getHealth() - Vérifier le statut du service RAG
// getStats() - Obtenir les statistiques
// parseDoctorsFromRAGResponse() - Parser les résultats
```

### 2. Interface HTML

#### `voice-search.html`
Interface web pour la capture vocale
- Bouton microphone animé
- Sélection de langue (FR, AR, EN)
- Transcription en temps réel
- Exemples de questions
- Design moderne et responsive

---

## 🚀 Utilisation

### Option 1: Interface Web (Recommandé)

#### Étape 1: Ouvrir l'Interface
```java
// Dans AnnuaireController.java
@FXML
public void openVoiceSearch() {
    try {
        // Ouvrir dans le navigateur par défaut
        File htmlFile = new File("src/main/resources/html/voice-search.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

#### Étape 2: Parler
1. Cliquez sur le bouton microphone 🎤
2. Autorisez l'accès au microphone
3. Parlez votre question
4. Cliquez sur "Rechercher"

#### Étape 3: Récupérer la Transcription
```javascript
// Dans voice-search.html
searchButton.addEventListener('click', () => {
    const query = transcriptText.textContent.trim();
    
    // Envoyer à l'application Java
    window.parent.postMessage({
        type: 'voice-search',
        query: query,
        language: currentLanguage
    }, '*');
});
```

---

### Option 2: Recherche RAG Directe

#### Code Java
```java
RAGSearchService ragService = new RAGSearchService();

try {
    // Question en langage naturel
    String question = "Je cherche un cardiologue à Tunis";
    int topK = 8; // Nombre de résultats
    
    // Envoyer la requête
    JSONObject response = ragService.query(question, topK);
    
    // Vérifier le statut
    if (ragService.isSuccess(response)) {
        // Obtenir la réponse IA
        String aiResponse = ragService.getResponseSentence(response);
        System.out.println("IA: " + aiResponse);
        
        // Obtenir les médecins
        List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
        System.out.println("Trouvé " + doctors.size() + " médecins");
        
        // Afficher les résultats
        for (DoctorAPI doctor : doctors) {
            System.out.println("- Dr. " + doctor.getName());
            System.out.println("  " + doctor.getSpecialty());
            System.out.println("  " + doctor.getGovernorate());
        }
        
        // Obtenir les scores de pertinence
        List<Double> scores = ragService.getScores(response);
        System.out.println("Scores: " + scores);
        
    } else if (ragService.isGreeting(response)) {
        // C'était juste un salut
        String greeting = ragService.getResponseSentence(response);
        System.out.println(greeting);
        
    } else if (ragService.isInsufficientContext(response)) {
        // Pas assez d'informations
        String message = ragService.getResponseSentence(response);
        System.out.println(message);
    }
    
} catch (Exception e) {
    System.err.println("Erreur: " + e.getMessage());
}
```

---

## 🌐 Endpoints API Utilisés

### 1. POST `/query` - Recherche IA

**Request:**
```json
{
  "question": "Je cherche un cardiologue à Tunis",
  "top_k": 8
}
```

**Response (Success):**
```json
{
  "status": "success",
  "responseSentence": "Selon les informations disponibles, voici les cardiologues à Tunis:",
  "doctors": [
    {
      "fullName": "Dr. Ahmed Ben Ali",
      "specialite": "Cardiology",
      "adresse": "123 Main St, Tunis",
      "telephone": "71 123 456",
      "governorate": "Tunis"
    }
  ],
  "scores": [0.92, 0.87, 0.85],
  "notes": []
}
```

**Response (Greeting):**
```json
{
  "status": "greeting",
  "message": "Bonjour! Je suis votre assistant médical..."
}
```

**Response (No Results):**
```json
{
  "status": "insufficient_context",
  "message": "Je ne trouve pas cette information dans la base de données."
}
```

---

### 2. GET `/health` - Statut RAG

**Response:**
```json
{
  "status": "ok",
  "records": 15234,
  "models": {
    "embed": "mxbai-embed-large",
    "generate": "openai/gpt-4o (GitHub Models)"
  }
}
```

---

### 3. GET `/stats` - Statistiques

**Response:**
```json
{
  "total_records": 15234,
  "by_governorate": {
    "Tunis": 2500,
    "Sfax": 1800,
    "Ariana": 1200
  },
  "top_10_specialties": {
    "Cardiology": 850,
    "Pediatrics": 720,
    "Dermatology": 680
  }
}
```

---

## 💡 Exemples de Questions

### En Français 🇫🇷
```
✅ "Je cherche un cardiologue à Tunis"
✅ "Trouvez-moi un pédiatre près de Sfax"
✅ "J'ai besoin d'un dermatologue"
✅ "Médecin généraliste à Ariana"
✅ "Gynécologue disponible à Sousse"
```

### En Arabe 🇹🇳
```
✅ "أريد طبيب قلب في تونس"
✅ "أبحث عن طبيب أطفال في صفاقس"
✅ "أحتاج طبيب أسنان"
✅ "طبيب عام في أريانة"
```

### En Anglais 🇬🇧
```
✅ "I need a cardiologist in Tunis"
✅ "Find me a pediatrician near Sfax"
✅ "Looking for a dermatologist"
✅ "General practitioner in Ariana"
```

---

## 🎨 Interface Vocale

### Design
```
┌─────────────────────────────────────────┐
│     🎤 Recherche Vocale                 │
│     Parlez pour rechercher un médecin   │
├─────────────────────────────────────────┤
│                                         │
│   🇫🇷 Français  🇹🇳 العربية  🇬🇧 English │
│                                         │
│            ┌─────────┐                  │
│            │    🎤   │  ← Bouton micro  │
│            └─────────┘                  │
│                                         │
│   🎤 Écoute en cours... Parlez maintenant│
│                                         │
│   ┌───────────────────────────────────┐ │
│   │ Transcription:                    │ │
│   │ Je cherche un cardiologue à Tunis │ │
│   └───────────────────────────────────┘ │
│                                         │
│   ┌──────────────┐  ┌────────────────┐ │
│   │ 🔍 Rechercher│  │ 🔄 Effacer     │ │
│   └──────────────┘  └────────────────┘ │
│                                         │
│   💡 Exemples de questions:             │
│   • Je cherche un cardiologue à Tunis   │
│   • أريد طبيب أسنان في صفاقس            │
│   • I need a pediatrician in Ariana     │
└─────────────────────────────────────────┘
```

### Fonctionnalités
- ✅ Bouton microphone animé (pulse pendant l'écoute)
- ✅ Sélection de langue (FR, AR, EN)
- ✅ Transcription en temps réel
- ✅ Affichage des erreurs
- ✅ Exemples de questions
- ✅ Design responsive

---

## 🔧 Configuration

### 1. API Flask
Assurez-vous que l'API Flask tourne sur `localhost:5000`

```bash
cd flask-api
python app.py
```

### 2. Permissions Microphone
Le navigateur demandera l'autorisation d'accès au microphone

### 3. Navigateurs Supportés
- ✅ Google Chrome (recommandé)
- ✅ Microsoft Edge
- ✅ Safari
- ❌ Firefox (support limité)

---

## 🎯 Intégration dans AnnuaireController

### Ajouter un Bouton Vocal

```java
@FXML private Button btnVoiceSearch;

@FXML
public void handleVoiceSearch() {
    try {
        // Option 1: Ouvrir l'interface web
        File htmlFile = new File("src/main/resources/html/voice-search.html");
        if (htmlFile.exists()) {
            Desktop.getDesktop().browse(htmlFile.toURI());
        }
        
        // Option 2: Utiliser WebView dans JavaFX
        Stage stage = new Stage();
        stage.setTitle("Recherche Vocale");
        
        WebView webView = new WebView();
        webView.getEngine().load(htmlFile.toURI().toString());
        
        // Écouter les messages JavaScript
        webView.getEngine().setOnAlert(event -> {
            String query = event.getData();
            performRAGSearch(query);
        });
        
        Scene scene = new Scene(webView, 600, 700);
        stage.setScene(scene);
        stage.show();
        
    } catch (Exception e) {
        showError("Erreur lors de l'ouverture de la recherche vocale: " + e.getMessage());
    }
}

private void performRAGSearch(String query) {
    showLoading(true);
    
    new Thread(() -> {
        try {
            RAGSearchService ragService = new RAGSearchService();
            JSONObject response = ragService.query(query, 8);
            
            Platform.runLater(() -> {
                if (ragService.isSuccess(response)) {
                    String aiResponse = ragService.getResponseSentence(response);
                    List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
                    
                    // Afficher les résultats
                    doctorList.setAll(doctors);
                    doctorListView.setItems(doctorList);
                    
                    // Afficher la réponse IA
                    showAlert(Alert.AlertType.INFORMATION, "Résultats", aiResponse);
                    
                } else if (ragService.isGreeting(response)) {
                    showAlert(Alert.AlertType.INFORMATION, "Assistant", 
                        ragService.getResponseSentence(response));
                        
                } else {
                    showAlert(Alert.AlertType.WARNING, "Aucun résultat", 
                        ragService.getResponseSentence(response));
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

## 📊 Comparaison des Méthodes

### Recherche Classique (CSV)
```
Avantages:
✅ Rapide
✅ Précis
✅ Filtres structurés

Inconvénients:
❌ Requiert des filtres exacts
❌ Pas de compréhension du contexte
❌ Pas de langage naturel
```

### Recherche RAG (IA)
```
Avantages:
✅ Langage naturel
✅ Compréhension du contexte
✅ Multilingue
✅ Recherche sémantique
✅ Réponses intelligentes

Inconvénients:
❌ Plus lent (2-3 secondes)
❌ Nécessite l'API Flask
❌ Consomme plus de ressources
```

### Recherche Vocale
```
Avantages:
✅ Mains libres
✅ Rapide à utiliser
✅ Accessible
✅ Multilingue
✅ Combine avec RAG

Inconvénients:
❌ Nécessite un microphone
❌ Sensible au bruit
❌ Nécessite un navigateur compatible
```

---

## 🚀 Prochaines Étapes

1. **Rebuild** le projet
2. **Démarrer** l'API Flask (`python app.py`)
3. **Tester** l'interface vocale (`voice-search.html`)
4. **Intégrer** dans AnnuaireController
5. **Ajouter** un bouton microphone dans l'UI

---

## 📚 Documentation Complète

- **ANNUAIRE_API_DOCUMENTATION.md** - Documentation API complète
- **API_QUICK_REFERENCE.md** - Référence rapide
- **VOICE_SEARCH_GUIDE_AR.md** - Ce guide

---

**Status**: ✅ Prêt à l'emploi  
**Date**: 29 avril 2026  
**Version**: 1.0
