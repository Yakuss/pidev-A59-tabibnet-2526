package com.pidev.controllers;

import com.pidev.models.Ordonnance;
import com.pidev.models.RendezVous;
import com.pidev.services.OrdonnanceService;
import com.pidev.services.RendezVousService;
import com.pidev.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class OrdonnanceFormController implements Initializable {

    @FXML private DatePicker datePicker;
    @FXML private TextField diagnosisTF;
    @FXML private TextField medicamentTF;
    @FXML private TextField posologieTF;
    @FXML private TextArea notesTA;
    @FXML private TextArea instructionsTA;
    @FXML private ComboBox<RendezVous> rendezVousCB;

    private final OrdonnanceService ordonnanceService = new OrdonnanceService();
    private final RendezVousService rdvService = new RendezVousService();
    private Ordonnance currentOrdonnance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        loadRendezVous();
    }

    private void setupComboBox() {
        rendezVousCB.setConverter(new StringConverter<>() {
            @Override
            public String toString(RendezVous r) {
                return r == null ? "" : "RDV #" + r.getId() + " - " + r.getDate();
            }
            @Override
            public RendezVous fromString(String string) { return null; }
        });
    }

    private void loadRendezVous() {
        Integer patientId = UserSession.getInstance().getSelectedPatientId();
        List<RendezVous> list;
        try {
            if (patientId != null) {
                list = rdvService.getByPatient(patientId);
            } else {
                list = rdvService.getAll();
            }
            rendezVousCB.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Integer selectedId = UserSession.getInstance().getSelectedAppointmentId();
        if (selectedId != null) {
            for (RendezVous r : rendezVousCB.getItems()) {
                if (r.getId() == selectedId) {
                    rendezVousCB.setValue(r);
                    break;
                }
            }
        }
    }

    public void setOrdonnance(Ordonnance o) {
        this.currentOrdonnance = o;
        if (o != null) {
            if (o.getDateOrdonnance() != null) datePicker.setValue(o.getDateOrdonnance().toLocalDate());
            diagnosisTF.setText(o.getDiagnosis());
            medicamentTF.setText(o.getMedicament());
            posologieTF.setText(o.getPosologie());
            notesTA.setText(o.getNotes());
            instructionsTA.setText(o.getInstructions());
            
            for (RendezVous rdv : rendezVousCB.getItems()) {
                if (rdv.getId() == o.getAppointmentId()) {
                    rendezVousCB.setValue(rdv);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (rendezVousCB.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un rendez-vous.");
            return;
        }

        Ordonnance o = (currentOrdonnance != null) ? currentOrdonnance : new Ordonnance();
        o.setAppointmentId(rendezVousCB.getValue().getId());
        o.setDiagnosis(diagnosisTF.getText());
        o.setMedicament(medicamentTF.getText());
        o.setPosologie(posologieTF.getText());
        o.setNotes(notesTA.getText());
        o.setInstructions(instructionsTA.getText());
        o.setPatientId(UserSession.getInstance().getSelectedPatientId());
        o.setMedecinId(UserSession.getInstance().getUser().getId());
        
        if (datePicker.getValue() != null) {
            o.setDateOrdonnance(datePicker.getValue().atStartOfDay());
        } else {
            o.setDateOrdonnance(LocalDateTime.now());
        }

        try {
            if (currentOrdonnance != null) {
                ordonnanceService.update(o);
            } else {
                ordonnanceService.add(o);
            }
            if (MainUserController.getInstance() != null) {
                MainUserController.getInstance().showOrdonnances();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML private void handleCancel() {
        if (MainUserController.getInstance() != null) {
            MainUserController.getInstance().showOrdonnances();
        }
    }
}
