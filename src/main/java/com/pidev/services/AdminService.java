package com.pidev.services;

import com.pidev.models.Admin;
import com.pidev.utils.DataSource;
import com.pidev.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService implements IService<Admin> {

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
    public void add(Admin admin) throws Exception {
        if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
            admin.setRoles(formatRoles("ROLE_ADMIN"));
        }
        String hashedPassword = PasswordUtils.hashPassword(admin.getPassword());

        String sql = "INSERT INTO admins (email, password, first_name, last_name, age, gender, is_active, roles, name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, admin.getEmail());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, admin.getFirstName());
            stmt.setString(4, admin.getLastName());
            stmt.setInt(5, admin.getAge());
            stmt.setString(6, admin.getGender());
            stmt.setBoolean(7, admin.isActive());
            stmt.setString(8, admin.getRoles());
            stmt.setString(9, admin.getName());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    admin.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Admin admin) throws Exception {
        if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
            admin.setRoles(formatRoles("ROLE_ADMIN"));
        }
        //String hashedPassword = PasswordUtils.hashPassword(admin.getPassword());
        // ✅ Only hash if the password is not already a SHA‑256 hash
        String passwordToStore = admin.getPassword();
        if (!PasswordUtils.isHashed(passwordToStore)) {
            passwordToStore = PasswordUtils.hashPassword(passwordToStore);
        }


        String sql = "UPDATE admins SET email=?, password=?, first_name=?, last_name=?, age=?, gender=?, is_active=?, roles=?, name=? WHERE id=?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getEmail());
            stmt.setString(2, passwordToStore);
            stmt.setString(3, admin.getFirstName());
            stmt.setString(4, admin.getLastName());
            stmt.setInt(5, admin.getAge());
            stmt.setString(6, admin.getGender());
            stmt.setBoolean(7, admin.isActive());
            stmt.setString(8, admin.getRoles());
            stmt.setString(9, admin.getName());
            stmt.setInt(10, admin.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM admins WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Admin findById(int id) throws Exception {
        String sql = "SELECT * FROM admins WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAdmin(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Admin> findAll() throws Exception {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admins";
        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                admins.add(mapAdmin(rs));
            }
        }
        return admins;
    }

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setEmail(rs.getString("email"));
        admin.setPassword(rs.getString("password"));
        admin.setFirstName(rs.getString("first_name"));
        admin.setLastName(rs.getString("last_name"));
        admin.setAge(rs.getInt("age"));
        admin.setGender(rs.getString("gender"));
        admin.setActive(rs.getBoolean("is_active"));
        admin.setRoles(rs.getString("roles"));
        admin.setName(rs.getString("name"));
        admin.setDiscriminator("admin");
        return admin;
    }
}