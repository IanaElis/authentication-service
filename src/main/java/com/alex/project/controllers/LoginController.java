package com.alex.project.controllers;

import com.alex.project.dtos.LoginDto;
import com.alex.project.entiies.User;
import com.alex.project.repositories.UserRepository;
import com.alex.project.services.AuthService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginController {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @Inject
    SessionBootstrapper sessionBootstrapper;

    @POST
    @Path("/login")
    @PermitAll
    public Uni<Response> login(@Valid LoginDto loginDto) {
        return Uni.createFrom().item(() -> {
            User user = authService.authenticate(loginDto);
            return sessionBootstrapper.bootstrap(user, "LOGIN").response();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/logout")
    @PermitAll
    public Uni<Response> logout() {
        return Uni.createFrom().item(() ->
            sessionBootstrapper.clearAllCookies()
        ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/me")
    @Authenticated
    public Uni<Response> checkLogin() {
        return Uni.createFrom().item(() -> {
            Long userId = Long.parseLong(jwt.getClaim("userid").toString());
            var userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            User user = userOpt.get();
            SessionBootstrapResponse body = new SessionBootstrapResponse(
                user.getId(),
                user.getRole().name(),
                user.getUsername(),
                "REFRESH",
                Map.of()
            );
            return Response.ok(body).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/me/role")
    @Authenticated
    public Uni<Response> meRole() {
        return Uni.createFrom().item(() -> {
            Long userId = Long.parseLong(jwt.getClaim("userid").toString());
            var user = userRepository.findByIdOptional(userId);
            if (user.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            var u = user.get();
            return Response.ok(new SessionBootstrapResponse(
                userId,
                u.getRole().name(),
                u.getUsername(),
                "REFRESH",
                Map.of()
            )).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
