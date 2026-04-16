# Plateforme de Gestion de Rendez-vous Médicaux (TabibNet)

Ce projet est une application JavaFX permettant la gestion complète des rendez-vous médicaux et des retours patients (feedbacks).

## Architecture du Projet
Le projet suit le modèle d'architecture **MVC (Modèle-Vue-Contrôleur)** adapté pour JavaFX :

*   **Modèle (`entity/` et `service/`)** :
    *   Les packages `entity` (`RendezVous.java`, `Feedback.java`) représentent les données sous forme d'objets ou "classes métiers". 
    *   Les packages `service` (`RendezVousService.java`, `FeedbackService.java`) gèrent la logique métier, maintiennent les listes de données (`ObservableList`) et fournissent les opérations CRUD (Ajouter, Modifier, Supprimer, Récupérer).
*   **Vue (`resources/*.fxml`)** :
    *   Définit l'interface graphique (GUI) de l'application à l'aide de fichiers XML structurés pour JavaFX (`rendez_vous.fxml` et `feedback.fxml`).
*   **Contrôleur (`controller/`)** :
    *   Fait le lien entre le Modèle et la Vue. Les contrôleurs (`RendezVousController`, `FeedbackController`) s'assurent de récupérer les événements utilisateur (clic sur un bouton), de valider les saisies via le package `utils`, de solliciter les `Services` puis de mettre à jour la `TableView` et autres composants visuels.

## Explications des Classes et Fonctionnalités

### 1. Entités
*   **RendezVous** : Contient l'`id`, le `nomPatient`, la `date` et l'`heure`. Le surchargement de la méthode `toString()` a été configuré pour une belle présentation dans la ComboBox des feedbacks.
*   **Feedback** : Contient le `commentaire`, la `note` (1-5), et l'identifiant du rendez-vous associé (`rendezVousId`). Cela modélise l'association entre feedback et rendez-vous.

### 2. Services (Logique Métier & CRUD)
*   **RendezVousService** : Implémente toutes les méthodes pour la gestion des listes en mémoire `ObservableList<RendezVous>`.
*   **FeedbackService** : Implémente le **CRUD Complet** des retours patients. Nous avons ajouté la méthode `modifier()` pour combler la grille de notation.

### 3. Contrôleurs (Interactions)
*   La prise de rendez-vous ainsi que sa recherche/filtrage (bonus statistique et tri inclus) se gèrent dans **RendezVousController**. Des alertes contextuelles apparaissent en cas de mauvaise saisie.
*   La modification du feedback et l'unicité (pour la réservation) se font en utilisant la classe **Validator** (dans le package utils).

---

## 🛠️ Comment résoudre l'erreur d'exécution JavaFX dans IntelliJ 
L'erreur `Error: JavaFX runtime components are missing...` que vous avez reçue se produit car JavaFX n'est plus inclus nativement dans le JDK depuis Java 11.

Pour corriger cela (**très important pour lancer le projet**), dans IntelliJ IDEA :
1. En haut à droite, à côté de l'icône "Play" verte, cliquez sur **Main** ou **Current File** puis sur **Edit Configurations...** (Modifier les configurations).
2. Vérifiez que la classe principale (Main class) est bien `Main`.
3. Cliquez sur **Modify options** (ou "Add VM options" / "Options de la machine virtuelle" selon votre version d'IntelliJ) et cochez **Add VM options**.
4. Dans le champ **VM options** qui s'affiche, copiez et collez la ligne **EXACTE** suivante :
   `--module-path "C:\Users\Zid Ayoub\Downloads\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml`
5. Cliquez sur **Apply** puis **OK**.
6. Relancez l'application avec le bouton "Play" !

---

## 💻 Validation de la grille Git (Branche Perso + Commits)
Pour obtenir vos **2 points collaboratifs**, ouvrez le terminal d'IntelliJ (en bas) et exécutez ces commandes l'une après l'autre :

1. Initialiser le dépôt Git :
   `git init`
2. Créer une branche personnelle selon les conventions collaboratives :
   `git checkout -b branche_zid_ayoub`
3. Ajouter tous les fichiers au suivi :
   `git add .`
4. Créer le commit final :
   `git commit -m "Implémentation CRUD Complet, Interface Feedback, et Contrôles de Saisie"`
