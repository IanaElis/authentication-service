package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.ContentPage;
import com.alex.project.dtos.chat.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.alex.project.utils.JwtService.currentUserId;
import static com.alex.project.utils.ResponseChecker.ensureOk;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@ApplicationScoped
public class ChatControllerImpl implements ChatController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatControllerImpl.class);

    @Channel("chat-message-request")
    Emitter<ChatMessageOperationalData> emitter;

    @RestClient
    ChatServiceRestClient chatServiceRestClient;

    @RestClient
    ChatWsRestClient chatWsRestClient;

    @Inject
    JsonWebToken token;
//
//    @Inject
//    ObjectMapper mapper;

    @GET
    @Path("/load-chatrooms")
    @Override
    public ContentPage<ChatroomEventfulElement> loadChatroomEventfulElements(
            @QueryParam("latestChatroomId") Integer latestChatroomId,
            @QueryParam("latestChatMessageId") Long latestChatMessageId,
            @QueryParam("latestEventTime") String latestEventTime
    ) {
        return chatServiceRestClient.loadChatroomEventfulElements(
                currentUserId(token),
                    latestEventTime,
                latestChatroomId,
                latestChatMessageId
        );
    }

    @POST
    @Path("/subscribe-chatrooms")
    @Override
    public Response subscribeUserToChatrooms() {
        long userId = currentUserId(token);

        List<Integer> rooms = chatServiceRestClient.getChatroomIdsForUser(userId);

        Response response = chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserIdToRoomsByResponse(userId, rooms)
        );

        ensureOk(response,
                "Unable to subscribe user to chatrooms",
                LOG);
        return Response.ok(response).build();
    }

    @POST
    @Path("/subscribe-chatrooms/{chatroom-id}")
    @Override
    public Response subscribeUserToChatroom(@PathParam("chatroom-id") @Positive int chatroomId) {
        long userId = currentUserId(token);

        Response response = chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserIdToRoomsByResponse(
                        userId,
                        List.of(chatroomId)
                )
        );

        ensureOk(response,
                "Unable to subscribe user to chatroom " + chatroomId,
                LOG);
        return Response.ok().build();
    }

    @POST
    @Path("/messages/send-message")
    @Override
    public Response sendMessage(@Valid ChatMessageOperationalData chatMessageOperationalData) {

        if(token.getClaim("userId") != null) {
            LOG.warn("Attempt to send message from user directly " +
                    "to service was made, token: {}",
                    token.getClaimNames().toString() );
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if(!token.getClaim("service").toString().equals("ws-service")) {
            LOG.warn("Attempt to send message was made without " +
                    "sufficient claim, token: {}",
                    token.getClaimNames().toString() );
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending message {} with clientId {}",
                    chatMessageOperationalData,
                    chatMessageOperationalData.clientMessageId());
        }

        emitter.send(chatMessageOperationalData);

        return Response.ok().build();
    }

    @PUT
    @Path("/messages/update")
    @Override
    public Response updateMessage(@Valid ChatServiceRestClient.MessageUpdateRequest request) {
        long userId = currentUserId(token);

        ChatServiceRestClient.MessageUpdateRequest enriched =
                new ChatServiceRestClient.MessageUpdateRequest(
                        userId,
                        request.chatroomId(),
                        request.message(),
                        request.newMessage()
                );

        ensureOk(
                chatServiceRestClient.updateMessage(enriched),
                "Unable to process chat message update",
                LOG
        );

        ensureOk(
                chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                request.newMessage()
                        )
                ),
                "Unable to broadcast message update",
                LOG
        );
        return Response.ok().build();

    }

    @PUT
    @Path("/messages/archive")
    @Override
    public Response archiveMessage(@Valid ChatServiceRestClient.MessageRemoveRequest request) {
        long userId = currentUserId(token);

        ChatServiceRestClient.MessageRemoveRequest enriched =
                new ChatServiceRestClient.MessageRemoveRequest(
                        userId,
                        request.chatroomId(),
                        request.message()
                );

        ensureOk(
                chatServiceRestClient.archiveMessage(enriched),
                "Unable to archive chat message",
                LOG
        );

        ensureOk(
                chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                null
                        )
                ),
                "Unable to broadcast message archive",
                LOG
        );
        return Response.ok().build();

    }

    @POST
    @Path("/chatrooms")
    @Override
    public Integer createChatroom(@Valid ChatServiceRestClient.CreateChatroomRequest request) {
        ChatServiceRestClient.CreateChatroomRequest enriched =
                new ChatServiceRestClient.CreateChatroomRequest(
                        currentUserId(token),
                        request.name()
                );

        return chatServiceRestClient.createChatroom(enriched);
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/archive")
    @Override
    public Response archiveChatroom(@PathParam("chatroomId") @Positive int chatroomId) {
        ensureOk(
                chatServiceRestClient.archiveChatroom(currentUserId(token), chatroomId),
                "Unable to archive chatroom " + chatroomId,
                LOG
        );

        return Response.ok().build();

    }

    @GET
    @Path("/chatrooms/{chatroomId}")
    @Override
    public ChatroomOverview getChatroomOverview(@PathParam("chatroomId") @Positive int chatroomId) {
        return chatServiceRestClient.getChatroomOverview(
                currentUserId(token),
                chatroomId
        );
    }

    @GET
    @Path("/chatrooms/{chatroomId}/users")
    @Override
    public ContentPage<ChatroomUserDetails> getUsers(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("oldestAdditionTimestamp") String oldestAdditionTimestamp,
            @QueryParam("oldestAdditionId") Integer oldestAdditionId
    ) {
        return chatServiceRestClient.getUsers(
                currentUserId(token),
                oldestAdditionTimestamp,
                oldestAdditionId,
                chatroomId
        );
    }

    @POST
    @Path("/chatrooms/{chatroomId}/users")
    @Override
    public Response addUsers(
            @PathParam("chatroomId") @Positive int chatroomId,
            @Valid ChatServiceRestClient.AddUsersRequest request) {

        ensureOk(
                chatServiceRestClient.addUsers(
                        currentUserId(token),
                        chatroomId,
                        request
                ),
                "Unable to add users to chatroom " + chatroomId,
                LOG
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
                "Unable to broadcast user addition for chatroom " + chatroomId,
                LOG
        );

        return Response.ok().build();
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/role")
    @Override
    public Response changeUserRole(
            @PathParam("chatroomId") @Positive int chatroomId,
            @PathParam("affectedUserId")@Positive long affectedUserId,
            @Valid ChatServiceRestClient.UpdateRoleRequest request
    ) {
        ensureOk(
                chatServiceRestClient.changeUserRole(
                        currentUserId(token),
                        chatroomId,
                        affectedUserId,
                        request
                ),
                "Unable to change user role",
                LOG
        );

        return Response.ok().build();
    }


    @PUT
    @Path("/chatrooms/{chatroomId}/users/update-last-read")
    @Override
    public Response updateLastReadState(@PathParam("chatroomId") @Positive int chatroomId,
                                        @QueryParam("messageId") @Positive long messageId) {
        return chatServiceRestClient.updateLastRead(
                chatroomId,
                currentUserId(token),
                new ChatServiceRestClient.UpdateLastReadStateRequest(messageId));
    }

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/membership-status")
    @Override
    public Response changeUserMembershipStatus(
            @PathParam("chatroomId")@Positive int chatroomId,
            @QueryParam("affectedUserId")@Positive long affectedUserId,
            @Valid ChatServiceRestClient.UpdateMembershipStatusRequest request
    ) {
        ensureOk(
                chatServiceRestClient.changeUserMembershipStatus(
                        currentUserId(token),
                        affectedUserId,
                        chatroomId,
                        request
                ),
                "Unable to change membership status",
                LOG
        );
        return Response.ok().build();
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/name")
    @Override
    public Response updateChatroomName(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("newName") @NotBlank String newName) {

        ensureOk(
                chatServiceRestClient.updateChatroomName(
                        chatroomId,
                        currentUserId(token),
                        newName
                ),
                "Unable to update chatroom name",
                LOG
        );

        return Response.ok().build();
    }



    @GET
    @Path("/messages/page")
    @Override
    public ContentPage<ChatMessageElement> getMessagePage(
            @QueryParam("chatroomId") int chatroomId,
            @QueryParam("oldestTimestamp") String oldestTimestamp,
            @QueryParam("oldestId") Integer oldestId,
            @QueryParam("requestForOlder") boolean requestForOlder) {

        return chatServiceRestClient.getMessagePage(
                currentUserId(token),
                chatroomId,
                oldestTimestamp,
                oldestId,
                requestForOlder
        );
    }

}