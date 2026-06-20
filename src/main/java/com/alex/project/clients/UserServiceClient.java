package com.alex.project.clients;

import com.alex.project.dtos.user.*;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("/api/v1/users")
@RegisterRestClient(configKey = "user-service")
public interface UserServiceClient {

    @POST
    Uni<Response> createProfile(CreateProfileDto request);

    @GET
    @Path("/profile/by-email/{email}")
    Uni<Response> getProfile(@PathParam("email") String email, @QueryParam("requesterId") long requesterId);

    @PUT
    @Path("/{userId}/update-request")
    Uni<Response> submitUpdateRequest(@PathParam("userId") Long userId, UserUpdateRequestDto request);

    @GET
    @Path("/{userId}/verification-status")
    Uni<Response> getVerificationStatus(@PathParam("userId") Long userId);

    @GET
    @Path("/field/all")
    Uni<Response> getAllFields();

    @POST
    @Path("/field/new")
    Uni<Response> addNewField(FieldDto dto);

    @DELETE
    @Path("/field/{id}")
    Uni<Response> deleteField(@PathParam("id") Long id);

    @GET
    @Path("/departments")
    Uni<Response> getAllDepartments();

    @GET
    @Path("/graduation-groups")
    Uni<Response> getAllGraduationGroups();

    @GET
    @Path("/specialty/all")
    Uni<Response> getAllSpecialty();

    @POST
    @Path("/specialty/new")
    Uni<Response> addNewSpecialty(SpecialtyDto dto);

    @DELETE
    @Path("/specialty/{id}")
    Uni<Response> deleteSpecialty(@PathParam("id") Long id);
}
