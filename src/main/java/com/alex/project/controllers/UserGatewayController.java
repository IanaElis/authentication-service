package com.alex.project.controllers;

import com.alex.project.clients.UserServiceClient;
import com.alex.project.dtos.user.FieldDto;
import com.alex.project.dtos.user.SearchUser;
import com.alex.project.dtos.user.SpecialtyDto;
import com.alex.project.dtos.user.UserUpdateRequestDto;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
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

    @POST
    @Path("/profiles/get")
    public Uni<Response> getProfile(@Valid SearchUser search) {
        return client.getProfile(search.email(), 0L)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/{userId}/verification-status")
    public Uni<Response> getVerificationStatus(@PathParam("userId") Long userId) {
        return client.getVerificationStatus(userId)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @PUT
    @Path("/{userId}/update-request")
    public Uni<Response> submitUpdateRequest(
            @PathParam("userId") Long userId,
            @Valid UserUpdateRequestDto request) {
        return client.submitUpdateRequest(userId, request)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/departments")
    public Uni<Response> getAllDepartments() {
        return client.getAllDepartments()
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/graduation-groups")
    public Uni<Response> getAllGraduationGroups() {
        return client.getAllGraduationGroups()
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/field/all")
    public Uni<Response> getAllFields() {
        return client.getAllFields()
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/field/new")
    @RolesAllowed("ADMIN")
    public Uni<Response> addNewField(@Valid FieldDto dto) {
        return client.addNewField(dto)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @DELETE
    @Path("/field/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> deleteField(@PathParam("id") Long id) {
        return client.deleteField(id)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("/specialty/all")
    public Uni<Response> getAllSpecialty() {
        return client.getAllSpecialty()
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/specialty/new")
    @RolesAllowed("ADMIN")
    public Uni<Response> addNewSpecialty(@Valid SpecialtyDto dto) {
        return client.addNewSpecialty(dto)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @DELETE
    @Path("/specialty/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> deleteSpecialty(@PathParam("id") Long id) {
        return client.deleteSpecialty(id)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
