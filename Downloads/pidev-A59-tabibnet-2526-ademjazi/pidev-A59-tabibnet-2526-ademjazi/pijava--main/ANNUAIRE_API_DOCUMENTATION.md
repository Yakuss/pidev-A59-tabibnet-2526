# 📖 Documentation API Annuaire National des Médecins

## 🌐 Informations Générales

### Base URL
```
http://localhost:5000
```

### Timeout
```
10 secondes (10000 ms)
```

### Content-Type
```
application/json
```

---

## 📋 Endpoints Disponibles

### 1. **GET /** - Status de l'API

Vérifie si l'API est en ligne et si les données sont chargées.

#### Request
```http
GET http://localhost:5000/
```

#### Response (200 OK)
```json
{
  "message": "API is running",
  "data_loaded": true,
  "record_count": 15234
}
```

#### Champs de Réponse
- `message` (string): Message de statut
- `data_loaded` (boolean): Indique si les données sont chargées
- `record_count` (int): Nombre total de médecins dans la base

---

### 2. **POST /search/doctorsList** - Recherche avec Pagination

Recherche des médecins avec filtres et pagination.

#### Request
```http
POST http://localhost:5000/search/doctorsList?page=1&size=20
Content-Type: application/json

{
  "name": "Ahmed",
  "specialty": "Cardiologie",
  "governorate": "Tunis"
}
```

#### Query Parameters
- `page` (int, optional): Numéro de page (défaut: 1)
- `size` (int, optional): Taille de page (défaut: 20, max: 100)

#### Request Body (tous optionnels)
```json
{
  "name": "string",        // Recherche fuzzy sur le nom
  "specialty": "string",   // Filtre par spécialité
  "governorate": "string"  // Filtre par gouvernorat
}
```

#### Response (200 OK)
```json
{
  "doctors": [
    {
      "Nom & Prénom": "Dr. Ahmed Ben Ali",
      "Spécialité": "Cardiologie",
      "Governorate": "Tunis",
      "Adresse Professionnelle": "Avenue Habib Bourguiba, Tunis",
      "Téléphone": "71 123 456",
      "Mode Exercice": "Libre Pratique",
      "email": "ahmed.benali@example.com"
    },
    {
      "Nom & Prénom": "Dr. Fatma Trabelsi",
      "Spécialité": "Dermatologie",
      "Governorate": "Ariana",
      "Adresse Professionnelle": "Rue de la République, Ariana",
      "Téléphone": "71 234 567",
      "Mode Exercice": "Libre Pratique"
    }
  ],
  "currentPage": 1,
  "pageSize": 20,
  "totalItems": 156,
  "totalPages": 8
}
```

#### Champs de Réponse

**Pagination:**
- `currentPage` (int): Page actuelle
- `pageSize` (int): Nombre d'éléments par page
- `totalItems` (int): Nombre total de résultats
- `totalPages` (int): Nombre total de pages

**Doctors Array:**
- `Nom & Prénom` (string): Nom complet du médecin
- `Spécialité` (string): Spécialité médicale
- `Governorate` (string): Gouvernorat
- `Adresse Professionnelle` (string): Adresse du cabinet
- `Téléphone` (string): Numéro de téléphone
- `Mode Exercice` (string): Mode d'exercice (généralement "Libre Pratique")
- `email` (string, optional): Email du médecin

---

### 3. **POST /search/doctors** - Vérification d'Existence

Vérifie si un médecin existe dans la base de données.

#### Request
```http
POST http://localhost:5000/search/doctors
Content-Type: application/json

{
  "name": "Ahmed Ben Ali",
  "specialty": "Cardiologie",
  "governorate": "Tunis"
}
```

#### Request Body (tous optionnels)
```json
{
  "name": "string",        // Nom du médecin
  "specialty": "string",   // Spécialité
  "governorate": "string"  // Gouvernorat
}
```

#### Response (200 OK)
```json
{
  "result": true
}
```

