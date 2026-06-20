package com.alex.project.controllers;

import jakarta.ws.rs.core.NewCookie;

public final class CookiePolicy {
    private CookiePolicy() {}

    public static NewCookie.Builder sessionCookie(String token, int maxAge) {
        return new NewCookie.Builder("JwtToken")
                .value(token)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(maxAge)
                .sameSite(NewCookie.SameSite.LAX);
    }

    public static NewCookie clearCookie(String name) {
        return new NewCookie.Builder(name)
                .value("")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }
}
