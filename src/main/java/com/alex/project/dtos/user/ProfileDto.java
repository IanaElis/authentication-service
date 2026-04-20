package com.alex.project.dtos.user;

import jakarta.validation.constraints.*;

public class ProfileDto {

    private String urlImage;
    private Long facultyNumber;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @Email
    private String email;

    @Pattern(
            regexp = "^\\+[1-9][0-9]{9,14}$",
            message = "Invalid phone number format"
    )
    private String phoneNumber;
    private String specialty;
    private String field;
    private String form;
    private String country;
    private String workplace;
    private String position;


    public ProfileDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Long getFacultyNumber() {
        return facultyNumber;
    }

    public void setFacultyNumber(Long facultyNumber) {
        this.facultyNumber = facultyNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
