# 🚀 API Annuaire - Référence Rapide

## 📍 Base URL
```
http://localhost:5000
```

---

## 🔥 Endpoints Principaux

### 1. Status API
```http
GET /
```
**Réponse:**
```json
{
  "message": "API is running",
  "data_loaded": true,
  "record_count": 15234
}
```

---

### 2. Recherche avec Pagination
```http
POST /search/doctorsList?page=1&size=20
Content-Type: application/json

{
  "name": "Ahmed",
  "specialty": "Cardiologie",
  "governorate": "Tunis"
}
```

**Réponse:**
```json
{
  "doctors": [
    {
      "Nom & Prénom": "Dr. Ahmed Ben Ali",
      "Spécialité": "Cardiologie",
      "Governorate": "Tunis",
      "Adresse Professionnelle": "Avenue Habib Bourguiba",
      "Téléphone": "71 123 456",
      "Mode Exercice": "Libre Pratique"
    }
  ],
  "currentPage": 1,
  "pageSize": 20,
  "totalItems": 156,
  "totalPages": 8
}
```

---

### 3. Vérifier Existence
```http
POST /search/doctors
Content-Type: application/json

{
  "name": "Ahmed Ben Ali",
  "specialty": "Cardiologie",
  "governorate": "Tunis"
}
```

**Réponse:**
```json
{
  "result": true
}
```

---

## 💻 Code Java

### Vérifier Status
```java
DoctorAPIService api = new DoctorAPIService();
JSONObject status = api.getAPIStatus();
boolean dataLoaded = status.getBoolean("data_loaded");
```

### Rechercher
```java
JSONObject response = api.searchDoctors("Ahmed", "Cardiologie", "Tunis", 1, 20);
List<DoctorAPI> doctors = api.parseDoctorsFromResponse(response);
```

### Vérifier Existence
```java
boolean exists = api.checkDoctorExists("Ahmed Ben Ali", "Cardiologie", "Tunis");
```

---

## 🗺️ Gouvernorats
Tunis, Ariana, Ben Arous, Manouba, Nabeul, Zaghouan, Bizerte, Béja, Jendouba, Kef, Siliana, Sousse, Monastir, Mahdia, Sfax, Kairouan, Kasserine, Sidi Bouzid, Gabès, Médenine, Tataouine, Gafsa, Tozeur, Kébili

---

## 🏥 Spécialités
Cardiologie, Dermatologie, Gastro-entérologie, Gynécologie, Médecine dentaire, Médecine générale, Neurologie, Ophtalmologie, Orthopédie - Traumatologie, ORL, Pédiatrie, Pneumologie, Psychiatrie, Radiologie, Urologie, Anesthésie-Réanimation, Chirurgie générale, Endocrinologie, Néphrologie, Rhumatologie

---

## ⚙️ Configuration

### Timeout
```java
private static final int TIMEOUT = 10000; // 10 secondes
```

### Pagination
- Défaut: 20 résultats/page
- Maximum: 100 résultats/page

---

## 🚀 Démarrer l'API

```bash
cd flask-api
python app.py
```

L'API démarre sur `http://localhost:5000`

---

## 📚 Documentation Complète

Voir **ANNUAIRE_API_DOCUMENTATION.md** pour plus de détails!
