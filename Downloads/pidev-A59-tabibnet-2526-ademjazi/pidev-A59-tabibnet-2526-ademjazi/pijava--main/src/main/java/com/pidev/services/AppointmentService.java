package com.pidev.services;

import com.pidev.models.Appointment;
import com.pidev.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Appointment entity using JDBC.
 */
public class AppointmentService implements IService<Appointment> {

    private final Connection conn = DataSource.getInstance().getConnection();

    @Override
    public void add(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointment (date, start_time, duration, status, message, department, " +
                     "patient_id, doctor_id, created_at, reminder_sent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, a.getDate() != null ? Timestamp.valueOf(a.getDate()) : null);
        ps.setTimestamp(2, a.getStartTime() != null ? Timestamp.valueOf(a.getStartTime()) : null);
        ps.setInt(3, a.getDuration());
        ps.setString(4, a.getStatus() != null ? a.getStatus() : "pending");
        ps.setString(5, a.getMessage());
        ps.setString(6, a.getDepartment());
        ps.setInt(7, a.getPatientId());
        ps.setInt(8, a.getDoctorId());
        ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        ps.setBoolean(10, false);
        ps.executeUpdate();
        System.out.println("✅ Appointment added!");
    }

    @Override
    public void update(Appointment a) throws SQLException {
        String sql = "UPDATE appointment SET date=?, start_time=?, duration=?, status=?, message=?, " +
                     "department=?, patient_id=?, doctor_id=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, a.getDate() != null ? Timestamp.valueOf(a.getDate()) : null);
        ps.setTimestamp(2, a.getStartTime() != null ? Timestamp.valueOf(a.getStartTime()) : null);
        ps.setInt(3, a.getDuration());
        ps.setString(4, a.getStatus());
        ps.setString(5, a.getMessage());
        ps.setString(6, a.getDepartment());
        ps.setInt(7, a.getPatientId());
        ps.setInt(8, a.getDoctorId());
        ps.setInt(9, a.getId());
        ps.executeUpdate();
        System.out.println("✅ Appointment updated!");
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM appointment WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Appointment deleted!");
    }

    @Override
    public List<Appointment> getAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Patient Inconnu') AS patient_name, " +
                     "COALESCE(CONCAT(d.first_name, ' ', d.last_name), 'Médecin Inconnu') AS doctor_name " +
                     "FROM appointment a " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN medecins d ON a.doctor_id = d.id " +
                     "ORDER BY a.id DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public Appointment getById(int id) throws SQLException {
        String sql = "SELECT a.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Patient Inconnu') AS patient_name, " +
                     "COALESCE(CONCAT(d.first_name, ' ', d.last_name), 'Médecin Inconnu') AS doctor_name " +
                     "FROM appointment a " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN medecins d ON a.doctor_id = d.id " +
                     "WHERE a.id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    private Appointment mapResultSet(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        Timestamp date = rs.getTimestamp("date");
        if (date != null) a.setDate(date.toLocalDateTime());
        Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) a.setStartTime(startTime.toLocalDateTime());
        a.setDuration(rs.getInt("duration"));
        a.setStatus(rs.getString("status"));
        a.setMessage(rs.getString("message"));
        a.setDepartment(rs.getString("department"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toLocalDateTime());
        a.setReminderSent(rs.getBoolean("reminder_sent"));
        try {
            a.setPatientName(rs.getString("patient_name"));
            a.setDoctorName(rs.getString("doctor_name"));
        } catch (SQLException ignored) {}
        return a;
    }

    public List<Appointment> getAppointmentsByPatientAndDoctor(Integer patientId, Integer doctorId) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Patient Inconnu') AS patient_name, " +
                     "COALESCE(CONCAT(d.first_name, ' ', d.last_name), 'Médecin Inconnu') AS doctor_name " +
                     "FROM appointment a " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN medecins d ON a.doctor_id = d.id " +
                     "WHERE a.patient_id=? AND a.doctor_id=? " +
                     "ORDER BY a.date DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, patientId);
        ps.setInt(2, doctorId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }
    public List<Appointment> getAppointmentsByDoctor(Integer doctorId) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "COALESCE(CONCAT(p.first_name, ' ', p.last_name), 'Patient Inconnu') AS patient_name, " +
                     "COALESCE(CONCAT(d.first_name, ' ', d.last_name), 'Médecin Inconnu') AS doctor_name " +
                     "FROM appointment a " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN medecins d ON a.doctor_id = d.id " +
                     "WHERE a.doctor_id=? " +
                     "ORDER BY a.date DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, doctorId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }
}
