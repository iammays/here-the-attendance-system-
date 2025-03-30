
package com.here.backend.Security.payload.request;

public class PasswordChangeRequest {
    private String name;
    private String currentPassword;
    private String newPassword;

    public String getName() {
        return name;
    }
    public void getName(String username) {
        this.name = username;
    }
    public String getCurrentPassword() {
        return currentPassword;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
