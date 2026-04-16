package controller;

import entity.RendezVous;
import service.RendezVousService;
import utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class RendezVousController {

    @FXML private TextField nomField;
    @FXML private TextField dateField;
    @FXML private TextField heureField;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String> colNom;
    @FXML private TableColumn<RendezVous, String> colMedecin;
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colStatut;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<String> medecinCombo;

    private RendezVousService service = new RendezVousService();
    private RendezVous selectedRdv = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(
                new PropertyValueFactory<>("nomPatient"));
        colMedecin.setCellValueFactory(
                new PropertyValueFactory<>("medecin"));
        colDate.setCellValueFactory(
                new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(
                new PropertyValueFactory<>("heure"));
        colStatut.setCellValueFactory(
                new PropertyValueFactory<>("statut"));
        
        statutCombo.getItems().addAll("En attente", "Terminé", "Annulé");
        medecinCombo.getItems().addAll(
                "Dr. Ali Ben Salah (Généraliste)", "Dr. Sarah Mansour (Cardiologue)",
                "Dr. Youssef Khelil (Dermatologue)", "Dr. Amira Trabelsi (Pédiatre)",
                "Dr. Mehdi Jendoubi (Ophtalmologue)", "Dr. Leila Bouzid (Gynécologue)",
                "Dr. Sami Gharbi (Neurologue)", "Dr. Rania Ayed (Dentiste)"
        );
        tableView.setItems(service.getAll());

        // Click row to fill form
        tableView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, newVal) -> {
                    if (newVal != null) {
                        selectedRdv = newVal;
                        nomField.setText(newVal.getNomPatient());
                        medecinCombo.setValue(newVal.getMedecin());
                        dateField.setText(newVal.getDate());
                        heureField.setText(newVal.getHeure());
                        statutCombo.setValue(newVal.getStatut());
                    }
                });

        // SEARCH — bonus feature for 1 pt
        searchField.textProperty().addListener(
                (obs, oldVal, newVal) -> filterTable(newVal));

        refreshTable();
    }

    private void refreshTable() {
        ObservableList<RendezVous> all = service.getAll();
        tableView.setItems(all);
        if (totalLabel != null) {
            totalLabel.setText("Total: " + all.size());
        }
    }

    private void updateStats() {
        // Obsolete but kept for backwards comp
        if (totalLabel != null) {
            totalLabel.setText("Total: " + service.getAll().size());
        }
    }

    private void filterTable(String query) {
        if (query == null || query.isEmpty()) {
            tableView.setItems(service.getAll());
            return;
        }
        ObservableList<RendezVous> filtered =
                FXCollections.observableArrayList();
        for (RendezVous r : service.getAll()) {
            if (r.getNomPatient().toLowerCase()
                    .contains(query.toLowerCase())) {
                filtered.add(r);
            }
        }
        tableView.setItems(filtered);
    }

    @FXML
    public void handleAjouter() {
        String nom = nomField.getText().trim();
        String date = dateField.getText().trim();
        String heure = heureField.getText().trim();
        String medecin = medecinCombo.getValue();

        // VALIDATION
        if (Validator.isEmpty(nom)) {
            showAlert("Erreur", "Le nom du patient est obligatoire.");
            return;
        }
        if (medecin == null) {
            showAlert("Erreur", "Le choix du médecin est obligatoire.");
            return;
        }
        if (!Validator.isValidDate(date)) {
            showAlert("Erreur",
                    "Date invalide. Format attendu: dd/MM/yyyy");
            return;
        }
        if (!Validator.isValidTime(heure)) {
            showAlert("Erreur",
                    "Heure invalide. Format attendu: HH:mm");
            return;
        }
        // UNIQUENESS CHECK
        if (Validator.isDuplicate(service.getAll(),
                nom, date, heure, -1)) {
            showAlert("Erreur",
                    "Ce rendez-vous existe déjà !");
            return;
        }
        
        String statut = statutCombo.getValue();
        if (statut == null) statut = "En attente";

        service.ajouter(new RendezVous(0, nom, medecin, date, heure, statut));
        refreshTable();
        showSuccess("Rendez-vous ajouté avec succès !");
        clearForm();
    }

    @FXML
    public void handleModifier() {
        if (selectedRdv == null) {
            showAlert("Erreur",
                    "Sélectionnez un rendez-vous à modifier.");
            return;
        }
        String nom = nomField.getText().trim();
        String date = dateField.getText().trim();
        String heure = heureField.getText().trim();
        String medecin = medecinCombo.getValue();

        if (Validator.isEmpty(nom) || !Validator.isValidDate(date)
                || !Validator.isValidTime(heure) || medecin == null) {
            showAlert("Erreur", "Données invalides ou médecin non sélectionné.");
            return;
        }
        if (Validator.isDuplicate(service.getAll(), nom, date,
                heure, selectedRdv.getId())) {
            showAlert("Erreur",
                    "Un autre rendez-vous identique existe déjà !");
            return;
        }
        
        String statut = statutCombo.getValue();
        if(statut == null) statut = "En attente";
        
        service.modifier(selectedRdv, nom, medecin, date, heure, statut);
        refreshTable();
        showSuccess("Rendez-vous modifié !");
        clearForm();
    }

    @FXML
    public void handleSupprimer() {
        if (selectedRdv == null) {
            showAlert("Erreur",
                    "Sélectionnez un rendez-vous à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer ce rendez-vous ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                service.supprimer(selectedRdv);
                refreshTable();
                clearForm();
            }
        });
    }

    @FXML
    public void openFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/feedback.fxml"));
            Parent root = loader.load();
            FeedbackController fc = loader.getController();
            fc.setRendezVousService(service);

            Stage stage = new Stage();
            stage.setTitle("Feedbacks");
            stage.setScene(new Scene(root, 650, 480));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface Feedback.");
        }
    }

    @FXML
    public void handleReset() {
        clearForm();
        tableView.getSelectionModel().clearSelection();
    }


    private void clearForm() {
        nomField.clear();
        medecinCombo.setValue(null);
        dateField.clear();
        heureField.clear();
        statutCombo.setValue(null);
        selectedRdv = null;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
