package com.alex.project.utils;

import com.alex.project.entiies.Role;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class JwtGenerator {

    @ConfigProperty(name = "mp.jwt.private-key.location")
    String privateKeyLocation;

    @ConfigProperty(name ="mp.jwt.verify.issuer")
    String verifyIssuer;

    public String jwtGenerator(String username, Role role) {
        return Jwt.issuer(verifyIssuer)
                .subject(username)
                .groups(role.toString())
                .expiresIn(Duration.ofDays(7))
                .sign(privateKeyLocation);
    }
}
