package com.pidev.controllers;

import com.pidev.models.Rapport;
import com.pidev.models.RendezVous;
import com.pidev.services.RapportService;
import com.pidev.services.RendezVousService;
import com.pidev.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class RapportFormController implements Initializable {

    @FXML private TextField consultationReasonTF;
    @FXML private TextField diagnosisTF;
    @FXML private TextArea observationsTA;
    @FXML private TextArea recommendationsTA;
    @FXML private TextArea treatmentsTA;
    @FXML private ComboBox<RendezVous> rendezVousCB;

    private final RapportService rapportService = new RapportService();
    private final RendezVousService rdvService = new RendezVousService();
    private Rapport currentRapport;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        loadRendezVous();
    }

    private void setupComboBox() {
        rendezVousCB.setConverter(new StringConverter<>() {
            @Override
            public String toString(RendezVous r) {
                return r == null ? "" : "RDV #" + r.getId() + " - " + r.getDate() + " (" + r.getStatut() + ")";
            }
            @Override
            public RendezVous fromString(String string) { return null; }
        });
    }

    private void loadRendezVous() {
        Integer patientId = UserSession.getInstance().getSelectedPatientId();
        List<RendezVous> list;
        if (patientId != null) {
            list = rdvService.getByPatient(patientId);
        } else {
            list = rdvService.getAll();
        }
        rendezVousCB.setItems(FXCollections.observableArrayList(list));
        
        // Auto-select if there's a global selection
        Integer selectedId = UserSession.getInstance().getSelectedAppointmentId();
        if (selectedId != null) {
            for (RendezVous r : list) {
                if (r.getId() == selectedId) {
                    rendezVousCB.setValue(r);
                    break;
                }
            }
        }
    }

    public void setRapport(Rapport r) {
        this.currentRapport = r;
        if (r != null) {
            consultationReasonTF.setText(r.getConsultationReason());
            diagnosisTF.setText(r.getDiagnosis());
            observationsTA.setText(r.getObservations());
            recommendationsTA.setText(r.getRecommendations());
            treatmentsTA.setText(r.getTreatments());
            
            // Select correct RDV
            for (RendezVous rdv : rendezVousCB.getItems()) {
                if (rdv.getId() == r.getAppointmentId()) {
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

        Rapport r = (currentRapport != null) ? currentRapport : new Rapport();
        r.setAppointmentId(rendezVousCB.getValue().getId());
        r.setConsultationReason(consultationReasonTF.getText());
        r.setDiagnosis(diagnosisTF.getText());
        r.setObservations(observationsTA.getText());
        r.setRecommendations(recommendationsTA.getText());
        r.setTreatments(treatmentsTA.getText());
        r.setPatientId(UserSession.getInstance().getSelectedPatientId());

        try {
            if (currentRapport != null) {
                rapportService.update(r);
            } else {
                rapportService.add(r);
            }
            if (MainUserController.getInstance() != null) {
                MainUserController.getInstance().showOrdonnanceRapport();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer le rapport: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
