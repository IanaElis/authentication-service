package com.alex.project.controllers;

import com.alex.project.clients.ModerationServiceClient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/moderation")
@ApplicationScoped
@RolesAllowed({"ADMIN", "MODERATOR"})
public class ModerationGatewayController {

    @Inject
    @RestClient
    ModerationServiceClient client;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/all")
    public Uni<Response> getAllModerationRequests() {
        return client.getAllModerationRequests();
    }

    @POST
    @Path("/{moderationId}/accept")
    public Uni<Response> accept(@PathParam("moderationId") Long moderationId) {
        String username = jwt.getSubject();
        return client.accept(moderationId, username);
    }

    @POST
    @Path("/{moderationId}/reject")
    public Uni<Response> reject(
            @PathParam("moderationId") Long moderationId,
            @QueryParam("reason") String reason) {
        String username = jwt.getSubject();
        return client.reject(moderationId, username, reason);
    }
}
