package com.alex.project.clients;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/")
@RegisterRestClient
public interface ChatWsRestClient {

    record UserIdToRoomsByResponse(
            @NotNull Long id,
            List<Integer> roomsResponse) {}

    record ChangeMessageStateEvent(
            @NotNull String clientMessageId,
            @Positive int chatroomId,
            String updatedContent) {}

    record ChatroomUserAddEvent(
            @Positive int chatroomId,
            @NotEmpty List<Long> usersAdded) {}

    @POST
    @Path("/subscribe-rooms")
    Response subscribeToRooms(UserIdToRoomsByResponse request);

    @POST
    @Path("/message-state-update")
    Response broadcastMessageUpdate(ChangeMessageStateEvent event);

    @POST
    @Path("/new-chatroom-broadcast")
    Response broadcastChatroomUserAddition(ChatroomUserAddEvent event);

}
