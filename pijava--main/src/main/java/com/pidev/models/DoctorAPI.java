package com.pidev.models;

/**
 * Model representing a doctor from the Flask API
 * This is separate from the Medecin model as it comes from external API
 */
public class DoctorAPI {
    private String name;
    private String specialty;
    private String governorate;
    private String address;
    private String phone;
    private String email;
    private String mode; // "Médecin de Libre Pratique"
    
    // Additional fields that might come from API
    private String qualification;
    private String experience;
    private String languages;

    public DoctorAPI() {
    }

    public DoctorAPI(String name, String specialty, String governorate) {
        this.name = name;
        this.specialty = specialty;
        this.governorate = governorate;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    /**
     * Get initials for avatar display
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    @Override
    public String toString() {
        return "DoctorAPI{" +
                "name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", governorate='" + governorate + '\'' +
                '}';
    }
}
