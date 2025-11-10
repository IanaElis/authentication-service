package com.alex.project.controllers;

import com.alex.project.dtos.LoginDto;
import com.alex.project.repositories.UserRepository;
import com.alex.project.services.AuthService;
import com.alex.project.utils.JwtGenerator;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class LoginController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginDto loginDto) {
        String token = authService.login(loginDto);
        return Response.ok(token).build();
    }
}
