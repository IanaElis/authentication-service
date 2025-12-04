package com.alex.project.controllers;

import com.alex.project.entiies.Role;
import com.alex.project.utils.JwtGenerator;
import io.quarkus.oidc.UserInfo;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/auth/callback")
public class WelcomeResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    JwtGenerator jwtGenerator;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state) {
        UserInfo userInfo = identity.getAttribute("userinfo");

        String email = userInfo.getEmail();

        String token = jwtGenerator.jwtGenerator(email, Role.USER);

        NewCookie jwtCookie = new NewCookie.Builder("JwtToken")
                .value(token)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(3600)
                .sameSite(NewCookie.SameSite.LAX)
                .build();

        return Response.seeOther(URI.create("http://localhost:5173")).cookie(jwtCookie).build();
    }
}
