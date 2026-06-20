package com.alex.project.dtos.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserUpdateRequestDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String middleName;

    @NotBlank
    private String photoKey;

    @NotBlank
    private String phone;

    @NotBlank
    private String specialty;

    @NotNull
    private Long departmentId;

    @NotNull
    private Long graduationGroupId;

    private Long fieldId;

    @NotNull
    private String studyForm;

    @NotBlank
    private String country;

    private String linkedinUrl;

    private String currentWorkPlace;

    private String currentPosition;

    public UserUpdateRequestDto() {
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getPhotoKey() { return photoKey; }
    public void setPhotoKey(String photoKey) { this.photoKey = photoKey; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getGraduationGroupId() { return graduationGroupId; }
    public void setGraduationGroupId(Long graduationGroupId) { this.graduationGroupId = graduationGroupId; }
    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }
    public String getStudyForm() { return studyForm; }
    public void setStudyForm(String studyForm) { this.studyForm = studyForm; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getCurrentWorkPlace() { return currentWorkPlace; }
    public void setCurrentWorkPlace(String currentWorkPlace) { this.currentWorkPlace = currentWorkPlace; }
    public String getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }
}
