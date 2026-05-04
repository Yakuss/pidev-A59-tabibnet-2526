# Guide: Sélection Persistante en Rouge du Rendez-vous

## Vue d'ensemble

Lorsque vous sélectionnez un rendez-vous dans la page "Gestion des Rendez-vous", celui-ci reste **ROUGE** et **SÉLECTIONNÉ** même si vous naviguez vers d'autres pages, menus ou sous-menus de l'application. Une barre d'information rouge s'affiche en bas de l'écran pour vous rappeler quel rendez-vous est actuellement sélectionné.

## Fonctionnalités Principales

### 1. **Sélection Visuelle Persistante (ROUGE)**
- Quand vous cliquez sur un rendez-vous, la ligne devient **ROUGE** avec du texte blanc
- Cette sélection **persiste** même si vous naviguez ailleurs
- Quand vous revenez à la page "Gestion des Rendez-vous", le rendez-vous reste sélectionné en rouge

### 2. **Barre d'Information en Bas (RED BAR)**
- Une barre **ROUGE** s'affiche en bas de l'écran
- Elle affiche les informations du rendez-vous sélectionné:
  - 📅 Date et heure
  - 👤 Nom du patient
  - 🩺 Nom du médecin
  - 🏥 Département
- Un bouton "✕ Effacer" permet de désélectionner le rendez-vous

### 3. **Stockage Global**
- L'information du rendez-vous est stockée dans `AppointmentSessionManager`
- Accessible de n'importe quel contrôleur
- Persiste tant que l'application est ouverte

### 4. **Pré-remplissage Automatique**
- Quand vous ouvrez "Nouveau Rapport" ou "Nouvelle Ordonnance"
- Le rendez-vous sélectionné est **automatiquement pré-rempli**
- Vous n'avez plus besoin de le sélectionner manuellement

## Flux d'Utilisation Complet

### Scénario: Créer un Rapport pour un Rendez-vous

```
1. Allez à "Gestion des Rendez-vous"
   ↓
2. Cliquez sur un rendez-vous
   → La ligne devient ROUGE
   → Une barre ROUGE s'affiche en bas avec les infos
   ↓
3. Naviguez vers "Dossier Médical" → "Ordonnances & Rapports"
   → Le rendez-vous reste ROUGE en bas de l'écran
   ↓
4. Cliquez sur "Nouveau Rapport"
   → Le rendez-vous est AUTOMATIQUEMENT pré-sélectionné
   ↓
5. Remplissez les autres champs et enregistrez
   ↓
6. Retournez à "Gestion des Rendez-vous"
   → Le rendez-vous est TOUJOURS ROUGE et sélectionné
```

## Architecture Technique

### Composants Clés

#### 1. **AppointmentSessionManager** (Singleton)
Gère le stockage global du rendez-vous sélectionné.

**Méthodes principales:**
```java
// Stockage
setSelectedAppointment(Appointment) - Stocke et notifie les listeners
getSelectedAppointment() - Récupère le rendez-vous stocké
clearSelectedAppointment() - Efface la sélection

// Listeners
addSelectionChangeListener(listener) - Enregistre un listener
removeSelectionChangeListener(listener) - Désenregistre un listener

// Getters de commodité
getSelectedAppointmentId()
getSelectedPatientName()
getSelectedDoctorName()
getSelectedDate()
getSelectedPatientId()
getSelectedDoctorId()
getSelectedDepartment()
```

#### 2. **AppointmentController**
Gère la sélection visuelle et le stockage.

**Fonctionnalités:**
- `setupRowFactory()` - Applique le style rouge aux lignes sélectionnées
- `loadAppointments()` - Restaure la sélection quand la page est rechargée
- Listener de sélection qui stocke dans `AppointmentSessionManager`

#### 3. **MainUserController**
Gère la barre d'information en bas de l'écran.

**Fonctionnalités:**
- `updateAppointmentInfoBar()` - Met à jour la barre d'info
- `clearAppointmentSelection()` - Efface la sélection
- Listener global pour les changements de sélection

#### 4. **MainUserView.fxml**
Contient la barre d'information rouge en bas.

