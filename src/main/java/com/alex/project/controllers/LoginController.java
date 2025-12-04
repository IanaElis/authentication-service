package com.alex.project.controllers;

import com.alex.project.dtos.LoginDto;
import com.alex.project.repositories.UserRepository;
import com.alex.project.services.AuthService;
import com.alex.project.utils.JwtGenerator;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;


@Path("/auth/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class LoginController {

    @Inject
    AuthService authService;

    @POST
    @Path("/")
    @PermitAll
    public Response login(@Valid LoginDto loginDto) {
        String token = authService.login(loginDto);
        return Response.ok(token).build();
    }

    @GET
    @Path("/google")
    @Authenticated
    public Response login() {
        throw new NotAuthorizedException("oidc");
    }
}
