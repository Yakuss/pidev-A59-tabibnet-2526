package com.pidev.models;

import java.time.LocalDateTime;

public class Patient extends BaseUser {
    private String phoneNumber;
    private String address;
    private LocalDateTime dateOfBirth;
    private boolean hasInsurance;
    private String insuranceNumber;

    public Patient() { super(); }

    public Patient(int id, String email, String password, String firstName, String lastName,
                   int age, String gender, boolean isActive, String roles,
                   String phoneNumber, String address, LocalDateTime dateOfBirth,
                   boolean hasInsurance, String insuranceNumber) {
        super(id, email, password, firstName, lastName, age, gender, isActive, roles);
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.hasInsurance = hasInsurance;
        this.insuranceNumber = insuranceNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public boolean isHasInsurance() { return hasInsurance; }
    public void setHasInsurance(boolean hasInsurance) { this.hasInsurance = hasInsurance; }

    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }
}