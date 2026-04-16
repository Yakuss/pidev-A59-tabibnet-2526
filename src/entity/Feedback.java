package entity;

public class Feedback {
    private int id;
    private String commentaire;
    private int note;           // 1 to 5
    private int rendezVousId;

    public Feedback() {}

    public Feedback(int id, String commentaire,
                    int note, int rendezVousId) {
        this.id = id;
        this.commentaire = commentaire;
        this.note = note;
        this.rendezVousId = rendezVousId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String c) {
        this.commentaire = c;
    }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public int getRendezVousId() { return rendezVousId; }
    public void setRendezVousId(int id) {
        this.rendezVousId = id;
    }
}