#### Champs de Réponse
- `result` (boolean): `true` si le médecin existe, `false` sinon

---

## 🔍 Exemples d'Utilisation

### Exemple 1: Vérifier le Statut de l'API

```bash
curl http://localhost:5000/
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

### Exemple 2: Rechercher Tous les Médecins (Page 1)

```bash
curl -X POST http://localhost:5000/search/doctorsList?page=1&size=20 \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

### Exemple 3: Rechercher par Nom

```bash
curl -X POST http://localhost:5000/search/doctorsList?page=1&size=20 \
  -H "Content-Type: application/json" \
  -d '{"name": "Ahmed"}'
```

---

### Exemple 4: Rechercher par Spécialité

```bash
curl -X POST http://localhost:5000/search/doctorsList?page=1&size=20 \
  -H "Content-Type: application/json" \
  -d '{"specialty": "Cardiologie"}'
```

---

### Exemple 5: Rechercher par Gouvernorat

```bash
curl -X POST http://localhost:5000/search/doctorsList?page=1&size=20 \
  -H "Content-Type: application/json" \
  -d '{"governorate": "Tunis"}'
```

---

### Exemple 6: Recherche Combinée

```bash
curl -X POST http://localhost:5000/search/doctorsList?page=1&size=20 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmed",
    "specialty": "Cardiologie",
    "governorate": "Tunis"
  }'
```

---

### Exemple 7: Vérifier si un Médecin Existe

```bash
curl -X POST http://localhost:5000/search/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmed Ben Ali",
    "specialty": "Cardiologie",
    "governorate": "Tunis"
  }'
```

**Réponse:**
```json
{
  "result": true
}
```

---

## 📊 Spécialités Disponibles

Liste des spécialités médicales supportées:

1. Cardiologie
2. Dermatologie
3. Gastro-entérologie
4. Gynécologie
5. Médecine dentaire
6. Médecine générale
7. Neurologie
8. Ophtalmologie
9. Orthopédie - Traumatologie
10. Oto-rhino-laryngologie (ORL)
11. Pédiatrie
12. Pneumologie
13. Psychiatrie
14. Radiologie
15. Urologie
16. Anesthésie-Réanimation
17. Chirurgie générale
18. Endocrinologie
19. Néphrologie
20. Rhumatologie

---

## 🗺️ Gouvernorats Disponibles

Liste des gouvernorats tunisiens:

1. Tunis
2. Ariana
3. Ben Arous
4. Manouba
5. Nabeul
6. Zaghouan
7. Bizerte
8. Béja
9. Jendouba
10. Kef
11. Siliana
12. Sousse
13. Monastir
14. Mahdia
15. Sfax
16. Kairouan
17. Kasserine
18. Sidi Bouzid
19. Gabès
20. Médenine
21. Tataouine
22. Gafsa
23. Tozeur
24. Kébili

---

## 🔧 Utilisation dans le Code Java

### 1. Vérifier le Statut de l'API

```java
DoctorAPIService apiService = new DoctorAPIService();

try {
    JSONObject status = apiService.getAPIStatus();
    String message = status.optString("message", "Unknown");
    boolean dataLoaded = status.optBoolean("data_loaded", false);
    int recordCount = status.optInt("record_count", 0);
    
    System.out.println("API Status: " + message);
    System.out.println("Data Loaded: " + dataLoaded);
    System.out.println("Record Count: " + recordCount);
} catch (Exception e) {
    System.err.println("API Error: " + e.getMessage());
}
```

---

### 2. Rechercher des Médecins

