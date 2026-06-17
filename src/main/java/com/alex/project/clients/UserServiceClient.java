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

    @POST
    @Path("/profile")
    Uni<Response> getProfile(@QueryParam("requesterId") long requesterId, @QueryParam("targetUserId") long targetUserId);

    @PUT
    @Path("/{userId}/update-request")
    Uni<Response> updateProfile(@PathParam("userId") long userId, ProfileDto profile);

    // Field and specialty management stubs — not yet implemented on user-service
    @GET
    @Path("/fields")
    Uni<Response> getAllFields();

    @POST
    @Path("/fields")
    Uni<Response> addNewField(FieldDto dto);

    @GET
    @Path("/specialties")
    Uni<Response> getAllSpecialty();

    @POST
    @Path("/specialties")
    Uni<Response> addNewSpecialty(SpecialtyDto dto);
}
