package com.here.backend.Security.payload.request;

public class PasswordValidationRequest {

    private String name;
    private String password;

    // Getters and setters

    public String getName() {
        return name;
    }

    public void getName(String username) {
        this.name = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
