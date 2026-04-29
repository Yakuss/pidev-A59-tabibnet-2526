# 🎤 Vosk Voice Search - Quick Reference

## 🚀 Quick Start (3 Steps)

### 1️⃣ Reload Maven
```bash
cd pijava--main
mvn clean install
```

### 2️⃣ Download Vosk Model (39 MB)
```bash
mkdir models
cd models
# Download from: https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip
# Extract to: models/vosk-model-small-fr-0.22/
```

### 3️⃣ Run & Test
```bash
# 1. Start Flask API
python Flask_of_AT.py

# 2. Run JavaFX App
# 3. Click "🎤 Recherche Vocale"
# 4. Speak: "Je cherche un cardiologue à Tunis"
# 5. Click "🔍 Rechercher"
```

---

## 📁 Required Structure

```
pijava--main/
├── models/
│   └── vosk-model-small-fr-0.22/    ← Download this!
│       ├── am/
│       ├── conf/
│       ├── graph/
│       └── ivector/
├── src/
│   └── main/
│       └── java/
│           └── com/pidev/
│               ├── controllers/
│               │   ├── AnnuaireController.java ✅
│               │   └── VoskVoiceSearchDialog.java ✅
│               └── services/
│                   ├── VoskSpeechService.java ✅
│                   └── RAGSearchService.java ✅
└── pom.xml ✅
```

---

## 🔗 Download Links

| Language | Model | Size | Link |
|----------|-------|------|------|
| 🇫🇷 French | vosk-model-small-fr-0.22 | 39 MB | [Download](https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip) |
| 🇸🇦 Arabic | vosk-model-ar-0.22-linto | 66 MB | [Download](https://alphacephei.com/vosk/models/vosk-model-ar-0.22-linto-1.1.0.zip) |
| 🇺🇸 English | vosk-model-small-en-us | 40 MB | [Download](https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip) |

---

## 🎯 Example Queries

### French 🇫🇷
```
"Je cherche un cardiologue à Tunis"
"Trouvez-moi un dentiste à Sfax"
"J'ai besoin d'un pédiatre à Ariana"
```

### Arabic 🇸🇦
```
"أريد طبيب قلب في تونس"
"أبحث عن طبيب أسنان في صفاقس"
"أحتاج طبيب أطفال في أريانة"
```

### English 🇺🇸
```
"I need a cardiologist in Tunis"
"Find me a dentist in Sfax"
"Looking for a pediatrician in Ariana"
```

---

## 🔧 Troubleshooting

| Error | Solution |
|-------|----------|
| ❌ Model not found | Check `models/vosk-model-small-fr-0.22/` exists |
| ❌ Microphone not supported | Connect microphone, grant permissions |
| ❌ API non disponible | Start Flask API: `python Flask_of_AT.py` |
| ❌ Empty transcription | Speak louder, reduce noise |

---

## ✅ Checklist

- [ ] Maven reloaded (pom.xml updated)
- [ ] Vosk model downloaded (39 MB)
- [ ] Model extracted to `models/vosk-model-small-fr-0.22/`
- [ ] Flask API running on port 5000
- [ ] Project rebuilt
- [ ] App running
- [ ] Voice search button clicked
- [ ] Microphone working
- [ ] Results displayed

---

## 🎨 How It Works

```
User clicks "🎤 Recherche Vocale"
         ↓
VoskVoiceSearchDialog opens
         ↓
User clicks microphone 🎤
         ↓
Vosk starts listening (real-time)
         ↓
User speaks: "Je cherche un cardiologue à Tunis"
         ↓
Text appears in transcript area
         ↓
User clicks "🔍 Rechercher"
         ↓
RAGSearchService sends query to /query endpoint
         ↓
AI analyzes and searches database
         ↓
Results displayed with AI response
```

---

## 📊 Why Vosk?

✅ **Offline** - No internet needed  
✅ **Free** - No API key required  
✅ **Fast** - Real-time recognition  
✅ **Multilingual** - 20+ languages  
✅ **Lightweight** - 39-66 MB models  
✅ **Pure JavaFX** - No WebView  
✅ **Privacy** - All data local  

---

## 🌐 API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/query` | POST | Natural language AI search |
| `/health` | GET | Check RAG service status |
| `/stats` | GET | Database statistics |

---

## 📚 Documentation

- **Full Guide (Arabic):** `VOSK_SETUP_GUIDE_AR.md`
- **Integration Guide:** `VOSK_INTEGRATION_GUIDE.md`
- **Vosk Website:** https://alphacephei.com/vosk/
- **Models:** https://alphacephei.com/vosk/models

---

**Ready to use! Just download the model and run!** 🎤✨
