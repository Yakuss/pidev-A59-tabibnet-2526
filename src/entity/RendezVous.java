package entity;

public class RendezVous {
    private int id;
    private String nomPatient;
    private String medecin;
    private String date;      // format: dd/MM/yyyy
    private String heure;     // format: HH:mm
    private String statut;    // "En attente", "Terminé", "Annulé"

    public RendezVous() {}

    public RendezVous(int id, String nomPatient, String medecin,
                      String date, String heure, String statut) {
        this.id = id;
        this.nomPatient = nomPatient;
        this.medecin = medecin;
        this.date = date;
        this.heure = heure;
        this.statut = statut != null ? statut : "En attente";
    }

    public RendezVous(int id, String nomPatient, String medecin,
                      String date, String heure) {
        this(id, nomPatient, medecin, date, heure, "En attente");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }

    public String getMedecin() { return medecin; }
    public void setMedecin(String medecin) {
        this.medecin = medecin;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "RDV#" + id + " - " + nomPatient + " avec " + medecin
                + " le " + date + " à " + heure + " [" + statut + "]";
    }
}