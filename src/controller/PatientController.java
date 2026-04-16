package controller;

import entity.Feedback;
import entity.RendezVous;
import service.FeedbackService;
import service.RendezVousService;
import utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class PatientController {

    @FXML private TextField nomPatientField;
    @FXML private ComboBox<String> medecinCombo;
    @FXML private TextField dateField;
    @FXML private TextField heureField;

    @FXML private TextField searchNomField;
    @FXML private TableView<RendezVous> patientTableView;
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colMedecin;
    @FXML private TableColumn<RendezVous, String> colStatut;

    @FXML private HBox annulerBox;
    @FXML private HBox feedbackBox;
    @FXML private ComboBox<Integer> noteCombo;
    @FXML private TextField commField;

    private RendezVousService rdvService = new RendezVousService();
    private FeedbackService feedbackService = new FeedbackService();
    private RendezVous selectedRdv = null;
    private Feedback existingFeedback = null;

    @FXML
    public void initialize() {
        medecinCombo.getItems().addAll(
                "Dr. Ali Ben Salah (Généraliste)", "Dr. Sarah Mansour (Cardiologue)",
                "Dr. Youssef Khelil (Dermatologue)", "Dr. Amira Trabelsi (Pédiatre)",
                "Dr. Mehdi Jendoubi (Ophtalmologue)", "Dr. Leila Bouzid (Gynécologue)",
                "Dr. Sami Gharbi (Neurologue)", "Dr. Rania Ayed (Dentiste)"
        );

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        noteCombo.getItems().addAll(1, 2, 3, 4, 5);

        patientTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            feedbackBox.setVisible(false);
            annulerBox.setVisible(false);
            selectedRdv = newVal;
            
            if (newVal != null) {
                if ("Terminé".equals(newVal.getStatut())) {
                    feedbackBox.setVisible(true);
                    checkExistingFeedback(newVal.getId());
                } else if ("En attente".equals(newVal.getStatut())) {
                    annulerBox.setVisible(true);
                }
            }
        });
    }

    @FXML
    public void handleReserver() {
        String nom = nomPatientField.getText().trim();
        String medecin = medecinCombo.getValue();
        String date = dateField.getText().trim();
        String heure = heureField.getText().trim();

        if (Validator.isEmpty(nom) || medecin == null) {
            showAlert("Erreur", "Veuillez renseigner votre nom et choisir un médecin.");
            return;
        }
        if (!Validator.isValidDate(date)) {
            showAlert("Erreur", "Date invalide. Format attendu: dd/MM/yyyy");
            return;
        }
        if (!Validator.isValidTime(heure)) {
            showAlert("Erreur", "Heure invalide. Format attendu: HH:mm");
            return;
        }
        if (Validator.isPastDate(date)) {
            showAlert("Erreur", "Vous ne pouvez pas prendre de rendez-vous dans le passé !");
            return;
        }
        if (Validator.isDuplicate(rdvService.getAll(), nom, date, heure, -1)) {
            showAlert("Erreur", "Ce créneau est déjà pris ou ce rendez-vous existe déjà !");
            return;
        }

        rdvService.ajouter(new RendezVous(0, nom, medecin, date, heure, "En attente"));
        showSuccess("Rendez-vous enregistré avec succès !");
        
        nomPatientField.clear();
        medecinCombo.setValue(null);
        dateField.clear();
        heureField.clear();
        
        if(!searchNomField.getText().isEmpty()) handleChercher();
    }

    @FXML
    public void handleChercher() {
        String query = searchNomField.getText().trim();
        if (query.isEmpty()) return;

        ObservableList<RendezVous> filtered = FXCollections.observableArrayList();
        for (RendezVous r : rdvService.getAll()) {
            if (r.getNomPatient().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(r);
            }
        }
        patientTableView.setItems(filtered);
    }

    @FXML
    public void handleAnnulerRdv() {
        if (selectedRdv == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler RDV");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir annuler ce rendez-vous (Date: " + selectedRdv.getDate() + ") ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                rdvService.supprimer(selectedRdv);
                showSuccess("Rendez-vous annulé avec succès !");
                handleChercher(); // Rafraîchir la liste
            }
        });
    }

    private void checkExistingFeedback(int rdvId) {
        ObservableList<Feedback> fbs = feedbackService.getByRendezVous(rdvId);
        if(!fbs.isEmpty()) {
            existingFeedback = fbs.get(0);
            noteCombo.setValue(existingFeedback.getNote());
            commField.setText(existingFeedback.getCommentaire());
        } else {
            existingFeedback = null;
            noteCombo.setValue(5);
            commField.clear();
        }
    }

    @FXML
    public void handleEnvoyerFeedback() {
        if(selectedRdv == null) return;
        Integer note = noteCombo.getValue();
        String comm = commField.getText().trim();

        if(Validator.isEmpty(comm) || note == null) {
            showAlert("Erreur", "Un commentaire et une note sont obligatoires.");
            return;
        }

        if(existingFeedback != null) {
            feedbackService.modifier(existingFeedback, comm, note, selectedRdv.getId());
            showSuccess("Votre avis a été modifié avec succès !");
        } else {
            feedbackService.ajouter(new Feedback(0, comm, note, selectedRdv.getId()));
            showSuccess("Merci pour votre avis !");
            checkExistingFeedback(selectedRdv.getId());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
