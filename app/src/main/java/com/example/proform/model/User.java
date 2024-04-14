package com.example.proform.model;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;
    private String phoneNumber;
    private String poste;
    private String password;
    private boolean isAdmin;
    private String userId;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String phoneNumber, String poste, String password, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.poste = poste;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // Getters and setters
    // Make sure to generate getters and setters for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}