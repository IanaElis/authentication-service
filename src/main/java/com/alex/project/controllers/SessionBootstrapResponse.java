package com.alex.project.controllers;

import java.util.Map;

public record SessionBootstrapResponse(
    Long userId,
    String role,
    String username,
    String sessionType,
    Map<String, Object> metadata
) {}
