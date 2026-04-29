# 🎤 Recherche Vocale - Démarrage Rapide

## ✅ Ce qui a été créé

### 1. Services Java
- ✅ `SpeechToTextService.java` - Transcription audio (optionnel)
- ✅ `RAGSearchService.java` - Recherche IA en langage naturel

### 2. Interface Web
- ✅ `voice-search.html` - Interface microphone moderne

---

## 🚀 Utilisation Rapide

### Méthode 1: Interface Web (Recommandé)

#### 1. Ouvrir l'Interface
```bash
# Ouvrir dans le navigateur
open src/main/resources/html/voice-search.html
```

#### 2. Utiliser
1. Cliquez sur le microphone 🎤
2. Autorisez l'accès
3. Parlez: "Je cherche un cardiologue à Tunis"
4. Cliquez "Rechercher"

---

### Méthode 2: Code Java Direct

```java
RAGSearchService ragService = new RAGSearchService();

// Recherche en langage naturel
JSONObject response = ragService.query("Je cherche un cardiologue à Tunis", 8);

// Obtenir les résultats
if (ragService.isSuccess(response)) {
    String aiResponse = ragService.getResponseSentence(response);
    List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
    
    System.out.println("IA: " + aiResponse);
    System.out.println("Trouvé: " + doctors.size() + " médecins");
}
```

---

## 🌐 API Endpoints

### POST `/query` - Recherche IA
```json
Request:
{
  "question": "Je cherche un cardiologue à Tunis",
  "top_k": 8
}

Response:
{
  "status": "success",
  "responseSentence": "Voici les cardiologues à Tunis:",
  "doctors": [...],
  "scores": [0.92, 0.87]
}
```

---

## 💡 Exemples

### Français
```
"Je cherche un cardiologue à Tunis"
"Trouvez-moi un pédiatre près de Sfax"
"J'ai besoin d'un dermatologue"
```

### Arabe
```
"أريد طبيب قلب في تونس"
"أبحث عن طبيب أطفال في صفاقس"
```

### Anglais
```
"I need a cardiologist in Tunis"
"Find me a pediatrician near Sfax"
```

---

## ⚙️ Configuration

### 1. Démarrer l'API Flask
```bash
cd flask-api
python app.py
```

### 2. Tester
```bash
# Ouvrir voice-search.html dans Chrome
# OU
# Utiliser RAGSearchService dans Java
```

---

## 📁 Fichiers

1. `SpeechToTextService.java` - Service transcription
2. `RAGSearchService.java` - Service recherche IA
3. `voice-search.html` - Interface web

---

## 🎯 Intégration dans AnnuaireController

```java
@FXML
public void handleVoiceSearch() {
    // Ouvrir l'interface web
    File htmlFile = new File("src/main/resources/html/voice-search.html");
    Desktop.getDesktop().browse(htmlFile.toURI());
}

private void performRAGSearch(String query) {
    RAGSearchService ragService = new RAGSearchService();
    JSONObject response = ragService.query(query, 8);
    
    if (ragService.isSuccess(response)) {
        List<DoctorAPI> doctors = ragService.parseDoctorsFromRAGResponse(response);
        doctorList.setAll(doctors);
    }
}
```

---

## 📚 Documentation

Voir **VOICE_SEARCH_GUIDE_AR.md** pour le guide complet!

---

**Prêt à utiliser!** 🚀
