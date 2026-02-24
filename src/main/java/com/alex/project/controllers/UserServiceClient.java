package com.alex.project.controllers;

import com.alex.project.dtos.CreateProfileDto;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("/internal/profiles")
@RegisterRestClient(configKey = "user-service")
public interface UserServiceClient {

    @POST
    Response createProfile(CreateProfileDto request);
}
