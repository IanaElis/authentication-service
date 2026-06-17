package com.alex.project.controllers;

import com.alex.project.clients.UserServiceClient;
import com.alex.project.dtos.user.FieldDto;
import com.alex.project.dtos.user.ProfileDto;
import com.alex.project.dtos.user.SearchUser;
import com.alex.project.dtos.user.SpecialtyDto;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/user")
@ApplicationScoped
@Authenticated
public class UserGatewayController {

    @Inject
    @RestClient
    UserServiceClient client;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/profiles/get")
    public Uni<Response> getProfile(@Valid SearchUser email) {
        // TODO: resolve userId from email first, then call getProfile
        return client.getAllFields(); // placeholder
    }

    @PUT
    @Path("/profiles/update/{userId}")
    public Uni<Response> updateProfile(
            @PathParam("userId") long userId,
            @Valid ProfileDto profile
    ) {
        return client.updateProfile(userId, profile);
    }

    @GET
    @Path("/field/all")
    public Uni<Response> getAllFields() {
        return client.getAllFields();
    }

    @POST
    @Path("/field/new")
    @RolesAllowed("MODERATOR")
    public Uni<Response> addNewField(@Valid FieldDto dto) {
        return client.addNewField(dto);
    }

    @GET
    @Path("/specialty/all")
    public Uni<Response> getAllSpecialty() {
        return client.getAllSpecialty();
    }

    @POST
    @Path("/specialty/new")
    @RolesAllowed("ADMIN")
    public Uni<Response> addNewSpecialty(@Valid SpecialtyDto dto) {
        return client.addNewSpecialty(dto);
    }
}
