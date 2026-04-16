package com.pidev.services;

import com.pidev.models.Patient;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Patient entity using JDBC.
 * Works with the shared Symfony database (user table with dtype discriminator).
 */
public class PatientService implements IService<Patient> {

    private final Connection conn = DataSource.getInstance().getConnection();

    @Override
    public void add(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (email, password, first_name, last_name, age, gender, is_active, roles, " +
                     "phone_number, address, date_of_birth, has_insurance, insurance_number) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, patient.getEmail());
        ps.setString(2, patient.getPassword());
        ps.setString(3, patient.getFirstName());
        ps.setString(4, patient.getLastName());
        ps.setInt(5, patient.getAge());
        ps.setString(6, patient.getGender());
        ps.setBoolean(7, patient.isActive());
        ps.setString(8, patient.getRoles() != null ? patient.getRoles() : "[\"ROLE_PATIENT\"]");
        ps.setString(9, patient.getPhoneNumber());
        ps.setString(10, patient.getAddress());
        ps.setTimestamp(11, patient.getDateOfBirth() != null ?
                Timestamp.valueOf(patient.getDateOfBirth()) : null);
        ps.setBoolean(12, patient.isHasInsurance());
        ps.setString(13, patient.getInsuranceNumber());
        ps.executeUpdate();
        System.out.println("✅ Patient added to 'patients'!");
    }

    @Override
    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET email=?, first_name=?, last_name=?, age=?, gender=?, is_active=?, " +
                     "phone_number=?, address=?, date_of_birth=?, has_insurance=?, insurance_number=? " +
                     "WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, patient.getEmail());
        ps.setString(2, patient.getFirstName());
        ps.setString(3, patient.getLastName());
        ps.setInt(4, patient.getAge());
        ps.setString(5, patient.getGender());
        ps.setBoolean(6, patient.isActive());
        ps.setString(7, patient.getPhoneNumber());
        ps.setString(8, patient.getAddress());
        ps.setTimestamp(9, patient.getDateOfBirth() != null ?
                Timestamp.valueOf(patient.getDateOfBirth()) : null);
        ps.setBoolean(10, patient.isHasInsurance());
        ps.setString(11, patient.getInsuranceNumber());
        ps.setInt(12, patient.getId());
        ps.executeUpdate();
        System.out.println("✅ Patient updated in 'patients'!");
    }

    /** Updates only the password column for a given patient id. */
    public void updatePassword(int id, String hashedPassword) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE patients SET password=? WHERE id=?");
        ps.setString(1, hashedPassword);
        ps.setInt(2, id);
        ps.executeUpdate();
        System.out.println("✅ Patient password updated!");
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Patient deleted from 'patients'!");
    }

    @Override
    public List<Patient> getAll() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY id DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            patients.add(mapResultSet(rs));
        }
        return patients;
    }

    @Override
    public Patient getById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    private Patient mapResultSet(ResultSet rs) throws SQLException {
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
        p.setPhoneNumber(rs.getString("phone_number"));
        p.setAddress(rs.getString("address"));
        Timestamp dob = rs.getTimestamp("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDateTime());
        p.setHasInsurance(rs.getBoolean("has_insurance"));
        p.setInsuranceNumber(rs.getString("insurance_number"));
        return p;
    }
}
