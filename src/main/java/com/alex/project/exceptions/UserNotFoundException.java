package com.alex.project.exceptions;

import com.alex.project.entiies.User;
import jakarta.ws.rs.core.Response;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message);
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.NOT_FOUND;
    }
}
