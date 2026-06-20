package com.alex.project.controllers;

import com.alex.project.entiies.User;
import com.alex.project.repositories.UserRepository;
import com.alex.project.utils.JwtService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@ApplicationScoped
public class SessionBootstrapper {
    @Inject
    JwtService jwtService;

    @Inject
    UserRepository userRepository;

    public BootstrapResult bootstrap(User user, String sessionType) {
        String token = jwtService.jwtGenerator(user.getUsername(), user.getRole(), user.getId());
        NewCookie jwtCookie = CookiePolicy.sessionCookie(token, 3600).build();
        SessionBootstrapResponse body = new SessionBootstrapResponse(
            user.getId(),
            user.getRole().name(),
            user.getUsername(),
            sessionType,
            Map.of()
        );
        return new BootstrapResult(token, jwtCookie,
            Response.ok(body).cookie(jwtCookie).build());
    }

    public BootstrapResult bootstrapWithMetadata(User user, String sessionType, Map<String, Object> metadata) {
        String token = jwtService.jwtGenerator(user.getUsername(), user.getRole(), user.getId());
        NewCookie jwtCookie = CookiePolicy.sessionCookie(token, 3600).build();
        SessionBootstrapResponse body = new SessionBootstrapResponse(
            user.getId(),
            user.getRole().name(),
            user.getUsername(),
            sessionType,
            metadata
        );
        return new BootstrapResult(token, jwtCookie,
            Response.ok(body).cookie(jwtCookie).build());
    }

    public Response clearAllCookies() {
        NewCookie jwtClear = CookiePolicy.clearCookie("JwtToken");
        NewCookie impClear = CookiePolicy.clearCookie("ImpersonationMarker");
        return Response.ok(Map.of("sessionType", "LOGOUT"))
            .cookie(jwtClear)
            .cookie(impClear)
            .build();
    }

    public record BootstrapResult(String token, NewCookie cookie, Response response) {}
}
