package com.alex.project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationDto {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public RegistrationDto() {}

    public RegistrationDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isValidPassword() {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = false, hasUpper = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isUpperCase(c)) hasUpper = true;
        }
        return hasLetter && hasUpper;
    }
}
