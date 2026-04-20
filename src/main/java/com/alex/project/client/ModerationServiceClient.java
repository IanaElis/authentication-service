package com.alex.project.client;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
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
