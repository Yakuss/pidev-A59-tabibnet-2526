package edu.connexion3a77.services;

import edu.connexion3a77.entities.Personne;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonneService implements IService<Personne> {

    @Override
    public void add(Personne personne) throws SQLException {
        String requete = "INSERT INTO personne (nom, prenom) VALUES (?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, personne.getNom());
            pst.setString(2, personne.getPrenom());
            pst.executeUpdate();
            System.out.println("Personne ajoutée avec succès !");
        }
    }

    @Override
    public void update(Personne personne) throws SQLException {
        String requete = "UPDATE personne SET nom = ?, prenom = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, personne.getNom());
            pst.setString(2, personne.getPrenom());
            pst.setInt(3, personne.getId());
            pst.executeUpdate();
            System.out.println("Personne mise à jour avec succès !");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM personne WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Personne supprimée avec succès !");
        }
    }

    @Override
    public Personne findById(int id) throws SQLException {
        String requete = "SELECT * FROM personne WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Personne p = new Personne();
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public List<Personne> findAll() throws SQLException {
        List<Personne> data = new ArrayList<>();
        String requete = "SELECT * FROM personne";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                Personne p = new Personne();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                data.add(p);
            }
        }
        return data;
    }
}