```xml
<HBox fx:id="appointmentInfoBar" 
      style="-fx-background-color: #ff0000; ...">
    <Label text="🔴 Rendez-vous sélectionné:"/>
    <Label fx:id="appointmentInfoLabel" text="..."/>
    <Button text="✕ Effacer" onAction="#clearAppointmentSelection"/>
</HBox>
```

### Flux de Données

```
AppointmentController (sélection utilisateur)
    ↓
AppointmentSessionManager.setSelectedAppointment()
    ↓
Notifie tous les listeners enregistrés
    ↓
MainUserController.updateAppointmentInfoBar()
    ↓
Affiche la barre ROUGE en bas
    ↓
RapportController / OrdonnanceController
    ↓
Récupère le rendez-vous et pré-remplit le formulaire
```

## Logs de Débogage

### Quand vous sélectionnez un rendez-vous:
```
✅ Rendez-vous stocké en session:
   ID: 1
   Date: 2026-05-10T14:30
   Patient: Jean Dupont
   Médecin: Dr. Marie Martin
   Département: Cardiologie
✅ Barre d'information mise à jour: 📅 10/05/2026 14:30 | 👤 Jean Dupont | 🩺 Dr. Marie Martin | 🏥 Cardiologie
```

### Quand vous revenez à la page:
```
✅ Rendez-vous restauré de la session: 1
```

### Quand vous effacez la sélection:
```
🗑️ Rendez-vous supprimé de la session
🗑️ Sélection du rendez-vous effacée
```

## Cas d'Utilisation

### 1. **Créer un Rapport pour un Rendez-vous**
- Sélectionnez le rendez-vous
- Allez à "Ordonnances & Rapports"
- Cliquez "Nouveau Rapport"
- Le rendez-vous est pré-sélectionné ✅

### 2. **Créer une Ordonnance pour un Rendez-vous**
- Sélectionnez le rendez-vous
- Allez à "Ordonnances & Rapports"
- Cliquez "Nouvelle Ordonnance"
- Le rendez-vous est pré-sélectionné ✅

### 3. **Consulter les Détails du Rendez-vous**
- Sélectionnez le rendez-vous
- La barre rouge en bas affiche tous les détails
- Vous pouvez naviguer partout, les infos restent visibles

### 4. **Changer de Rendez-vous**
- Cliquez sur un autre rendez-vous
- La sélection change immédiatement
- La barre rouge se met à jour avec les nouvelles infos

### 5. **Désélectionner**
- Cliquez le bouton "✕ Effacer" dans la barre rouge
- Ou cliquez sur le même rendez-vous pour le désélectionner

## Améliorations Futures

- [ ] Ajouter un indicateur visuel dans la barre de navigation
- [ ] Ajouter un son de notification quand un rendez-vous est sélectionné
- [ ] Ajouter un historique des rendez-vous sélectionnés
- [ ] Ajouter des raccourcis clavier pour naviguer entre les rendez-vous
- [ ] Ajouter une option pour "épingler" un rendez-vous

## Notes Importantes

1. **Persistance**: L'information reste stockée jusqu'à ce que vous:
   - Sélectionniez un autre rendez-vous
   - Cliquiez le bouton "✕ Effacer"
   - Fermiez l'application

2. **Automatique**: Aucune action manuelle n'est requise - tout est automatique

3. **Flexible**: Vous pouvez toujours changer le rendez-vous manuellement dans les formulaires

4. **Sécurisé**: Les données sont stockées en mémoire et ne sont pas persistées sur le disque

5. **Performant**: Aucun impact sur les performances de l'application

## Dépannage

### La barre rouge n'apparaît pas
- Vérifiez que vous avez cliqué sur un rendez-vous
- Vérifiez que le rendez-vous est bien sélectionné (ligne rouge)
- Redémarrez l'application

### Le rendez-vous n'est pas pré-sélectionné dans le formulaire
- Vérifiez que la barre rouge est visible en bas
- Vérifiez que le rendez-vous est bien stocké
- Vérifiez les logs de la console

### La sélection disparaît quand je change de page
- C'est normal si vous avez cliqué le bouton "✕ Effacer"
- Sinon, c'est un bug - veuillez redémarrer l'application

## Support

Pour toute question ou problème, consultez les logs de la console ou contactez l'équipe de développement.
