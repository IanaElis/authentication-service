package com.alex.project.clients;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "moderation-service")
public interface ModerationServiceClient {

    @GET
    @Path("/internal/moderationProfiles/all")
    Uni<Response> getAllModerationRequests();

    @POST
    @Path("/moderationResponse/{moderationId}/accept")
    Uni<Response> accept(@PathParam("moderationId") Long moderationId, @QueryParam("reviewer") String reviewer);

    @POST
    @Path("/moderationResponse/{moderationId}/reject")
    Uni<Response> reject(@PathParam("moderationId") Long moderationId, @QueryParam("reviewer") String reviewer, @QueryParam("reason") String reason);
}
