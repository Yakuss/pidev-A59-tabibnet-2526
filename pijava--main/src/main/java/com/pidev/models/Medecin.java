package com.pidev.models;

import com.pidev.constant.Governorate;
import com.pidev.constant.Specialty;

import java.time.LocalDateTime;

/**
 * Medecin (Doctor) entity - inherits from BaseUser.
 */
public class Medecin extends BaseUser {
    private String phoneNumber;
    private Specialty specialty;
    private String cin;
    private String address;
    private Governorate governorate;
    private String education;
    private String experience;
    private boolean isVerified;
    private Double aiAverageScore;
    private LocalDateTime aiScoreUpdatedAt;
    private Double averageRating;      // Average rating from patient feedback (0.00 to 5.00)
    private Integer totalReviews;      // Total number of reviews/feedback

    public Medecin() {
        super();
        this.averageRating = 0.0;
        this.totalReviews = 0;
    }

    public Medecin(int id, String email, String password, String firstName, String lastName,
                   int age, String gender, boolean isActive, String roles,
                   String phoneNumber, Specialty specialty, String cin, String address,
                   Governorate governorate, String education, String experience,
                   boolean isVerified, Double aiAverageScore) {
        super(id, email, password, firstName, lastName, age, gender, isActive, roles);
        this.phoneNumber = phoneNumber;
        this.specialty = specialty;
        this.cin = cin;
        this.address = address;
        this.governorate = governorate;
        this.education = education;
        this.experience = experience;
        this.isVerified = isVerified;
        this.aiAverageScore = aiAverageScore;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Governorate getGovernorate() { return governorate; }
    public void setGovernorate(Governorate governorate) { this.governorate = governorate; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public Double getAiAverageScore() { return aiAverageScore; }
    public void setAiAverageScore(Double aiAverageScore) { this.aiAverageScore = aiAverageScore; }

    public LocalDateTime getAiScoreUpdatedAt() { return aiScoreUpdatedAt; }
    public void setAiScoreUpdatedAt(LocalDateTime aiScoreUpdatedAt) { this.aiScoreUpdatedAt = aiScoreUpdatedAt; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}
