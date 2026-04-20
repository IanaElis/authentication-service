package com.alex.project.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SearchUser(@Email @NotBlank String email) {
}
