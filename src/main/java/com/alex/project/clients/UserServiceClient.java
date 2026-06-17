package com.alex.project.clients;

import com.alex.project.dtos.user.*;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("/internal")
@RegisterRestClient(configKey = "user-service")
public interface UserServiceClient {

    @POST
    @Path("/profiles")
    Uni<Response> createProfile(CreateProfileDto request);

    @POST
    @Path("/profiles/get")
    Uni<Response> getProfile(SearchUser email);

    @POST
    @Path("/profiles/update")
    Uni<Response> updateProfile(ProfileDto profile, @HeaderParam("X-USER-EMAIL") String email);

    @GET
    @Path("/field/all")
    Uni<Response> getAllFields();

    @POST
    @Path("/field/new")
    Uni<Response> addNewField(FieldDto dto);

    @GET
    @Path("/specialty/all")
    Uni<Response> getAllSpecialty();

    @POST
    @Path("/specialty/new")
    Uni<Response> addNewSpecialty(SpecialtyDto dto);
}
