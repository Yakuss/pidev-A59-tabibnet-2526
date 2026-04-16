# Architecture pour l'Application JavaFX

Ce document décrit l'architecture de la base de données et des entités pour le développement de la nouvelle version JavaFX qui partagera la même base de données que le projet Symfony actuel.

## Couches de l'Application (Architecture Recommandée)
Pour interagir avec la même base de données (`pidev`), nous recommandons l'utilisation de **JDBC** ou d'un ORM comme **Hibernate (JPA)** en Java.

1. **Couche Modèle (Entities)** : Classes Java miroir des tables de la BD.
2. **Couche DAO / Repository** : Interfaces pour les opérations CRUD (accès à la base de données).
3. **Couche Service** : Logique métier.
4. **Couche Contrôleur (JavaFX)** : Contrôleurs pour l'interface graphique (fichiers `.fxml`).

## Correspondance des Types (PHP/Symfony -> Java)
- `string` -> `String`
- `int` -> `int` ou `Integer`
- `float` -> `double` ou `Double`
- `bool` -> `boolean`
- `\DateTime` / `\DateTimeInterface` / `\DateTimeImmutable` -> `java.time.LocalDateTime`
- `Collection` / `array` -> `List<Entity>` ou `Set<Entity>`

## Dictionnaire de Données (Modèles)

### Mots-clés des relations (Hibernate JPA)
- `OneToMany` -> `@OneToMany`
- `ManyToOne` -> `@ManyToOne`

### 1. Utilisateurs (Héritage / Base)
Dans Symfony, Admin, Patient et Medecin héritent généralement de BaseUser. Vous devrez refléter ce mapping en Java (ex: `@Inheritance(strategy = InheritanceType.JOINED)` ou `SINGLE_TABLE`).

**BaseUser**
- `id` (Integer)
- `email` (String)
- `roles` (List<String>)
- `password` (String)
- `firstName` (String)
- `lastName` (String)
- `age` (Integer)
- `gender` (String)
- `isActive` (boolean)

**Admin** (hérite de BaseUser)
- `name` (String)

**Patient** (hérite de BaseUser)
- `phoneNumber` (String)
- `address` (String)
- `dateOfBirth` (LocalDateTime)
- `hasInsurance` (boolean)
- `insuranceNumber` (String)
- Relations: `questions` (List<Question>), `feedbacks` (List<Feedback>), `appointments` (List<Appointment>)

**Medecin** (hérite de BaseUser)
- `phoneNumber` (String)
- `specialty` (String)
- `cin` (String)
- `address` (String)
- `governorate` (String)
- `education` (String)
- `experience` (String)
- `isVerified` (boolean)
- `aiAverageScore` (Double)
- `aiScoreUpdatedAt` (LocalDateTime)
- Relations: `reponses` (List<Reponse>), `feedbacks` (List<Feedback>), `appointments` (List<Appointment>)

### 2. Rendez-vous et Disponibilités

**Appointment**
- `id` (Integer)
- `date` (LocalDateTime)
- `startTime` (LocalDateTime)
- `duration` (Integer)
- `status` (String)
- `message` (String)
- `department` (String)
- `patient` (Patient - ManyToOne)
- `doctor` (Medecin - ManyToOne)
- `createdAt` (LocalDateTime)
- `reminderSent` (boolean)
- Relations: `feedbacks` (List<Feedback>)

**CalendarSetting / TempsTravail / Indisponibilite**
- **CalendarSetting**: `id`, `slotDuration`, `pauseStart`, `pauseEnd`, `doctorId`
- **TempsTravail**: `id`, `dayOfWeek`, `startTime`, `endTime`, `doctorId`, `specificDate`
- **Indisponibilite**: `id`, `date`, `doctorId`, `isEmergency`

### 3. Consultation (Dossier Médical)

**Document**
- `id`, `nom`, `type`, `chemin`, `taille`, `description`, `createdAt`, `updatedAt`
- Relations: `medecin` (Medecin), `patient` (Patient)

**Ordonnance**
- `id`, `dateOrdonnance`, `diagnosis`, `medicament`, `posologie`, `notes`, `instructions`, `createdAt`, `updatedAt`
- Relations: `patient` (Patient), `medecin` (Medecin), `appointment` (Appointment), `document` (Document)

**Rapport**
- `id`, `consultationReason`, `createdAt`, `diagnosis`, `observations`, `recommendations`, `treatments`, `updatedAt`
- Relations: `patient` (Patient), `medecin` (Medecin), `appointment` (Appointment), `document` (Document)

### 4. Paramètres et Feedback

**Feedback**
- `id`, `rating` (Integer), `comment` (String), `createdAt` (LocalDateTime), `sentimentScore` (Double)
- Relations: `patient` (Patient), `medecin` (Medecin), `appointment` (Appointment)

**PatientNotification**
- `id`, `message`, `targetUrl`, `isRead`, `createdAt`
- Relations: `patient` (Patient)

### 5. Forum / Magazine

**Article**
- `id`, `title`, `resume`, `auteur`, `datePub`, `statut`, `image`
- Relations: `magazine` (Magazine)

**Magazine**
- `id`, `title`, `description`, `image`, `dateCreate`, `statut`, `pdfFile`
- Relations: `articles` (List<Article>)

**Question**
- `id`, `titre`, `description`, `createdAt`, `updatedAt`, `likes`, `status`, `imageName`
- Relations: `specialite` (Specialite), `patient` (Patient), `reponses` (List<Reponse>)

**Reponse**
- `id`, `contenu`, `createdAt`, `likes`
- Relations: `question` (Question), `medecin` (Medecin)

**Specialite**
- `id`, `nom`, `description`
- Relations: `questions` (List<Question>)

## Considérations Importantes pour la Base de Données Partagée

Puisque que le backend en PHP (Symfony Doctrine) et l'application Java (JavaFX) utiliseront la même base de données :

1. **Ne laissez pas Java modifier le Schéma Structurel** : Mettez la propriété `hibernate.hbm2ddl.auto` à `none` ou `validate` dans le fichier `hibernate.cfg.xml`. C'est Symfony qui gère l'état et l'évolution de la base de données via ses migrations (`php bin/console make:migration`).
2. **Génération d'Identifiants** : Assurez-vous d'utiliser `@GeneratedValue(strategy = GenerationType.IDENTITY)` ou équivalent pour les ID.
3. **Mots de passe Utilisateur** : Si l'application JavaFX a un système de login natif (se connectant sans API), elle devra hasher/vérifier les mots de passe de la même façon que l'encodeur Symfony (généralement `Bcrypt` ou `Argon2i`). Sinon, l'authentification JavaFX devrait simplement utiliser des appels d'API HTTPS via un SpringBoot ou PHP Backend.

Fin du Document.
