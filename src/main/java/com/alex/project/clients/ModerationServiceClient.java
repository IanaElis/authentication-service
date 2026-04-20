package com.alex.project.clients;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "moderation-service")
public interface ModerationServiceClient {

    @GET
    @Path("/internal/moderationProfiles/all")
    Response getAllModerationRequests();

    @POST
    @Path("/moderationResponse/{moderationId}/accept")
    Response accept(@PathParam("moderationId") Long moderationId, @HeaderParam("X-USER-EMAIL") String email);
}
