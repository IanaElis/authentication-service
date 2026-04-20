package com.alex.project.utils.advice;

import com.alex.project.exceptions.UserAlreadyExist;
import com.alex.project.exceptions.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionAdvice implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if(exception instanceof SecurityException ex){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Password not matches!")
                    .build();
        }
        if(exception instanceof UserAlreadyExist ex){
            return Response.status(ex.getStatus())
                    .entity(ex.getMessage())
                    .build();
        }
        if(exception instanceof UserNotFoundException ex){
            return Response.status(ex.getStatus())
                    .entity(ex.getMessage())
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();    }
}
