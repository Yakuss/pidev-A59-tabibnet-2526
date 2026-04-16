package com.pidev.utils;

import com.pidev.models.Admin;
import com.pidev.services.AdminService;

public class SetupAdmin {
    public static void main(String[] args) {
        AdminService adminService = new AdminService();
        try {
            // Check if any admin exists
            if (adminService.findAll().isEmpty()) {
                Admin admin = new Admin();
                admin.setEmail("admin@example.com");
                admin.setPassword("admin123"); // In production, hash this!
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setAge(30);
                admin.setGender("Male");
                admin.setActive(true);
                admin.setName("System Administrator");
                // Roles will be set automatically by AdminService.add() as ["ROLE_ADMIN"]

                adminService.add(admin);
                System.out.println("✅ Default admin created successfully!");
                System.out.println("   Email: admin@example.com");
                System.out.println("   Password: admin123");
            } else {
                System.out.println("ℹ️ Admin already exists in database.");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to create admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}