package com.alex.project.controllers;

import com.alex.project.entiies.Role;
import com.alex.project.entiies.User;
import com.alex.project.repositories.UserRepository;
import com.alex.project.services.AuthService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/auth/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AdminController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Inject
    UserRepository userRepository;

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @Inject
    SessionBootstrapper sessionBootstrapper;

    public record UserInfo(long id, String username, String role) {}
    public record PasswordChange(String newPassword) {}
    public record EmailChange(String newEmail) {}

    @GET
    @Path("/users")
    @RolesAllowed("ADMIN")
    public Uni<Response> listUsers() {
        return Uni.createFrom().item(() -> {
            List<UserInfo> users = userRepository.listAll().stream()
                    .map(u -> new UserInfo(u.getId(), u.getUsername(), u.getRole().name()))
                    .collect(Collectors.toList());
            return Response.ok(users).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @PUT
    @Path("/users/{id}/password")
    @RolesAllowed("ADMIN")
    public Uni<Response> changePassword(@PathParam("id") long userId, PasswordChange body) {
        return Uni.createFrom().item(() -> {
            if (body.newPassword() == null || !isValidPassword(body.newPassword())) {
                return Response.status(400)
                        .entity(Map.of("error", "Password must be 8+ chars with 1 letter and 1 uppercase"))
                        .build();
            }
            var userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            User user = userOpt.get();
            user.setPassword(BcryptUtil.bcryptHash(body.newPassword()));
            userRepository.persist(user);
            LOG.info("Password changed for user {}", userId);
            return Response.ok(Map.of("success", true)).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @PUT
    @Path("/users/{id}/email")
    @RolesAllowed("ADMIN")
    public Uni<Response> changeEmail(@PathParam("id") long userId, EmailChange body) {
        return Uni.createFrom().item(() -> {
            if (body.newEmail() == null || body.newEmail().isBlank()) {
                return Response.status(400).entity(Map.of("error", "Email required")).build();
            }
            var userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            User user = userOpt.get();
            user.setUsername(body.newEmail());
            userRepository.persist(user);
            LOG.info("Email changed for user {}", userId);
            return Response.ok(Map.of("success", true)).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @DELETE
    @Path("/users/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> deleteUser(@PathParam("id") long userId) {
        return Uni.createFrom().item(() -> {
            var userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            userRepository.delete(userOpt.get());
            LOG.info("User {} deleted", userId);
            return Response.ok(Map.of("success", true)).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/impersonate/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> impersonate(@PathParam("id") long userId) {
        return Uni.createFrom().item(() -> {
            var userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            User adminUser = userRepository.findByIdOptional(
                Long.parseLong(jwt.getClaim("userid").toString())
            ).orElseThrow(() -> new RuntimeException("Admin not found"));

            User targetUser = userOpt.get();
            SessionBootstrapper.BootstrapResult result = sessionBootstrapper.bootstrapWithMetadata(
                targetUser, "IMPERSONATION",
                Map.of("adminUserId", adminUser.getId(), "impersonatedUserId", targetUser.getId())
            );

            NewCookie impMarker = CookiePolicy.sessionCookie("ImpersonationMarker", 3600)
                    .value("true")
                    .build();

            LOG.info("Admin {} impersonating user {}", adminUser.getId(), userId);
            return Response.ok(result.response().getEntity())
                    .cookie(result.cookie())
                    .cookie(impMarker)
                    .build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/impersonate/exit")
    @RolesAllowed("ADMIN")
    public Uni<Response> exitImpersonation() {
        return Uni.createFrom().item(() -> {
            Long userId = Long.parseLong(jwt.getClaim("userid").toString());
            var userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "User not found")).build();
            }
            User adminUser = userOpt.get();
            SessionBootstrapper.BootstrapResult result = sessionBootstrapper.bootstrap(
                adminUser, "LOGIN"
            );

            NewCookie impClear = CookiePolicy.clearCookie("ImpersonationMarker");
            LOG.info("Admin {} exiting impersonation", userId);
            return Response.ok(result.response().getEntity())
                    .cookie(result.cookie())
                    .cookie(impClear)
                    .build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = false;
        boolean hasUpper = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isUpperCase(c)) hasUpper = true;
        }
        return hasLetter && hasUpper;
    }
}
