# 🚀 Vosk - Démarrage Rapide

## ✅ Pourquoi Vosk?

**Offline + Gratuit + Pure JavaFX = Parfait pour Desktop!**

---

## 📦 Installation (3 étapes)

### 1. Ajouter dans pom.xml

```xml
<dependency>
    <groupId>com.alphacephei</groupId>
    <artifactId>vosk</artifactId>
    <version>0.3.45</version>
</dependency>
```

### 2. Télécharger le Modèle Français

```bash
cd pijava--main
mkdir models
cd models

# Télécharger (39 MB)
wget https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip

# Extraire
unzip vosk-model-small-fr-0.22.zip
```

**Ou télécharge manuellement:**  
https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip

### 3. Rebuild

```
Build → Rebuild Project
```

---

## 💻 Utilisation (2 lignes de code)

### Dans AnnuaireController.java

```java
@FXML
public void openVoskVoiceSearch() {
    Stage stage = (Stage) searchNameField.getScene().getWindow();
    VoskVoiceSearchDialog dialog = new VoskVoiceSearchDialog(
        stage, 
        "models/vosk-model-small-fr-0.22", 
        this::performRAGSearch
    );
    dialog.show();
}
```

---

## 🎤 Test

1. **Run** l'application
2. **Cliquer** sur "🎤 Recherche Vocale"
3. **Parler**: "Je cherche un cardiologue à Tunis"
4. **Voir** les résultats!

---

## 📁 Structure

```
pijava--main/
├── models/
│   └── vosk-model-small-fr-0.22/  ← Modèle ici
├── src/
│   └── main/
│       └── java/
│           └── com/pidev/
│               ├── services/
│               │   └── VoskSpeechService.java  ← Créé
│               └── controllers/
│                   └── VoskVoiceSearchDialog.java  ← Créé
└── pom.xml  ← Ajouter dépendance
```

---

## 🌍 Autres Langues

### Arabe (66 MB)
```
https://alphacephei.com/vosk/models/vosk-model-ar-0.22-linto-1.1.0.zip
```

### Anglais (40 MB)
```
https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
```

---

## ✨ Avantages

✅ **Offline** - Pas d'internet  
✅ **Gratuit** - Pas d'API key  
✅ **Rapide** - Temps réel  
✅ **Léger** - 39 MB  
✅ **Pure JavaFX** - Pas de WebView  

---

**C'est tout! Simple et efficace!** 🎉

Voir **VOSK_INTEGRATION_GUIDE.md** pour plus de détails.
