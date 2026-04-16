package controller;

import entity.Feedback;
import entity.RendezVous;
import service.FeedbackService;
import service.RendezVousService;
import utils.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class FeedbackController {

    @FXML private TextArea commentaireArea;
    @FXML private ComboBox<Integer> noteCombo;
    @FXML private ComboBox<RendezVous> rdvCombo;
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, Integer> colId;
    @FXML private TableColumn<Feedback, String> colCommentaire;
    @FXML private TableColumn<Feedback, Integer> colNote;
    @FXML private TableColumn<Feedback, Integer> colRdvId;

    private FeedbackService feedbackService = new FeedbackService();
    // Shared service — pass it from main or use singleton
    private RendezVousService rdvService;
    private Feedback selectedFeedback = null;

    public void setRendezVousService(RendezVousService s) {
        this.rdvService = s;
        // Load RDV list into combo
        rdvCombo.setItems(s.getAll());
    }

    @FXML
    public void initialize() {
        noteCombo.getItems().addAll(1, 2, 3, 4, 5);
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colCommentaire.setCellValueFactory(
                new PropertyValueFactory<>("commentaire"));
        colNote.setCellValueFactory(
                new PropertyValueFactory<>("note"));
        colRdvId.setCellValueFactory(
                new PropertyValueFactory<>("rendezVousId"));
        feedbackTable.setItems(feedbackService.getAll());

        feedbackTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedFeedback = newV;
                commentaireArea.setText(newV.getCommentaire());
                noteCombo.setValue(newV.getNote());
                if (rdvService != null) {
                    rdvCombo.setValue(rdvService.findById(newV.getRendezVousId()));
                }
            }
        });
    }

    private void refreshTable() {
        feedbackTable.setItems(feedbackService.getAll());
        if (rdvService != null) {
            rdvCombo.setItems(rdvService.getAll());
        }
    }

    @FXML
    public void handleAjouter() {
        String commentaire = commentaireArea.getText().trim();
        Integer note = noteCombo.getValue();
        RendezVous rdv = rdvCombo.getValue();

        if (Validator.isEmpty(commentaire)) {
            showAlert("Le commentaire est obligatoire.");
            return;
        }
        if (note == null || !Validator.isValidNote(note)) {
            showAlert("Choisissez une note entre 1 et 5.");
            return;
        }
        if (rdv == null) {
            showAlert("Sélectionnez un rendez-vous.");
            return;
        }

        Feedback f = new Feedback(0, commentaire, note, rdv.getId());
        feedbackService.ajouter(f);
        refreshTable();
        clearForm();
        showSuccess("Feedback ajouté !");
    }

    @FXML
    public void handleModifier() {
        if (selectedFeedback == null) {
            showAlert("Sélectionnez un feedback à modifier.");
            return;
        }

        String commentaire = commentaireArea.getText().trim();
        Integer note = noteCombo.getValue();
        RendezVous rdv = rdvCombo.getValue();

        if (Validator.isEmpty(commentaire)) {
            showAlert("Le commentaire est obligatoire.");
            return;
        }
        if (note == null || !Validator.isValidNote(note)) {
            showAlert("Choisissez une note entre 1 et 5.");
            return;
        }
        if (rdv == null) {
            showAlert("Sélectionnez un rendez-vous.");
            return;
        }

        feedbackService.modifier(selectedFeedback, commentaire, note, rdv.getId());
        refreshTable();
        clearForm();
        showSuccess("Feedback modifié !");
    }

    @FXML
    public void handleSupprimer() {
        Feedback selected = feedbackTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un feedback à supprimer.");
            return;
        }
        feedbackService.supprimer(selected);
        refreshTable();
        clearForm();
    }

    @FXML
    public void handleReset() {
        clearForm();
        feedbackTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        commentaireArea.clear();
        noteCombo.setValue(null);
        rdvCombo.setValue(null);
        selectedFeedback = null;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès");
        a.setContentText(msg);
        a.showAndWait();
    }
}