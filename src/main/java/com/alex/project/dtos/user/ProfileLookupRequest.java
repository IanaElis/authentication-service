package com.alex.project.dtos.user;

import jakarta.validation.constraints.Positive;

public record ProfileLookupRequest(@Positive long userId) {}
