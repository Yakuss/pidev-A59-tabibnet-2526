package controller;

import entity.RendezVous;
import service.RendezVousService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MedecinController {

    @FXML private ComboBox<String> proMedecinSelect;
    @FXML private TableView<RendezVous> proTableView;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String> colPatient;
    @FXML private TableColumn<RendezVous, String> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colStatut;

    @FXML private HBox actionBox;
    @FXML private ComboBox<String> actionStatutCombo;

    @FXML private TextField searchPatientField;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> sortDateCombo;

    private RendezVousService rdvService = new RendezVousService();
    private RendezVous selectedRdv = null;
    private List<RendezVous> currentDoctorRdvs = new ArrayList<>();

    @FXML
    public void initialize() {
        proMedecinSelect.getItems().addAll(
                "Dr. Ali Ben Salah (Généraliste)", "Dr. Sarah Mansour (Cardiologue)",
                "Dr. Youssef Khelil (Dermatologue)", "Dr. Amira Trabelsi (Pédiatre)",
                "Dr. Mehdi Jendoubi (Ophtalmologue)", "Dr. Leila Bouzid (Gynécologue)",
                "Dr. Sami Gharbi (Neurologue)", "Dr. Rania Ayed (Dentiste)"
        );

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("nomPatient"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        actionStatutCombo.getItems().addAll("En attente", "Terminé", "Annulé");
        filterStatutCombo.getItems().addAll("Tous", "En attente", "Terminé", "Annulé");
        sortDateCombo.getItems().addAll("Plus anciens d'abord", "Plus récents d'abord");

        searchPatientField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        filterStatutCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> applyFilters());
        sortDateCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> applyFilters());

        proMedecinSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if(newV != null) loadAgenda();
        });

        proTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedRdv = newVal;
                actionBox.setVisible(true);
                actionStatutCombo.setValue(newVal.getStatut());
            } else {
                actionBox.setVisible(false);
                selectedRdv = null;
            }
        });
    }

    private void loadAgenda() {
        String medecin = proMedecinSelect.getValue();
        if(medecin == null) return;

        currentDoctorRdvs.clear();
        for (RendezVous r : rdvService.getAll()) {
            if (r.getMedecin().equals(medecin)) {
                currentDoctorRdvs.add(r);
            }
        }
        applyFilters();
    }

    private void applyFilters() {
        String search = searchPatientField.getText() == null ? "" : searchPatientField.getText().toLowerCase().trim();
        String statut = filterStatutCombo.getValue();
        String sort = sortDateCombo.getValue();

        ObservableList<RendezVous> filtered = FXCollections.observableArrayList();

        for (RendezVous r : currentDoctorRdvs) {
            boolean match = true;
            if (!search.isEmpty() && !r.getNomPatient().toLowerCase().contains(search)) match = false;
            if (statut != null && !statut.equals("Tous") && !r.getStatut().equals(statut)) match = false;
            
            if (match) filtered.add(r);
        }

        if (sort != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            filtered.sort((a, b) -> {
                try {
                    LocalDate da = LocalDate.parse(a.getDate(), fmt);
                    LocalDate db = LocalDate.parse(b.getDate(), fmt);
                    if ("Plus anciens d'abord".equals(sort)) return da.compareTo(db);
                    else return db.compareTo(da);
                } catch (Exception e) {
                    return 0;
                }
            });
        }

        proTableView.setItems(filtered);
    }

    @FXML
    public void handleUpdateStatut() {
        if(selectedRdv == null) return;
        String newStatut = actionStatutCombo.getValue();
        if(newStatut == null) return;

        rdvService.modifier(selectedRdv, selectedRdv.getNomPatient(), selectedRdv.getMedecin(), 
                            selectedRdv.getDate(), selectedRdv.getHeure(), newStatut);
        
        loadAgenda();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Le statut a été mis à jour avec succès.");
        alert.showAndWait();
    }
}
