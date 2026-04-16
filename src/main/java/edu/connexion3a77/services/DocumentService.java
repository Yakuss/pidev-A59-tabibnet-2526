package edu.connexion3a77.services;

import edu.connexion3a77.entities.Document;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocumentService implements IService<Document> {

    @Override
    public void add(Document document) throws SQLException {
        String requete = "INSERT INTO document (nom_fichier, chemin_fichier) VALUES (?, ?)";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, document.getNomFichier());
            pst.setString(2, document.getCheminFichier());
            pst.executeUpdate();
            System.out.println("Document ajouté !");
        }
    }

    @Override
    public void update(Document document) throws SQLException {
        String requete = "UPDATE document SET nom_fichier = ?, chemin_fichier = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, document.getNomFichier());
            pst.setString(2, document.getCheminFichier());
            pst.setInt(3, document.getId());
            pst.executeUpdate();
            System.out.println("Document modifié !");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM document WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Document supprimé !");
        }
    }

    @Override
    public Document findById(int id) throws SQLException {
        String requete = "SELECT * FROM document WHERE id = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Document> findAll() throws SQLException {
        List<Document> list = new ArrayList<>();
        String requete = "SELECT * FROM document";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToDocument(rs));
            }
        }
        return list;
    }

    public List<Document> findAllWithPdfContent() throws SQLException {
        List<Document> list = new ArrayList<>();
        String requete = "SELECT * FROM document WHERE type = 'pdf' OR type = 'application/pdf'";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                list.add(mapResultSetToDocument(rs));
            }
        }
        return list;
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setId(rs.getInt("id"));
        d.setNomFichier(rs.getString("nom_fichier"));
        d.setCheminFichier(rs.getString("chemin_fichier"));
        // d.setType(rs.getString("type")); - Si le type était ajouté à l'entité. Je le garde commenté au cas où.
        return d;
    }
}
