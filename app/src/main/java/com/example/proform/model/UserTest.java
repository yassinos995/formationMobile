package com.example.proform.model;

public class UserTest {
    private String name;
    private String email;
    private String idTransporter;
    private String testAlcool;
    private String testReconnaissance;

    public UserTest() {
        // Default constructor required for calls to DataSnapshot.getValue(UserTest.class)
    }

    public UserTest(String name, String email, String idTransporter, String testAlcool, String testReconnaissance) {
        this.name = name;
        this.email = email;
        this.idTransporter = idTransporter;
        this.testAlcool = testAlcool;
        this.testReconnaissance = testReconnaissance;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getIdTransporter() {
        return idTransporter;
    }

    public String getTestAlcool() {
        return testAlcool;
    }

    public String getTestReconnaissance() {
        return testReconnaissance;
    }
}
