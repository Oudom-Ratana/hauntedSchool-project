package com.khmerspirit.admin.model;

/**
 * Model representing admin login credentials stored in config/admin.json.
 */
public class AdminUser {

    private String username;
    private String password;
    private String role;

    public AdminUser() {
    }

    public AdminUser(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
