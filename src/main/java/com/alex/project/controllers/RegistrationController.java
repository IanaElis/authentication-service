package com.alex.project.controllers;

import com.alex.project.dtos.RegistrationDto;
import com.alex.project.entiies.Role;
import com.alex.project.services.AuthService;
import io.quarkus.security.Authenticated;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/auth/signup")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);
    @Inject
    AuthService authService;

    @Inject
    JWTParser parser;

    @POST
    @Path("/user")
    @PermitAll
    public Response register(@Valid RegistrationDto registrationDto) throws ParseException {
        try {
            String token = authService.registration(registrationDto);

            NewCookie jwtCookie = new NewCookie.Builder("JwtToken")
                    .value(token)
                    .path("/")
                    .httpOnly(true)
                    .secure(false)
                    .maxAge(3600)
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();


            return Response
                    .ok(parser.parse(token).getClaim("userid").toString())
                    .cookie(jwtCookie).build();

        } catch (Exception e) {
            log.error("Error while registering user", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/moderator")
    @RolesAllowed("ADMIN")
    public Response registerModerator(@Valid RegistrationDto registrationDto){
        try {
            String token = authService.registrationModerator(registrationDto);

            NewCookie jwtCookie = new NewCookie.Builder("JwtToken")
                    .value(token)
                    .path("/")
                    .httpOnly(true)
                    .secure(false)
                    .maxAge(3600)
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();

            return Response
                    .ok(parser.parse(token).getClaim("userid").toString())
                    .cookie(jwtCookie).build();
        } catch (Exception e) {
            log.error("Error while registering moderator", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/admin")
    @RolesAllowed("INNER")
    public Response registerAdmin(@Valid RegistrationDto registrationDto){
        try {
            String token = authService.registrationAdmin(registrationDto);

            NewCookie jwtCookie = new NewCookie.Builder("JwtToken")
                    .value(token)
                    .path("/")
                    .httpOnly(true)
                    .secure(false)
                    .maxAge(3600)
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();

            return Response
                    .ok(parser.parse(token).getClaim("userid").toString())
                    .cookie(jwtCookie).build();
        } catch (Exception e) {
            log.error("Error while registering admin", e);
            return Response.serverError().build();
        }
    }


}
