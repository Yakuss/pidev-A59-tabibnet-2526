# TabibNet - Guide de Démarrage

## ✅ État du Projet
Le projet a été testé et fonctionne correctement !

## 📋 Prérequis

### 1. Java Development Kit (JDK) 17
- Le projet nécessite Java 17 ou supérieur
- Vérifiez votre version : `java -version`

### 2. Base de données MySQL
- XAMPP ou MySQL Server doit être installé et en cours d'exécution
- Base de données : `tabibnet`
- Utilisateur : `root`
- Mot de passe : (vide)
- Port : `3306`

### 3. Maven
- Maven est déjà configuré dans le projet
- Chemin : `c:\Users\user\OneDrive\Desktop\TP_3A\pidev-A59-tabibnet-2526-updateferiel\apache-maven-3.9.6`

## 🚀 Démarrage Rapide

### Option 1 : Utiliser les scripts batch (Recommandé)

#### Lancer l'application
```batch
run.bat
```
Ce script va :
1. Compiler le projet
2. Lancer l'application JavaFX

#### Compiler uniquement
```batch
compile.bat
```

### Option 2 : Ligne de commande Maven

#### Compiler le projet
```batch
"c:\Users\user\OneDrive\Desktop\TP_3A\pidev-A59-tabibnet-2526-updateferiel\apache-maven-3.9.6\bin\mvn.cmd" clean compile
```

#### Lancer l'application
```batch
"c:\Users\user\OneDrive\Desktop\TP_3A\pidev-A59-tabibnet-2526-updateferiel\apache-maven-3.9.6\bin\mvn.cmd" javafx:run
```

## 🗄️ Configuration de la Base de Données

### Étape 1 : Démarrer MySQL
1. Ouvrez XAMPP Control Panel
2. Démarrez le service MySQL

### Étape 2 : Créer la base de données
```sql
CREATE DATABASE IF NOT EXISTS tabibnet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tabibnet;
```

### Étape 3 : Créer les tables
Exécutez les scripts SQL dans cet ordre :

1. **Tables de base** (patients, medecins, specialite, etc.)
2. **create_question_table.sql** - Tables pour le forum
3. **magazine_article_schema.sql** - Tables pour les magazines et articles

### Étape 4 : Données de test (optionnel)
Exécutez **create_users.sql** pour créer des utilisateurs de test.

## 👤 Comptes par Défaut

### Administrateur
- Email : `admin@gmail.com`
- Mot de passe : `admin123`
- Créé automatiquement au démarrage

### Médecin (si create_users.sql exécuté)
- Email : `medecin2@test.com`
- Mot de passe : (voir le script SQL)

### Patient (si create_users.sql exécuté)
- Email : `patient2@test.com`
- Mot de passe : (voir le script SQL)

## 📁 Structure du Projet

```
pijava--main/
├── src/main/java/com/pidev/
│   ├── controllers/     # Contrôleurs JavaFX
│   ├── models/          # Modèles de données
│   ├── services/        # Logique métier
│   ├── utils/           # Utilitaires (DB, Email, etc.)
│   └── MainApp.java     # Point d'entrée
├── src/main/resources/
│   └── views/           # Fichiers FXML
├── models/              # Modèles Vosk pour reconnaissance vocale
├── resume-api/          # API Python pour analyse de CV
├── pom.xml              # Configuration Maven
├── run.bat              # Script de lancement
└── compile.bat          # Script de compilation
```

## 🔧 Fonctionnalités

- ✅ Authentification (Admin, Médecin, Patient)
- ✅ Gestion des rendez-vous
- ✅ Annuaire des médecins
- ✅ Forum médical avec IA
- ✅ Magazine et articles
- ✅ Recherche vocale (Vosk)
- ✅ Analyse de sentiment
- ✅ Gamification du forum

## 🐛 Résolution des Problèmes

### Erreur de connexion à la base de données
- Vérifiez que MySQL est démarré dans XAMPP
- Vérifiez que la base de données `tabibnet` existe
- Vérifiez les identifiants dans `DatabaseConnection.java`

### Erreur de compilation
- Vérifiez que Java 17 est installé
- Nettoyez le projet : `mvn clean`
- Supprimez le dossier `target/` et recompilez

### L'application ne démarre pas
- Vérifiez les logs dans la console
- Assurez-vous que JavaFX est correctement configuré
- Vérifiez que tous les fichiers FXML existent dans `src/main/resources/views/`

## 📞 Support

Pour toute question ou problème, consultez :
- Les fichiers de documentation dans le projet
- Les logs de compilation et d'exécution
- Le code source dans `src/main/java/com/pidev/`

## ✨ Statut de Compilation

**Dernière compilation : ✅ SUCCÈS**
- Date : 2026-05-06
- Temps : 4.899 s
- Fichiers compilés : 79 fichiers source
- Avertissements : Modules automatiques détectés (normal)

---

**Projet prêt à l'emploi ! 🎉**
