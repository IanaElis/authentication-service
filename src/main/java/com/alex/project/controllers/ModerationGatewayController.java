package com.alex.project.controllers;

import com.alex.project.clients.ModerationServiceClient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/moderation")
@ApplicationScoped
@Authenticated
public class ModerationGatewayController {

    @Inject
    @RestClient
    ModerationServiceClient client;

    @GET
    @Path("/all")
    public Uni<Response> getAllModerationRequests() {
        return client.getAllModerationRequests();
    }

    @POST
    @Path("/{moderationId}/accept")
    public Uni<Response> accept(@PathParam("moderationId") Long moderationId, @HeaderParam("X-USER-EMAIL") String email) {
        return client.accept(moderationId, email);
    }
}
