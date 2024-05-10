package com.example.proform.model;

public class Test {
    private String idTransporter;
    private String testAlcool;
    private String testReconnaissance;

    public Test() {
        // Default constructor required for calls to DataSnapshot.getValue(Test.class)
    }

    public Test(String idTransporter, String testAlcool, String testReconnaissance) {
        this.idTransporter = idTransporter;
        this.testAlcool = testAlcool;
        this.testReconnaissance = testReconnaissance;
    }

    public String getIdTransporter() {
        return idTransporter;
    }

    public void setIdTransporter(String idTransporter) {
        this.idTransporter = idTransporter;
    }

    public String getTestAlcool() {
        return testAlcool;
    }

    public void setTestAlcool(String testAlcool) {
        this.testAlcool = testAlcool;
    }

    public String getTestReconnaissance() {
        return testReconnaissance;
    }

    public void setTestReconnaissance(String testReconnaissance) {
        this.testReconnaissance = testReconnaissance;
    }
}
