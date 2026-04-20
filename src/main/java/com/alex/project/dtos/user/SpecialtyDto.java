package com.alex.project.dtos.user;

import jakarta.validation.constraints.NotBlank;

public record SpecialtyDto(@NotBlank String name) {
}