```java
DoctorAPIService apiService = new DoctorAPIService();

try {
    // Recherche avec filtres
    String name = "Ahmed";
    String specialty = "Cardiologie";
    String governorate = "Tunis";
    int page = 1;
    int size = 20;
    
    JSONObject response = apiService.searchDoctors(name, specialty, governorate, page, size);
    
    // Parser les résultats
    List<DoctorAPI> doctors = apiService.parseDoctorsFromResponse(response);
    
    // Obtenir les infos de pagination
    DoctorAPIService.PaginationInfo pagination = apiService.getPaginationInfo(response);
    
    System.out.println("Found " + doctors.size() + " doctors");
    System.out.println("Page " + pagination.currentPage + "/" + pagination.totalPages);
    
    // Afficher les médecins
    for (DoctorAPI doctor : doctors) {
        System.out.println("Dr. " + doctor.getName());
        System.out.println("  Specialty: " + doctor.getSpecialty());
        System.out.println("  Location: " + doctor.getGovernorate());
        System.out.println("  Phone: " + doctor.getPhone());
        System.out.println();
    }
} catch (Exception e) {
    System.err.println("Search Error: " + e.getMessage());
}
```

---

### 3. Vérifier si un Médecin Existe

```java
DoctorAPIService apiService = new DoctorAPIService();

try {
    String name = "Ahmed Ben Ali";
    String specialty = "Cardiologie";
    String governorate = "Tunis";
    
    boolean exists = apiService.checkDoctorExists(name, specialty, governorate);
    
    if (exists) {
        System.out.println("✅ Doctor found in national directory");
    } else {
        System.out.println("❌ Doctor not found");
    }
} catch (Exception e) {
    System.err.println("Verification Error: " + e.getMessage());
}
```

---

## 🚀 Démarrage de l'API Flask

### Prérequis
```bash
pip install flask flask-cors pandas
```

### Structure du Projet Flask
```
flask-api/
├── app.py
├── data/
│   └── doctors.csv
└── requirements.txt
```

### Démarrer le Serveur
```bash
cd flask-api
python app.py
```

Le serveur démarre sur `http://localhost:5000`

---

## ⚠️ Gestion des Erreurs

### Erreurs Courantes

#### 1. API Non Disponible
```
❌ API non disponible (localhost:5000)
```
**Solution:** Démarrer le serveur Flask

#### 2. Timeout
```
java.net.SocketTimeoutException: Read timed out
```
**Solution:** Vérifier que le serveur répond rapidement

#### 3. Données Non Chargées
```
⚠️ API connectée mais données non chargées
```
**Solution:** Vérifier que le fichier CSV est présent dans `data/doctors.csv`

#### 4. Format de Réponse Invalide
```
org.json.JSONException: ...
```
**Solution:** Vérifier que l'API retourne du JSON valide

---

## 📝 Notes Importantes

1. **Recherche Fuzzy**: La recherche par nom utilise une correspondance floue (fuzzy matching)
2. **Pagination**: Maximum 100 résultats par page
3. **Timeout**: Les requêtes ont un timeout de 10 secondes
4. **Encodage**: Utilise UTF-8 pour supporter les caractères arabes et français
5. **Mode Exercice**: Actuellement, seul "Libre Pratique" est supporté

---

## 🔗 Fichiers Liés

### Services
- `DoctorAPIService.java` - Service pour communiquer avec l'API
- `MedecinService.java` - Service pour la base de données locale

### Controllers
- `AnnuaireController.java` - Controller pour l'annuaire national
- `MedecinController.java` - Controller pour les médecins locaux

### Models
- `DoctorAPI.java` - Modèle pour les médecins de l'API
- `Medecin.java` - Modèle pour les médecins locaux

### Views
- `AnnuaireView.fxml` - Interface de l'annuaire national
- `MedecinDirectoryView.fxml` - Interface de l'annuaire local

---

## 📊 Statistiques

- **Base URL**: `http://localhost:5000`
- **Endpoints**: 3
- **Timeout**: 10 secondes
- **Pagination**: Oui (max 100/page)
- **Recherche Fuzzy**: Oui
- **Filtres**: Nom, Spécialité, Gouvernorat
- **Format**: JSON
- **Encodage**: UTF-8

---

**Date**: 29 avril 2026  
**Version**: 1.0  
**Status**: ✅ Opérationnel
