package com.alex.project.exceptions;

import jakarta.ws.rs.core.Response;

public class UserAlreadyExist extends UserException {
    public UserAlreadyExist(String message) {
        super(message);
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.CONFLICT;
    }
}
