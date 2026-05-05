package com.pidev.models;

/**
 * Admin entity - inherits from BaseUser.
 */
public class Admin extends BaseUser {
    private String name;

    public Admin() {
        super();
    }

    public Admin(int id, String email, String password, String firstName, String lastName,
                 int age, String gender, boolean isActive, String roles, String name) {
        super(id, email, password, firstName, lastName, age, gender, isActive, roles);
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
