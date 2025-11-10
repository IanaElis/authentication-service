package com.alex.project.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public abstract Response.Status getStatus();
}
