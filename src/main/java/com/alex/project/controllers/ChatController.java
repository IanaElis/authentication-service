package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.chat.ChatMessageSaveData;
import com.alex.project.dtos.chat.ChatroomEventfulElement;
import com.alex.project.dtos.chat.ChatroomOverview;
import io.quarkus.security.Authenticated;
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
@Authenticated
@ApplicationScoped
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    @Channel("chat-message-request")
    Emitter<ChatMessageSaveData> emitter;

    @RestClient
    ChatServiceRestClient chatServiceRestClient;

    @RestClient
    ChatWsRestClient chatWsRestClient;

    @Inject
    JsonWebToken token;

    private long currentUserId() {
        Object claim = token.getClaim("userid");
        if (claim == null) {
            throw new NotAuthorizedException("Missing userid claim");
        }
        return Long.parseLong(claim.toString());
    }

    private void ensureOk(Response response, String errorMessage) {
        if (response == null || response.getStatus() != Response.Status.OK.getStatusCode()) {
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }
    }

    @GET
    @Path("/load-chatrooms")
    public List<ChatroomEventfulElement> loadChatroomEventfulElements(
            @QueryParam("latestChatroomId") Integer latestChatroomId,
            @QueryParam("latestChatMessageId") Long latestChatMessageId,
            @QueryParam("latestEventTime") String latestEventTime
    ) {
        return chatServiceRestClient.loadChatroomEventfulElements(
                currentUserId(),
                latestEventTime,
                latestChatroomId,
                latestChatMessageId
        );
    }

    @POST
    @Path("/subscribe-chatrooms")
    public void subscribeUserToChatrooms() {
        long userId = currentUserId();

        List<Integer> rooms = chatServiceRestClient.getChatroomIdsByUserId(userId);

        Response response = chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserIdToRoomsByResponse(userId, rooms)
        );

        ensureOk(response, "Unable to subscribe user to chatrooms");
    }

    @POST
    @Path("/subscribe-chatrooms/{chatroom-id}")
    public void subscribeUserToChatroom(@PathParam("chatroom-id") int chatroomId) {
        long userId = currentUserId();

        Response response = chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserIdToRoomsByResponse(
                        userId,
                        List.of(chatroomId)
                )
        );

        ensureOk(response, "Unable to subscribe user to chatroom " + chatroomId);
    }

    @POST
    @Path("/send-message")
    public Response sendMessage(ChatMessageSaveData chatMessageSaveData) {

        if(token.getClaim("userId") != null) {
            log.warn("Attempt to send message from user directly " +
                    "to service was made, token: {}",
                    token.getClaimNames().toString() );
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if(!token.getClaim("service").toString().equals("ws-service")) {
            log.warn("Attempt to send message was made without " +
                    "sufficient claim, token: {}",
                    token.getClaimNames().toString() );
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending message {} with clientId {}",
                    chatMessageSaveData,
                    chatMessageSaveData.clientMessageId());
        }
        emitter.send(chatMessageSaveData);

        return Response.ok().build();
    }

    @PUT
    @Path("/messages/update")
    public void updateMessage(ChatServiceRestClient.MessageUpdateRequest request) {
        long userId = currentUserId();

        ChatServiceRestClient.MessageUpdateRequest enriched =
                new ChatServiceRestClient.MessageUpdateRequest(
                        userId,
                        request.message(),
                        request.chatroomId(),
                        request.newMessage()
                );

        ensureOk(
                chatServiceRestClient.updateMessage(enriched),
                "Unable to process chat message update"
        );

        ensureOk(
                chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                request.newMessage()
                        )
                ),
                "Unable to broadcast message update"
        );
    }

    @PUT
    @Path("/messages/archive")
    public void archiveMessage(ChatServiceRestClient.MessageRemoveRequest request) {
        long userId = currentUserId();

        ChatServiceRestClient.MessageRemoveRequest enriched =
                new ChatServiceRestClient.MessageRemoveRequest(
                        userId,
                        request.chatroomId(),
                        request.message()
                );

        ensureOk(
                chatServiceRestClient.archiveMessage(enriched),
                "Unable to archive chat message"
        );

        ensureOk(
                chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                null
                        )
                ),
                "Unable to broadcast message archive"
        );
    }

    @POST
    @Path("/chatrooms")
    public Integer createChatroom(ChatServiceRestClient.CreateChatroomRequest request) {
        ChatServiceRestClient.CreateChatroomRequest enriched =
                new ChatServiceRestClient.CreateChatroomRequest(
                        currentUserId(),
                        request.name()
                );

        return chatServiceRestClient.createChatroom(enriched);
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/archive")
    public void archiveChatroom(@PathParam("chatroomId") int chatroomId) {
        ensureOk(
                chatServiceRestClient.archiveChatroom(currentUserId(), chatroomId),
                "Unable to archive chatroom " + chatroomId
        );
    }

    @GET
    @Path("/chatrooms/{chatroomId}")
    public ChatroomOverview getChatroomOverview(@PathParam("chatroomId") int chatroomId) {
        return chatServiceRestClient.getChatroomOverview(
                currentUserId(),
                chatroomId
        );
    }

    @GET
    @Path("/chatrooms/{chatroomId}/users")
    public Response getUsers(
            @PathParam("chatroomId") int chatroomId,
            @QueryParam("oldestAdditionTimestamp") String oldestAdditionTimestamp,
            @QueryParam("oldestAdditionId") Integer oldestAdditionId
    ) {
        return chatServiceRestClient.getUsers(
                currentUserId(),
                oldestAdditionTimestamp,
                oldestAdditionId,
                chatroomId
        );
    }

    @POST
    @Path("/chatrooms/{chatroomId}/users")
    public void addUsers(
            @PathParam("chatroomId") int chatroomId,
            ChatServiceRestClient.AddUsersRequest request) {

        ensureOk(
                chatServiceRestClient.addUsers(
                        currentUserId(),
                        chatroomId,
                        request
                ),
                "Unable to add users to chatroom " + chatroomId
        );

        ensureOk(
                chatWsRestClient.broadcastChatroomUserAddition(
                        new ChatWsRestClient.ChatroomUserAddEvent(
                                chatroomId,
                                request.usersWithRoles()
                                        .keySet()
                                        .stream()
                                        .toList()
                        )
                ),
                "Unable to broadcast user addition for chatroom " + chatroomId
        );
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/role")
    public void changeUserRole(
            @PathParam("chatroomId") int chatroomId,
            @PathParam("affectedUserId") long affectedUserId,
            ChatServiceRestClient.UpdateRoleRequest request
    ) {
        ensureOk(
                chatServiceRestClient.changeUserRole(
                        currentUserId(),
                        chatroomId,
                        affectedUserId,
                        request
                ),
                "Unable to change user role"
        );
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/membership-status")
    public void changeUserMembershipStatus(
            @PathParam("chatroomId") int chatroomId,
            @PathParam("affectedUserId") long affectedUserId,
            ChatServiceRestClient.UpdateMembershipStatusRequest request
    ) {
        ensureOk(
                chatServiceRestClient.changeUserMembershipStatus(
                        currentUserId(),
                        affectedUserId,
                        chatroomId,
                        request
                ),
                "Unable to change membership status"
        );
    }
}