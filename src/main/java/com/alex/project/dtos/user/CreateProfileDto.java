package com.alex.project.dtos.user;

public class CreateProfileDto {
    public String email;
    public Long id;

    public CreateProfileDto(String email, Long id) {
        this.email = email;this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
