# 🚀 Guide de Démarrage Rapide - TabibNet

## ⚡ Démarrage en 3 étapes

### Étape 1️⃣ : Démarrer MySQL
1. Ouvrez **XAMPP Control Panel**
2. Cliquez sur **Start** pour MySQL
3. Attendez que le statut devienne vert

### Étape 2️⃣ : Initialiser la base de données
Double-cliquez sur : **`init_db.bat`**

Ce script va :
- Créer la base de données `tabibnet`
- Créer toutes les tables nécessaires
- Insérer les spécialités médicales

### Étape 3️⃣ : Lancer l'application
Double-cliquez sur : **`run.bat`**

Ce script va :
- Compiler le projet
- Lancer l'application JavaFX

---

## 🎯 C'est tout !

L'application devrait maintenant s'ouvrir avec l'écran de connexion.

### 👤 Compte Administrateur par défaut
- **Email** : `admin@gmail.com`
- **Mot de passe** : `admin123`

---

## ❓ Problèmes courants

### ❌ "MySQL non trouvé"
- Vérifiez que XAMPP est installé
- Modifiez le chemin dans `init_db.bat` si nécessaire

### ❌ "Erreur de connexion BD"
- Assurez-vous que MySQL est démarré dans XAMPP
- Vérifiez que le port 3306 n'est pas utilisé par un autre programme

### ❌ "Erreur de compilation"
- Vérifiez que Java 17 est installé : `java -version`
- Téléchargez Java 17 si nécessaire : https://adoptium.net/

---

## 📂 Scripts disponibles

| Script | Description |
|--------|-------------|
| `run.bat` | Compile et lance l'application |
| `compile.bat` | Compile uniquement le projet |
| `init_db.bat` | Initialise la base de données |

---

## 📚 Documentation complète

Pour plus de détails, consultez : **`README_DEMARRAGE.md`**

---

## ✅ Statut du Projet

**Le projet fonctionne correctement !** ✨

- ✅ Compilation réussie
- ✅ Connexion à la base de données OK
- ✅ Application démarre correctement
- ✅ Compte admin créé automatiquement

---

**Bon développement ! 🎉**
