package com.alex.project.controllers;

import com.alex.project.dtos.LoginDto;
import com.alex.project.services.AuthService;
import io.quarkus.security.Authenticated;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;


@Path("/auth/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class LoginController {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken token;
    @Inject
    JWTParser parser;


    @POST
    @Path("/")
    @PermitAll
    public Response login(@Valid LoginDto loginDto) throws ParseException {
        String token = authService.login(loginDto);
        JsonWebToken jwt = parser.parse(token);

        String username = jwt.getSubject();
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

//    @GET
//    @Path("/google")
//    @Authenticated
//    public Response login() {
//        throw new NotAuthorizedException("oidc");
//    }
}
