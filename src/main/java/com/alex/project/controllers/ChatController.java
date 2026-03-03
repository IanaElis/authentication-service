package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.chat.ChatMessageElement;
import com.alex.project.dtos.chat.ChatMessageSaveData;
import com.alex.project.dtos.chat.ChatroomEventfulElement;
import com.alex.project.dtos.chat.ChatroomOverview;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Channel("chat-message-request")
    Emitter<ChatMessageSaveData> emitter;

    @Channel("chat-message-response")
    Multi<ChatMessageElement> chatMessages;

    @RestClient
    ChatServiceRestClient chatServiceRestClient;

    @RestClient
    ChatWsRestClient chatWsRestClient;

    @Inject
    JsonWebToken token;

    @GET
    @Path("/load-chatrooms")
    public List<ChatroomEventfulElement> loadChatroomEventfulElements(
            @QueryParam("latestChatroomId") Integer latestChatroomId,
            @QueryParam("latestChatMessageId") Long latestChatMessageId,
            @QueryParam("latestEventTime") String latestEventTime
    ) {

        String jwtToken = token.getRawToken();
        var userId = token.getClaim("userid");

        try {
            return chatServiceRestClient.loadChatroomEventfulElements(
                    Long.parseLong(userId.toString()),
                    latestEventTime,
                    latestChatroomId,
                    latestChatMessageId);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
//            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/subscribe-chatrooms")
    public Response subscribeUserToChatrooms()
    {
        long userId = token.getClaim("userid");

        List<Integer> roomsResponse = chatServiceRestClient.getChatroomIdsByUserId(userId);

        if(chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserIdToRoomsByResponse(userId, roomsResponse))
                .getStatus() == Response.Status.OK.getStatusCode()) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Path("/subscribe-chatrooms/{chatroom-id}")
    // this is called if a new chatroom was made
    public Response subscribeUserToChatroom(
            @PathParam("chatroom-id") int chatroomId) {
        long userId = token.getClaim("userid");

        if(chatWsRestClient.subscribeToRooms(
                        new ChatWsRestClient.UserIdToRoomsByResponse(
                                userId,
                                List.of(chatroomId))
                ).getStatus() == Response.Status.OK.getStatusCode()) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    // called by send message request, lifts up for all users that have the room
    @POST
    @Path("/send-message")
    public void sendMessage(ChatMessageSaveData chatMessageSaveData) {
        if(log.isDebugEnabled()) {
            log.debug("Sending message {} to {}",
                    chatMessageSaveData.toString(),
                    chatMessageSaveData.clientMessageId());
        }
        emitter.send(chatMessageSaveData);
    }

    @PUT
    @Path("/messages/update")
    public Response updateMessage(ChatServiceRestClient.MessageUpdateRequest request) {
        long userId = token.getClaim("userid");

        ChatServiceRestClient.MessageUpdateRequest enriched =
                new ChatServiceRestClient.MessageUpdateRequest(
                        userId,
                        request.message(),
                        request.chatroomId(),
                        request.newMessage()
                );

        if(chatServiceRestClient.updateMessage(enriched).getStatus() != Response.Status.OK.getStatusCode()) {
            log.error("Unable to process ChatMessage Update: {}", enriched.toString());
            throw new BadRequestException();
        }

        if(chatWsRestClient.broadcastMessageUpdate(
                new ChatWsRestClient.ChangeMessageStateEvent(
                        request.message().uuid(),
                        request.chatroomId(),
                        request.newMessage()))
                .getStatus() != Response.Status.OK.getStatusCode()) {
            log.error("Unable to broadcast message update result: {}", enriched.toString());
            throw new BadRequestException();
        }

        return Response.ok().build();
    }



    @PUT
    @Path("/messages/archive")
    public Response archiveMessage(ChatServiceRestClient.MessageRemoveRequest request) {
        long userId = token.getClaim("userid");

        ChatServiceRestClient.MessageRemoveRequest enriched =
                new ChatServiceRestClient.MessageRemoveRequest(
                        userId,
                        request.chatroomId(),
                        request.message()
                );

        if(chatServiceRestClient.archiveMessage(enriched).getStatus() != Response.Status.OK.getStatusCode()) {
            log.error("Unable to Archive ChatMessage : {}", enriched.toString());
            throw new BadRequestException();
        }

        if(chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                null))
                .getStatus() != Response.Status.OK.getStatusCode()) {
            log.error("Unable to broadcast message archive result: {}", enriched.toString());
            throw new BadRequestException();
        }

        return Response.ok().build();
    }

    @POST
    @Path("/chatrooms")
    public Integer createChatroom(ChatServiceRestClient.CreateChatroomRequest request) {
        long userId = token.getClaim("userid");

        ChatServiceRestClient.CreateChatroomRequest enriched =
                new ChatServiceRestClient.CreateChatroomRequest(
                        userId,
                        request.name()
                );

        return chatServiceRestClient.createChatroom(enriched);
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/archive")
    public Response archiveChatroom(@PathParam("chatroomId") int chatroomId) {
        long userId = token.getClaim("userid");
        return chatServiceRestClient.archiveChatroom(userId, chatroomId);
    }

    @GET
    @Path("/chatrooms/{chatroomId}")
    public ChatroomOverview getChatroomOverview(@PathParam("chatroomId") int chatroomId) {
        long userId = token.getClaim("userid");
        return chatServiceRestClient.getChatroomOverview(userId, chatroomId);
    }

    @GET
    @Path("/chatrooms/{chatroomId}/users")
    public Response getUsers(
            @PathParam("chatroomId") int chatroomId,
            @QueryParam("oldestAdditionTimestamp") String oldestAdditionTimestamp,
            @QueryParam("oldestAdditionId") Integer oldestAdditionId
    ) {
        long userId = token.getClaim("userid");

        return chatServiceRestClient.getUsers(
                userId,
                oldestAdditionTimestamp,
                oldestAdditionId,
                chatroomId
        );
    }

    @POST
    @Path("/chatrooms/{chatroomId}/users")
    public Response addUsers(
            @PathParam("chatroomId") int chatroomId,
            ChatServiceRestClient.AddUsersRequest request) {

        long userId = token.getClaim("userid");

        if(chatServiceRestClient.addUsers(
                userId,
                chatroomId,
                request).getStatus()
                == Response.Status.OK.getStatusCode()) {

            if(chatWsRestClient.broadcastChatroomUserAddition(
                    new ChatWsRestClient
                            .ChatroomUserAddEvent(
                                    chatroomId,
                            request.usersWithRoles()
                                    .keySet()
                                    .stream()
                                    .toList())).getStatus()
                    == Response.Status.OK.getStatusCode()) {
                return Response.ok().build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/role")
    public Response changeUserRole(
            @PathParam("chatroomId") int chatroomId,
            @PathParam("affectedUserId") long affectedUserId,
            ChatServiceRestClient.UpdateRoleRequest request
    ) {
        long userId = token.getClaim("userid");

        return chatServiceRestClient.changeUserRole(
                userId,
                chatroomId,
                affectedUserId,
                request
        );
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/membership-status")
    public Response changeUserMembershipStatus(
            @PathParam("chatroomId") int chatroomId,
            @PathParam("affectedUserId") long affectedUserId,
            ChatServiceRestClient.UpdateMembershipStatusRequest request
    ) {
        long userId = token.getClaim("userid");

        return chatServiceRestClient.changeUserMembershipStatus(
                userId,
                affectedUserId,
                chatroomId,
                request
        );
    }

}
