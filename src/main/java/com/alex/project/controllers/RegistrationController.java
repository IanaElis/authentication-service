package com.alex.project.controllers;

import com.alex.project.dtos.LoginDto;
import com.alex.project.dtos.RegistrationDto;
import com.alex.project.services.AuthService;
import com.alex.project.utils.JwtGenerator;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class RegistrationController {

    @Inject
    AuthService authService;

    @POST
    @Path("/signup")
    @PermitAll
    public Response register(@Valid RegistrationDto registrationDto) {
        String token = authService.registration(registrationDto);
        NewCookie jwtCookie = new NewCookie.Builder("JwtToken")
                .value(token)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(3600)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
        return Response.ok().cookie(jwtCookie).build();
    }
}
