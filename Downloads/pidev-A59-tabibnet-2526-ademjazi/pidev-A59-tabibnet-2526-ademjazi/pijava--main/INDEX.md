# 📚 TabibNet - Index de Documentation

## 🚀 Démarrage Rapide

### Pour commencer immédiatement
1. **[GUIDE_RAPIDE.md](GUIDE_RAPIDE.md)** - Démarrage en 3 étapes ⚡
   - Démarrer MySQL
   - Initialiser la base de données
   - Lancer l'application

### Pour une installation complète
2. **[README_DEMARRAGE.md](README_DEMARRAGE.md)** - Documentation complète 📖
   - Prérequis détaillés
   - Configuration de la base de données
   - Résolution des problèmes
   - Structure du projet

## 🔧 Scripts Disponibles

### Scripts de Lancement
- **`run.bat`** - Compile et lance l'application
- **`compile.bat`** - Compile uniquement le projet
- **`check_setup.bat`** - Vérifie la configuration complète

### Scripts de Base de Données
- **`init_db.bat`** - Initialise la base de données automatiquement
- **`init_database.sql`** - Script SQL d'initialisation

## 📋 Documentation Technique

### Analyse et Résolution
3. **[PROBLEMES_RESOLUS.md](PROBLEMES_RESOLUS.md)** - Rapport complet 🔍
   - Problèmes identifiés
   - Solutions implémentées
   - Tests effectués
   - Structure de la base de données
   - Fonctionnalités vérifiées

### Configuration Maven
4. **[pom.xml](pom.xml)** - Configuration du projet
   - Dépendances
   - Plugins
   - Configuration de compilation

### Configuration Java
5. **[module-info.java](src/main/java/module-info.java)** - Configuration du module
   - Modules requis
   - Exports
   - Opens pour JavaFX

## 🗄️ Base de Données

### Scripts SQL
- **[init_database.sql](init_database.sql)** - Schéma complet
  - 10 tables
  - Relations (Foreign Keys)
  - Données initiales (spécialités)

- **[create_question_table.sql](create_question_table.sql)** - Tables du forum
- **[magazine_article_schema.sql](magazine_article_schema.sql)** - Tables magazine

### Tables Principales
1. **patients** - Utilisateurs patients
2. **medecins** - Utilisateurs médecins
3. **admins** - Administrateurs
4. **appointments** - Rendez-vous
5. **question** - Questions du forum
6. **reponse** - Réponses du forum
7. **magazine** - Magazines médicaux
8. **article** - Articles
9. **specialite** - Spécialités médicales
10. **feedback** - Évaluations

## 📁 Structure du Code Source

### Packages Principaux
```
src/main/java/com/pidev/
├── controllers/     # Contrôleurs JavaFX (29 fichiers)
├── models/          # Modèles de données (13 fichiers)
├── services/        # Logique métier (20 fichiers)
├── utils/           # Utilitaires (13 fichiers)
├── constant/        # Constantes (2 fichiers)
└── MainApp.java     # Point d'entrée
```

### Ressources
```
src/main/resources/
└── views/           # Fichiers FXML (33 fichiers)
```

## 🎯 Fonctionnalités

### Authentification
- Login multi-rôles (Admin, Médecin, Patient)
- Récupération de mot de passe
- Gestion de session

### Gestion des Rendez-vous
- Prise de rendez-vous
- Calendrier
- Notifications

### Forum Médical
- Questions/Réponses
- Système de likes
- Gamification
- Analyse de sentiment (IA)

### Annuaire des Médecins
- Recherche par spécialité
- Recherche par gouvernorat
- Recherche vocale (Vosk)

### Magazine
- Gestion des magazines
- Gestion des articles
- Upload d'images et PDFs

### Intelligence Artificielle
- Analyse de sentiment (Gemini AI)
- Reconnaissance vocale (Vosk)
- Recherche RAG

## 🔐 Comptes par Défaut

### Administrateur
- **Email** : `admin@gmail.com`
- **Mot de passe** : `admin123`
- Créé automatiquement au démarrage

## 🛠️ Technologies Utilisées

### Frontend
- JavaFX 17.0.11
- FXML pour les interfaces

### Backend
- Java 17
- Maven 3.9.6

### Base de Données
- MySQL 8.0
- JDBC

### Intelligence Artificielle
- Vosk (Reconnaissance vocale)
- Gemini AI (Analyse de sentiment)

### Communication
- Jakarta Mail (Emails)
- JSON (API)

## 📊 Statut du Projet

### ✅ Tests Réussis
- Compilation : ✅ (4.899s, 79 fichiers)
- Connexion BD : ✅
- Initialisation Admin : ✅
- Lancement Application : ✅

### ⚠️ Avertissements (Non Critiques)
- Modules automatiques détectés (mysql-connector-j, vosk)
- Impact : Aucun - Le projet fonctionne normalement

## 🚦 Démarrage Étape par Étape

### 1️⃣ Vérification de la Configuration
```batch
check_setup.bat
```
Ce script vérifie :
- Java installé
- Maven disponible
- MySQL installé
- Base de données créée
- Fichiers du projet présents

### 2️⃣ Initialisation de la Base de Données
```batch
init_db.bat
```
Crée la base de données et toutes les tables.

### 3️⃣ Lancement de l'Application
```batch
run.bat
```
Compile et lance l'application.

## 📞 Support et Aide

### En cas de problème
1. Consultez **[PROBLEMES_RESOLUS.md](PROBLEMES_RESOLUS.md)**
2. Exécutez **`check_setup.bat`** pour diagnostiquer
3. Vérifiez les logs dans la console

### Logs Disponibles
- Console Maven : Erreurs de compilation
- Console Application : Erreurs d'exécution
- XAMPP/MySQL : Logs de la base de données

## 📝 Documentation Additionnelle

### Guides Spécifiques
- **AI_SCORING_QUICK_START.md** - Guide du scoring IA
- **AI_SENTIMENT_SCORING_GUIDE.md** - Analyse de sentiment
- **ANNUAIRE_API_DOCUMENTATION.md** - API de l'annuaire
- **FORUM_READY_NOW.md** - Documentation du forum
- **VOICE_SEARCH_QUICK_START.md** - Recherche vocale
- **VOSK_QUICK_START.md** - Configuration Vosk

### API Python
- **resume-api/** - API d'analyse de CV
  - `main.py` - Serveur FastAPI
  - `requirements.txt` - Dépendances Python
  - `start.bat` - Script de lancement

## 🎓 Pour les Développeurs

### Commandes Maven Utiles
```batch
# Nettoyer le projet
mvn clean

# Compiler
mvn compile

# Lancer l'application
mvn javafx:run

# Créer un package
mvn package
```

### Structure Recommandée pour Nouveaux Modules
```
com.pidev.nouveaumodule/
├── controllers/     # Contrôleurs JavaFX
├── models/          # Modèles de données
├── services/        # Logique métier
└── views/           # Fichiers FXML (dans resources)
```

## ✨ Conclusion

**Le projet TabibNet est complètement opérationnel et prêt à l'emploi !**

Pour démarrer rapidement, suivez le **[GUIDE_RAPIDE.md](GUIDE_RAPIDE.md)**.

Pour une compréhension complète, consultez le **[README_DEMARRAGE.md](README_DEMARRAGE.md)**.

---

*Dernière mise à jour : 2026-05-06*
*Version : 1.0-SNAPSHOT*

**Bon développement ! 🎉**
