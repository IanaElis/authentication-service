package com.alex.project.controllers;

import com.alex.project.clients.UserServiceClient;
import com.alex.project.dtos.user.FieldDto;
import com.alex.project.dtos.user.ProfileDto;
import com.alex.project.dtos.user.SearchUser;
import com.alex.project.dtos.user.SpecialtyDto;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
    public Response getProfile(@Valid SearchUser email){
        return client.getProfile(email);
    }

    @POST
    @Path("/profiles/update")
    public Response updateProfile(@Valid ProfileDto profile, @HeaderParam("X-USER-EMAIL") String email){
        return client.updateProfile(profile, email);
    }

    @GET
    @Path("/field/all")
    public Response getAllFields(){
        return client.getAllFields();
    }

    @POST
    @Path("/field/new")
    @RolesAllowed("MODERATOR")
    public Response addNewField(@Valid FieldDto dto){
        return client.addNewField(dto);
    }

    @GET
    @Path("/specialty/all")
    public Response getAllSpecialty(){
        return client.getAllSpecialty();
    }

    @POST
    @Path("/specialty/new")
    @RolesAllowed("ADMIN")
    public Response addNewSpecialty(@Valid SpecialtyDto dto){
        return client.addNewSpecialty(dto);
    }
}
