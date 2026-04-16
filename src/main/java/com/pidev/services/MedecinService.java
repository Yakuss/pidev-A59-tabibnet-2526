package com.pidev.services;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;
import com.pidev.models.Medecin;
import com.pidev.utils.DataSource;
import com.pidev.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedecinService implements IService<Medecin> {

    private String formatRoles(String... roles) {
        if (roles == null || roles.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < roles.length; i++) {
            sb.append("\"").append(roles[i]).append("\"");
            if (i < roles.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void add(Medecin medecin) throws Exception {
        if (medecin.getRoles() == null || medecin.getRoles().isEmpty()) {
            medecin.setRoles(formatRoles("ROLE_MEDECIN"));
        }
        String hashedPassword = PasswordUtils.hashPassword(medecin.getPassword());

        String sql = "INSERT INTO medecins (email, password, first_name, last_name, age, gender, is_active, roles, " +
                "phone_number, specialty, cin, address, governorate, education, experience, is_verified, consultation_fee) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int idx = 1;
            stmt.setString(idx++, medecin.getEmail());
            stmt.setString(idx++, hashedPassword);
            stmt.setString(idx++, medecin.getFirstName());
            stmt.setString(idx++, medecin.getLastName());
            stmt.setInt(idx++, medecin.getAge());
            stmt.setString(idx++, medecin.getGender());
            stmt.setBoolean(idx++, medecin.isActive());
            stmt.setString(idx++, medecin.getRoles());
            stmt.setString(idx++, medecin.getPhoneNumber());
            stmt.setString(idx++, medecin.getSpecialtyName()); // stores enum constant name
            stmt.setString(idx++, medecin.getCin());
            stmt.setString(idx++, medecin.getAddress());
            stmt.setString(idx++, medecin.getGovernorateName()); // stores enum constant name

            if (medecin.getEducation() != null && !medecin.getEducation().isEmpty()) {
                stmt.setString(idx++, medecin.getEducation());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            if (medecin.getExperience() != null && !medecin.getExperience().isEmpty()) {
                stmt.setString(idx++, medecin.getExperience());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            stmt.setBoolean(idx++, medecin.isVerified());

            if (medecin.getConsultationFee() != null) {
                stmt.setDouble(idx++, medecin.getConsultationFee());
            } else {
                stmt.setNull(idx++, Types.DOUBLE);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    medecin.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Medecin medecin) throws Exception {
        if (medecin.getRoles() == null || medecin.getRoles().isEmpty()) {
            medecin.setRoles(formatRoles("ROLE_MEDECIN"));
        }

        // ✅ Only hash if the password is not already a SHA‑256 hash
        String passwordToStore = medecin.getPassword();
        if (!PasswordUtils.isHashed(passwordToStore)) {
            passwordToStore = PasswordUtils.hashPassword(passwordToStore);
        }

        String sql = "UPDATE medecins SET email=?, password=?, first_name=?, last_name=?, age=?, gender=?, is_active=?, roles=?, " +
                "phone_number=?, specialty=?, cin=?, address=?, governorate=?, education=?, experience=?, is_verified=?, consultation_fee=? " +
                "WHERE id=?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int idx = 1;
            stmt.setString(idx++, medecin.getEmail());
            stmt.setString(idx++, passwordToStore);   // ← uses the conditional hash
            stmt.setString(idx++, medecin.getFirstName());
            stmt.setString(idx++, medecin.getLastName());
            stmt.setInt(idx++, medecin.getAge());
            stmt.setString(idx++, medecin.getGender());
            stmt.setBoolean(idx++, medecin.isActive());
            stmt.setString(idx++, medecin.getRoles());
            stmt.setString(idx++, medecin.getPhoneNumber());
            stmt.setString(idx++, medecin.getSpecialtyName());
            stmt.setString(idx++, medecin.getCin());
            stmt.setString(idx++, medecin.getAddress());
            stmt.setString(idx++, medecin.getGovernorateName());

            if (medecin.getEducation() != null && !medecin.getEducation().isEmpty()) {
                stmt.setString(idx++, medecin.getEducation());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            if (medecin.getExperience() != null && !medecin.getExperience().isEmpty()) {
                stmt.setString(idx++, medecin.getExperience());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            stmt.setBoolean(idx++, medecin.isVerified());

            if (medecin.getConsultationFee() != null) {
                stmt.setDouble(idx++, medecin.getConsultationFee());
            } else {
                stmt.setNull(idx++, Types.DOUBLE);
            }

            stmt.setInt(idx++, medecin.getId());
            stmt.executeUpdate();
        }
    }
    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM medecins WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Medecin findById(int id) throws Exception {
        String sql = "SELECT * FROM medecins WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapMedecin(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Medecin> findAll() throws Exception {
        List<Medecin> medecins = new ArrayList<>();
        String sql = "SELECT * FROM medecins";
        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                medecins.add(mapMedecin(rs));
            }
        }
        return medecins;
    }

    private Medecin mapMedecin(ResultSet rs) throws SQLException {
        Medecin m = new Medecin();
        m.setId(rs.getInt("id"));
        m.setEmail(rs.getString("email"));
        m.setPassword(rs.getString("password")); // stored hash
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        m.setAge(rs.getInt("age"));
        m.setGender(rs.getString("gender"));
        m.setActive(rs.getBoolean("is_active"));
        m.setRoles(rs.getString("roles"));
        m.setDiscriminator("medecin");
        m.setPhoneNumber(rs.getString("phone_number"));

        // DB stores enum constant names (e.g., "BIOLOGIE_MEDICALE")
        String specialtyStr = rs.getString("specialty");
        if (specialtyStr != null && !specialtyStr.isEmpty()) {
            try {
                m.setSpecialty(Specialty.valueOf(specialtyStr));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown specialty constant: " + specialtyStr);
            }
        }

        m.setCin(rs.getString("cin"));
        m.setAddress(rs.getString("address"));

        String govStr = rs.getString("governorate");
        if (govStr != null && !govStr.isEmpty()) {
            try {
                m.setGovernorate(Governorate.valueOf(govStr));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown governorate constant: " + govStr);
            }
        }

        m.setEducation(rs.getString("education"));
        m.setExperience(rs.getString("experience"));
        m.setVerified(rs.getBoolean("is_verified"));

        double fee = rs.getDouble("consultation_fee");
        if (!rs.wasNull()) {
            m.setConsultationFee(fee);
        }
        return m;
    }
}