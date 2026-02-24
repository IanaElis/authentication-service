package com.alex.project.clients;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("http://localhost:8081/chat-ws-rest")
@RegisterRestClient
public interface ChatWsRestClient {

    record UserJwtToRooms(String jwtToken, List<Integer> rooms){}

    record ChatSideActionRequest(String requestType, String affected, Integer room){}

    @POST
    Response subscribeToRooms(UserJwtToRooms request);

    @POST
    Response broadcastSideActionRequest(ChatSideActionRequest request); //update or archive

}
