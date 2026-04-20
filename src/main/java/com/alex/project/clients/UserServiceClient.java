package com.alex.project.clients;

import com.alex.project.dtos.user.*;
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
    Response createProfile(CreateProfileDto request);

    @POST
    @Path("/profiles/get")
    Response getProfile(SearchUser email);

    @POST
    @Path("/profiles/update")
    Response updateProfile(ProfileDto profile, @HeaderParam("X-USER-EMAIL") String email);

    @GET
    @Path("/field/all")
    Response getAllFields();

    @POST
    @Path("/field/new")
    Response addNewField(FieldDto dto);

    @GET
    @Path("/specialty/all")
    Response getAllSpecialty();

    @POST
    @Path("/specialty/new")
    Response addNewSpecialty(SpecialtyDto dto);
}
