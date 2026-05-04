# Guide: Filtrage Automatique des Rapports et Ordonnances

## Vue d'ensemble

Quand vous sélectionnez un rendez-vous dans "Gestion des Rendez-vous", les informations (date, patient, médecin) sont stockées. Quand vous allez à "Rapports & Ordonnances", vous voyez **SEULEMENT** les rapports et ordonnances de ce patient et ce médecin.

## Fonctionnalités

### 1. **Sélection du Rendez-vous**
- Allez à "Gestion des Rendez-vous"
- Cliquez sur un rendez-vous
- Les informations sont stockées:
  - Date et heure
  - Nom du patient
  - Nom du médecin
  - ID du patient
  - ID du médecin

### 2. **Filtrage Automatique**
- Allez à "Dossier Médical" → "Ordonnances & Rapports"
- Les tableaux affichent **SEULEMENT**:
  - Les ordonnances de ce patient et ce médecin
  - Les rapports de ce patient et ce médecin
- Les compteurs affichent le nombre filtré

### 3. **Logs de Débogage**
Quand vous allez à "Rapports & Ordonnances", vous verrez dans la console:
```
📋 Filtrage par rendez-vous sélectionné:
   Patient ID: 1
   Médecin ID: 2
✅ Ordonnances trouvées: 3
✅ Rapports trouvés: 2
```

## Flux d'Utilisation

### Scénario 1: Voir les Rapports d'un Patient

```
1. Allez à "Gestion des Rendez-vous"
   ↓
2. Cliquez sur le rendez-vous "Patient Test" / "Dr. Martin"
   → Infos stockées
   ↓
3. Allez à "Dossier Médical" → "Ordonnances & Rapports"
   ↓
4. Vous voyez SEULEMENT:
   - Les ordonnances de "Patient Test" avec "Dr. Martin"
   - Les rapports de "Patient Test" avec "Dr. Martin"
```

### Scénario 2: Voir Tous les Rapports (Sans Sélection)

```
1. Allez directement à "Dossier Médical" → "Ordonnances & Rapports"
   ↓
2. Vous voyez TOUS les rapports et ordonnances orphelins
   (pas de rendez-vous sélectionné)
```

## Fichiers Modifiés

### 1. **DashboardController.java**
- Méthode `refreshData()` modifiée
- Vérifie si un rendez-vous est sélectionné
- Filtre les données en conséquence

### 2. **OrdonnanceService.java**
- Ajout de la méthode `getByPatientAndDoctor()`
- Récupère les ordonnances pour un patient et un médecin

### 3. **RapportService.java**
- Ajout de la méthode `getByPatientAndDoctor()`
- Récupère les rapports pour un patient et un médecin

### 4. **AppointmentSessionManager.java**
- Stockage global du rendez-vous sélectionné
- Accessible de n'importe quel contrôleur

## Détails Techniques

### Flux de Données

```
AppointmentController (sélection utilisateur)
    ↓
AppointmentSessionManager.setSelectedAppointment()
    ↓
DashboardController.refreshData()
    ↓
Vérifie si rendez-vous sélectionné
    ↓
Si OUI:
  - OrdonnanceService.getByPatientAndDoctor()
  - RapportService.getByPatientAndDoctor()
    ↓
Si NON:
  - OrdonnanceService.findOrphans()
  - RapportService.findOrphans()
    ↓
Affiche les résultats filtrés
```

### Méthodes Clés

**AppointmentSessionManager:**
```java
setSelectedAppointment(Appointment) - Stocke le rendez-vous
getSelectedAppointment() - Récupère le rendez-vous
getSelectedPatientId() - ID du patient
getSelectedDoctorId() - ID du médecin
```

**OrdonnanceService:**
```java
getByPatientAndDoctor(int patientId, int doctorId)
  - Retourne les ordonnances pour ce patient et médecin
```

**RapportService:**
```java
getByPatientAndDoctor(int patientId, int doctorId)
  - Retourne les rapports pour ce patient et médecin
```

**DashboardController:**
```java
refreshData()
  - Charge les données filtrées ou orphelines
  - Vérifie la sélection du rendez-vous
```

## Cas d'Utilisation

### 1. **Médecin Consulte les Rapports d'un Patient**
- Sélectionne le rendez-vous du patient
- Va à "Rapports & Ordonnances"
- Voit tous les rapports de ce patient avec ce médecin

### 2. **Créer un Rapport pour un Patient Spécifique**
- Sélectionne le rendez-vous
- Va à "Rapports & Ordonnances"
- Voit les rapports existants
- Crée un nouveau rapport

### 3. **Gérer les Ordonnances d'un Patient**
- Sélectionne le rendez-vous
- Va à "Rapports & Ordonnances"
- Voit les ordonnances existantes
- Peut modifier ou ajouter des ordonnances

## Logs de Débogage

### Quand vous sélectionnez un rendez-vous:
```
✅ Rendez-vous stocké en session:
   ID: 1
   Date: 2026-05-10T14:30
   Patient: Jean Dupont
   Médecin: Dr. Marie Martin
   Département: Cardiologie
```

### Quand vous allez à "Rapports & Ordonnances":
```
📋 Filtrage par rendez-vous sélectionné:
   Patient ID: 1
   Médecin ID: 2
✅ Ordonnances trouvées: 3
✅ Rapports trouvés: 2
```

### Quand vous allez directement sans sélection:
```
(Pas de message de filtrage)
(Affiche tous les rapports et ordonnances orphelins)
```

## Points Importants

✅ Le filtrage est **automatique**  
✅ Aucune action manuelle n'est requise  
✅ Les données sont **en temps réel**  
✅ Vous pouvez toujours voir tous les rapports en allant directement à "Rapports & Ordonnances"  
✅ Le filtrage fonctionne avec les **compteurs dynamiques**  
✅ La **recherche** fonctionne aussi sur les données filtrées  

## Améliorations Futures

- [ ] Ajouter un indicateur visuel montrant le filtrage actif
- [ ] Ajouter un bouton "Réinitialiser le filtre"
- [ ] Ajouter un badge avec le nom du patient/médecin
- [ ] Ajouter un historique des sélections
