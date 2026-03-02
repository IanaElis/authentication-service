package com.alex.project.clients;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/")
@RegisterRestClient
public interface ChatWsRestClient {

    record UserJwtToRoom(String jwtToken, Integer rooms){}

    record ChatSideActionRequest(String requestType, String affected, Integer room){}

    @POST
    @Path("/subscribe-room")
    Response subscribeToRoom(UserJwtToRoom request);

    @POST
    Response broadcastSideActionRequest(ChatSideActionRequest request); //update or archive

}
