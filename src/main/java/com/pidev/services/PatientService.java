package com.pidev.services;

import com.pidev.models.Patient;
import com.pidev.utils.DataSource;
import com.pidev.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientService implements IService<Patient> {

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
    public void add(Patient patient) throws Exception {
        if (patient.getRoles() == null || patient.getRoles().isEmpty()) {
            patient.setRoles(formatRoles("ROLE_PATIENT"));
        }
        String hashedPassword = PasswordUtils.hashPassword(patient.getPassword());


        String sql = "INSERT INTO patients (email, password, first_name, last_name, age, gender, is_active, roles, " +
                "phone_number, address, date_of_birth, has_insurance, insurance_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int idx = 1;
            stmt.setString(idx++, patient.getEmail());
            stmt.setString(idx++, hashedPassword);
            stmt.setString(idx++, patient.getFirstName());
            stmt.setString(idx++, patient.getLastName());
            stmt.setInt(idx++, patient.getAge());
            stmt.setString(idx++, patient.getGender());
            stmt.setBoolean(idx++, patient.isActive());
            stmt.setString(idx++, patient.getRoles());

            if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
                stmt.setString(idx++, patient.getPhoneNumber());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
                stmt.setString(idx++, patient.getAddress());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            if (patient.getDateOfBirth() != null) {
                stmt.setTimestamp(idx++, Timestamp.valueOf(patient.getDateOfBirth()));
            } else {
                stmt.setNull(idx++, Types.TIMESTAMP);
            }

            stmt.setBoolean(idx++, patient.isHasInsurance());

            if (patient.getInsuranceNumber() != null && !patient.getInsuranceNumber().isEmpty()) {
                stmt.setString(idx++, patient.getInsuranceNumber());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Patient patient) throws Exception {
        if (patient.getRoles() == null || patient.getRoles().isEmpty()) {
            patient.setRoles(formatRoles("ROLE_PATIENT"));
        }

        // ✅ Only hash if the password is not already a SHA‑256 hash
        String passwordToStore = patient.getPassword();
        if (!PasswordUtils.isHashed(passwordToStore)) {
            passwordToStore = PasswordUtils.hashPassword(passwordToStore);
        }

        String sql = "UPDATE patients SET email=?, password=?, first_name=?, last_name=?, age=?, gender=?, is_active=?, roles=?, " +
                "phone_number=?, address=?, date_of_birth=?, has_insurance=?, insurance_number=? WHERE id=?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int idx = 1;
            stmt.setString(idx++, patient.getEmail());
            stmt.setString(idx++, passwordToStore);   // ← conditional hash
            stmt.setString(idx++, patient.getFirstName());
            stmt.setString(idx++, patient.getLastName());
            stmt.setInt(idx++, patient.getAge());
            stmt.setString(idx++, patient.getGender());
            stmt.setBoolean(idx++, patient.isActive());
            stmt.setString(idx++, patient.getRoles());

            if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
                stmt.setString(idx++, patient.getPhoneNumber());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
                stmt.setString(idx++, patient.getAddress());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            if (patient.getDateOfBirth() != null) {
                stmt.setTimestamp(idx++, Timestamp.valueOf(patient.getDateOfBirth()));
            } else {
                stmt.setNull(idx++, Types.TIMESTAMP);
            }

            stmt.setBoolean(idx++, patient.isHasInsurance());

            if (patient.getInsuranceNumber() != null && !patient.getInsuranceNumber().isEmpty()) {
                stmt.setString(idx++, patient.getInsuranceNumber());
            } else {
                stmt.setNull(idx++, Types.VARCHAR);
            }

            stmt.setInt(idx++, patient.getId());
            stmt.executeUpdate();
        }
    }
    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM patients WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Patient findById(int id) throws Exception {
        String sql = "SELECT * FROM patients WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPatient(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Patient> findAll() throws Exception {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(mapPatient(rs));
            }
        }
        return patients;
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setEmail(rs.getString("email"));
        p.setPassword(rs.getString("password"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setActive(rs.getBoolean("is_active"));
        p.setRoles(rs.getString("roles"));
        p.setDiscriminator("patient");
        p.setPhoneNumber(rs.getString("phone_number"));
        p.setAddress(rs.getString("address"));
        Timestamp dob = rs.getTimestamp("date_of_birth");
        p.setDateOfBirth(dob != null ? dob.toLocalDateTime() : null);
        p.setHasInsurance(rs.getBoolean("has_insurance"));
        p.setInsuranceNumber(rs.getString("insurance_number"));
        return p;
    }
}