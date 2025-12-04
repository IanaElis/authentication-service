package com.alex.project.controllers;

import io.quarkus.oidc.UserInfo;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.security.User;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import static org.eclipse.microprofile.jwt.Claims.email;

@Path("/auth/callback")
public class WelcomeResource {

    @Inject
    SecurityIdentity identity;

    @GET
    public String welcome() {
        UserInfo userInfo = identity.getAttribute("userinfo");

        String email = userInfo.getEmail();

        if (email == null) {
            email = (String) identity.getAttribute("preferred_username");
        }

        if (email == null) {
            email = identity.getPrincipal().getName();
        }

        return "Привет, " + email + "!";
    }
}
