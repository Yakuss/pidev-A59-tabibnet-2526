package com.pidev.models;

import com.pidev.constants.Governorate;
import com.pidev.constants.Specialty;

public class Medecin extends BaseUser {
    private String phoneNumber;
    private Specialty specialty;
    private String cin;
    private String address;
    private Governorate governorate;
    private String education;
    private String experience;
    private boolean isVerified;
    private Double consultationFee;

    public Medecin() {
        super();
    }

    public Medecin(int id, String email, String password, String firstName, String lastName,
                   int age, String gender, boolean isActive, String roles,
                   String phoneNumber, Specialty specialty, String cin, String address,
                   Governorate governorate, String education, String experience,
                   boolean isVerified, Double consultationFee) {
        super(id, email, password, firstName, lastName, age, gender, isActive, roles);
        this.phoneNumber = phoneNumber;
        this.specialty = specialty;
        this.cin = cin;
        this.address = address;
        this.governorate = governorate;
        this.education = education;
        this.experience = experience;
        this.isVerified = isVerified;
        this.consultationFee = consultationFee;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Governorate getGovernorate() {
        return governorate;
    }

    public void setGovernorate(Governorate governorate) {
        this.governorate = governorate;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public Double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }

    // Convenience methods for enum string conversion (DB mapping)

    public String getSpecialtyName() {
        return specialty != null ? specialty.name() : null;
    }

    public void setSpecialtyName(String name) {
        this.specialty = name != null ? Specialty.valueOf(name) : null;
    }

    public String getGovernorateName() {
        return governorate != null ? governorate.name() : null;
    }

    public void setGovernorateName(String name) {
        this.governorate = name != null ? Governorate.valueOf(name) : null;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + (specialty != null ? specialty.getDisplayName() : "No specialty") + ")";
    }
}