# Guide: Sélection de Rendez-vous Persistante

## Vue d'ensemble

Lorsque vous sélectionnez un rendez-vous dans la page "Gestion des Rendez-vous", l'information est maintenant stockée globalement et reste accessible même si vous naviguez vers d'autres pages ou sous-menus.

## Fonctionnalités

### 1. Sélection Visuelle (Rouge)
- Quand vous cliquez sur un rendez-vous dans le tableau, la ligne devient **rouge** avec du texte blanc
- Cette sélection est immédiate et visuelle

### 2. Stockage Global
- L'information du rendez-vous sélectionné est stockée dans `AppointmentSessionManager`
- Les données stockées incluent:
  - ID du rendez-vous
  - Date et heure
  - Nom du patient
  - Nom du médecin
  - Département
  - ID du patient
  - ID du médecin

### 3. Utilisation dans les Rapports & Ordonnances
- Quand vous ouvrez le formulaire "Nouveau Rapport" ou "Nouvelle Ordonnance"
- Le rendez-vous sélectionné est **automatiquement pré-rempli** dans le champ "Rendez-vous"
- Vous n'avez plus besoin de le sélectionner manuellement

## Flux d'utilisation

### Scénario 1: Créer un Rapport pour un Rendez-vous
1. Allez à "Gestion des Rendez-vous"
2. Cliquez sur un rendez-vous (il devient rouge)
3. Allez à "Rapports & Ordonnances" → "Nouveau Rapport"
4. Le rendez-vous est automatiquement sélectionné
5. Remplissez les autres champs et enregistrez

### Scénario 2: Créer une Ordonnance pour un Rendez-vous
1. Allez à "Gestion des Rendez-vous"
2. Cliquez sur un rendez-vous (il devient rouge)
3. Allez à "Rapports & Ordonnances" → "Nouvelle Ordonnance"
4. Le rendez-vous est automatiquement sélectionné
5. Remplissez les autres champs et enregistrez

## Détails Techniques

### AppointmentSessionManager
Classe singleton qui gère le stockage global du rendez-vous sélectionné.

**Méthodes principales:**
- `getInstance()` - Obtient l'instance unique
- `setSelectedAppointment(Appointment)` - Stocke un rendez-vous
- `getSelectedAppointment()` - Récupère le rendez-vous stocké
- `hasSelectedAppointment()` - Vérifie s'il y a un rendez-vous stocké
- `clearSelectedAppointment()` - Efface le rendez-vous stocké

**Getters de commodité:**
- `getSelectedAppointmentId()` - ID du rendez-vous
- `getSelectedPatientName()` - Nom du patient
- `getSelectedDoctorName()` - Nom du médecin
- `getSelectedDate()` - Date et heure
- `getSelectedPatientId()` - ID du patient
- `getSelectedDoctorId()` - ID du médecin
- `getSelectedDepartment()` - Département

### Intégration dans les Contrôleurs

**AppointmentController:**
- Stocke le rendez-vous sélectionné dans `AppointmentSessionManager` lors du clic
- Affiche la sélection en rouge

**RapportController:**
- Récupère le rendez-vous de la session au démarrage
- Pré-remplit automatiquement le champ "Rendez-vous"

**OrdonnanceController:**
- Récupère le rendez-vous de la session au démarrage
- Pré-remplit automatiquement le champ "Rendez-vous"

## Logs de Débogage

Quand vous sélectionnez un rendez-vous, vous verrez dans la console:
```
✅ Rendez-vous stocké en session:
   ID: 1
   Date: 2026-05-10T14:30
   Patient: Jean Dupont
   Médecin: Dr. Marie Martin
   Département: Cardiologie
```

Quand vous ouvrez un formulaire de Rapport/Ordonnance:
```
RapportController: Auto-selecting appointment from session: 1
```

## Notes Importantes

1. **Persistance**: L'information reste stockée jusqu'à ce que vous sélectionniez un autre rendez-vous ou fermiez l'application
2. **Automatique**: Aucune action manuelle n'est requise - la sélection est automatique
3. **Flexible**: Vous pouvez toujours changer le rendez-vous manuellement dans le formulaire si nécessaire
4. **Sécurisé**: Les données sont stockées en mémoire et ne sont pas persistées sur le disque

## Améliorations Futures

- Ajouter un indicateur visuel montrant quel rendez-vous est actuellement sélectionné
- Ajouter un bouton "Effacer la sélection" pour réinitialiser manuellement
- Ajouter une notification quand on navigue vers un formulaire avec un rendez-vous pré-sélectionné
