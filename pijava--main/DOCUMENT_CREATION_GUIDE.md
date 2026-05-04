# Guide: Création de Documents avec Rapports et Ordonnances

## Vue d'ensemble

Quand vous créez un document dans "Rapports & Ordonnances" et liez des rapports/ordonnances:
1. ✅ Le document est créé et stocké dans le sous-menu "Documents"
2. ✅ Les rapports et ordonnances **restent visibles** dans le tableau "Rapports & Ordonnances"
3. ✅ Les rapports et ordonnances sont **liés au document** (copie dans le document)
4. ✅ Aucune donnée n'est perdue

## Flux d'Utilisation

### Étape 1: Sélectionner un Rendez-vous
```
1. Allez à "Gestion des Rendez-vous"
2. Cliquez sur un rendez-vous
   → Les infos sont stockées (patient, médecin)
```

### Étape 2: Aller à "Rapports & Ordonnances"
```
1. Allez à "Dossier Médical" → "Ordonnances & Rapports"
2. Vous voyez les rapports et ordonnances du patient et médecin sélectionnés
```

### Étape 3: Créer un Document
```
1. Cliquez le bouton "➕ Créer un document"
2. Une fenêtre s'ouvre avec:
   - Champ "Nom du document"
   - Champ "Type"
   - Champ "Taille"
   - Champ "Description"
   - Bouton "Importer un fichier"
3. Remplissez les champs
4. Cliquez "Créer"
```

### Étape 4: Résultat
```
✅ Document créé et stocké dans "Documents"
✅ Rapports et ordonnances restent dans le tableau
✅ Rapports et ordonnances sont liés au document
✅ Vous pouvez voir le document dans "Dossier Médical" → "Documents"
```

## Détails Techniques

### Avant (Ancien Comportement)
```
Créer un document
    ↓
Lier les rapports/ordonnances
    ↓
Rapports/ordonnances disparaissent du tableau
    ↓
Seuls les rapports/ordonnances orphelins s'affichent
```

### Après (Nouveau Comportement)
```
Créer un document
    ↓
Lier les rapports/ordonnances
    ↓
Rapports/ordonnances RESTENT dans le tableau
    ↓
Rapports/ordonnances sont AUSSI dans le document
    ↓
Affichage: TOUS les rapports/ordonnances (orphelins + liés)
```

### Fichiers Modifiés

**DashboardController.java**
- Méthode `refreshData()` modifiée
- Affiche TOUS les rapports et ordonnances (pas seulement les orphelins)
- Filtre par patient/médecin si un rendez-vous est sélectionné

### Méthodes Clés

**OrdonnanceService:**
```java
getAll() - Retourne TOUS les ordonnances (orphelins + liés)
getByPatientAndDoctor() - Retourne les ordonnances d'un patient/médecin
```

**RapportService:**
```java
getAll() - Retourne TOUS les rapports (orphelins + liés)
getByPatientAndDoctor() - Retourne les rapports d'un patient/médecin
```

**DashboardController:**
```java
refreshData() - Charge TOUS les rapports/ordonnances
```

## Cas d'Utilisation

### Scénario 1: Créer un Document Complet

```
1. Sélectionnez un rendez-vous
   → Patient: Jean Dupont
   → Médecin: Dr. Martin
   
2. Allez à "Rapports & Ordonnances"
   → Vous voyez 2 rapports et 3 ordonnances
   
3. Cliquez "Créer un document"
   → Remplissez le formulaire
   → Cliquez "Créer"
   
4. Résultat:
   ✅ Document créé dans "Documents"
   ✅ 2 rapports et 3 ordonnances liés au document
   ✅ Les 2 rapports et 3 ordonnances restent visibles dans le tableau
```

### Scénario 2: Voir le Document Créé

```
1. Allez à "Dossier Médical" → "Documents"
2. Vous voyez le document créé avec:
   - Nom: "Dossier Complet Jean Dupont"
   - Nombre de rapports: 2
   - Nombre d'ordonnances: 3
3. Cliquez "Voir" pour consulter le document
```

### Scénario 3: Exporter le Document en PDF

```
1. Allez à "Dossier Médical" → "Documents"
2. Cliquez le bouton "PDF" pour le document
3. Sélectionnez le dossier de destination
4. Le PDF est généré avec:
   - Infos du document
   - Tous les rapports liés
   - Toutes les ordonnances liées
```

## Points Importants

✅ Les rapports/ordonnances **ne disparaissent pas** après création du document  
✅ Les rapports/ordonnances sont **copiés** dans le document (pas déplacés)  
✅ Vous pouvez créer **plusieurs documents** avec les mêmes rapports/ordonnances  
✅ Les rapports/ordonnances restent **modifiables** dans le tableau  
✅ Le document affiche le **nombre de rapports et ordonnances** liés  

## Logs de Débogage

Quand vous créez un document, vous verrez dans la console:
```
Document créé avec ID: 5
✅ Ordonnances trouvées: 3
✅ Rapports trouvés: 2
Finalisation: 2 rapports et 3 ordonnances liés.
Document modifié avec succès !
```

Quand vous allez à "Rapports & Ordonnances":
```
✅ Ordonnances trouvées: 3
✅ Rapports trouvés: 2
```

## Améliorations Futures

- [ ] Ajouter un badge "Lié au document" sur les rapports/ordonnances
- [ ] Ajouter un filtre "Afficher seulement les orphelins"
- [ ] Ajouter un filtre "Afficher seulement les liés"
- [ ] Ajouter la possibilité de délier un rapport/ordonnance d'un document
- [ ] Ajouter un historique des documents créés
