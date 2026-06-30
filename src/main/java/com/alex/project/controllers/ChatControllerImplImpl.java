package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.ContentPage;
import com.alex.project.dtos.chat.*;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
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
public class ChatControllerImplImpl implements ChatControllerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ChatControllerImplImpl.class);

    @Channel("chat-message-request")
    Emitter<ChatMessageOperationalData> emitter;

    @RestClient
    ChatServiceRestClient chatServiceRestClient;

    @RestClient
    ChatWsRestClient chatWsRestClient;

    @Inject
    JsonWebToken token;

    @GET
    @Path("/load-chatrooms")
    @Override
    public Uni<ContentPage<ChatroomEventfulElement>> loadChatroomEventfulElements(
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
    public Uni<Response> subscribeUserToChatrooms() {
        long userId = currentUserId(token);

        Uni<List<Integer>> roomsUni = chatServiceRestClient.getChatroomIdsForUser(userId);
        Uni<Response> subscribeUni = roomsUni.flatMap(rooms ->
                chatWsRestClient.subscribeToRooms(
                        new ChatWsRestClient.UserIdToRoomsByResponse(userId, rooms)));
        return subscribeUni
                .invoke(response -> ensureOk(response,
                        "Unable to subscribe user to chatrooms", LOG))
                .map(r -> Response.ok(r).build());
    }

    @POST
    @Path("/subscribe-chatrooms/{chatroom-id}")
    @Override
    public Uni<Response> subscribeUserToChatroom(@PathParam("chatroom-id") @Positive int chatroomId) {
        long userId = currentUserId(token);

        Uni<Response> subscribeUni = chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserIdToRoomsByResponse(
                        userId,
                        List.of(chatroomId)));
        return subscribeUni
                .invoke(response -> ensureOk(response,
                        "Unable to subscribe user to chatroom " + chatroomId, LOG))
                .map(r -> Response.ok().build());
    }

    @POST
    @Path("/messages/send-message")
    @Override
    public Uni<Response> sendMessage(@Valid ChatMessageOperationalData chatMessageOperationalData) {
        return Uni.createFrom().item(() -> {
            if (token.getClaim("userId") != null) {
                LOG.warn("Attempt to send message from user directly " +
                        "to service was made, token: {}",
                        token.getClaimNames().toString());
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (!token.getClaim("service").toString().equals("ws-service")) {
                LOG.warn("Attempt to send message was made without " +
                        "sufficient claim, token: {}",
                        token.getClaimNames().toString());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending message {} with clientId {}",
                        chatMessageOperationalData,
                        chatMessageOperationalData.clientMessageId());
            }

            emitter.send(chatMessageOperationalData);
            return Response.ok().build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @PUT
    @Path("/messages/update")
    @Override
    public Uni<Response> updateMessage(@Valid ChatServiceRestClient.MessageUpdateRequest request) {
        long userId = currentUserId(token);

        ChatServiceRestClient.MessageUpdateRequest enriched =
                new ChatServiceRestClient.MessageUpdateRequest(
                        userId,
                        request.chatroomId(),
                        request.message(),
                        request.newMessage()
                );

        Uni<Response> updateUni = chatServiceRestClient.updateMessage(enriched)
                .invoke(response -> ensureOk(response,
                        "Unable to process chat message update", LOG));

        Uni<Response> broadcastUni = updateUni.flatMap(r ->
                chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                request.newMessage())));

        return broadcastUni
                .invoke(response -> ensureOk(response,
                        "Unable to broadcast message update", LOG))
                .map(r -> Response.ok().build());
    }

    @PUT
    @Path("/messages/archive")
    @Override
    public Uni<Response> archiveMessage(@Valid ChatServiceRestClient.MessageRemoveRequest request) {
        long userId = currentUserId(token);

        ChatServiceRestClient.MessageRemoveRequest enriched =
                new ChatServiceRestClient.MessageRemoveRequest(
                        userId,
                        request.chatroomId(),
                        request.message()
                );

        Uni<Response> archiveUni = chatServiceRestClient.archiveMessage(enriched)
                .invoke(response -> ensureOk(response,
                        "Unable to archive chat message", LOG));

        Uni<Response> broadcastUni = archiveUni.flatMap(r ->
                chatWsRestClient.broadcastMessageUpdate(
                        new ChatWsRestClient.ChangeMessageStateEvent(
                                request.message().uuid(),
                                request.chatroomId(),
                                null)));

        return broadcastUni
                .invoke(response -> ensureOk(response,
                        "Unable to broadcast message archive", LOG))
                .map(r -> Response.ok().build());
    }

    @POST
    @Path("/chatrooms")
    @Override
    public Uni<Integer> createChatroom(@Valid ChatServiceRestClient.CreateChatroomRequest request) {
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
    public Uni<Response> archiveChatroom(@PathParam("chatroomId") @Positive int chatroomId) {
        return chatServiceRestClient.archiveChatroom(currentUserId(token), chatroomId)
                .invoke(response -> ensureOk(response,
                        "Unable to archive chatroom " + chatroomId, LOG))
                .map(r -> Response.ok().build());
    }

    @GET
    @Path("/chatrooms/{chatroomId}")
    @Override
    public Uni<ChatroomOverview> getChatroomOverview(@PathParam("chatroomId") @Positive int chatroomId) {
        return chatServiceRestClient.getChatroomOverview(
                currentUserId(token),
                chatroomId
        );
    }

    @GET
    @Path("/chatrooms/{chatroomId}/users")
    @Override
    public Uni<ContentPage<ChatroomUserDetails>> getUsers(
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
    public Uni<Response> addUsers(
            @PathParam("chatroomId") @Positive int chatroomId,
            @Valid ChatServiceRestClient.AddUsersRequest request) {

        Uni<Response> addUsersUni = chatServiceRestClient.addUsers(
                        currentUserId(token),
                        chatroomId,
                        request)
                .invoke(response -> ensureOk(response,
                        "Unable to add users to chatroom " + chatroomId, LOG));

        Uni<Response> broadcastUni = addUsersUni.flatMap(r ->
                chatWsRestClient.broadcastChatroomUserAddition(
                        new ChatWsRestClient.ChatroomUserAddEvent(
                                chatroomId,
                                request.usersWithRoles()
                                        .keySet()
                                        .stream()
                                        .toList())));

        return broadcastUni
                .invoke(response -> ensureOk(response,
                        "Unable to broadcast user addition for chatroom " + chatroomId, LOG))
                .map(r -> Response.ok().build());
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/role")
    @Override
    public Uni<Response> changeUserRole(
            @PathParam("chatroomId") @Positive int chatroomId,
            @PathParam("affectedUserId") @Positive long affectedUserId,
            @Valid ChatServiceRestClient.UpdateRoleRequest request
    ) {
        return chatServiceRestClient.changeUserRole(
                        currentUserId(token),
                        chatroomId,
                        affectedUserId,
                        request)
                .invoke(response -> ensureOk(response,
                        "Unable to change user role", LOG))
                .map(r -> Response.ok().build());
    }

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/update-last-read")
    @Override
    public Uni<Response> updateLastReadState(@PathParam("chatroomId") @Positive int chatroomId,
                                              @QueryParam("messageId") long messageId,
                                              @QueryParam("timeSent") @Positive long timestamp) {
        return chatServiceRestClient.updateLastRead(
                chatroomId,
                currentUserId(token),
                new ChatServiceRestClient.UpdateLastReadStateRequest(messageId, Long.toString(timestamp)));
    }

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/membership-status")
    @Override
    public Uni<Response> changeUserMembershipStatus(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("affectedUserId") @Positive long affectedUserId,
            @Valid ChatServiceRestClient.UpdateMembershipStatusRequest request
    ) {
        return chatServiceRestClient.changeUserMembershipStatus(
                        currentUserId(token),
                        affectedUserId,
                        chatroomId,
                        request)
                .invoke(response -> ensureOk(response,
                        "Unable to change membership status", LOG))
                .map(r -> Response.ok().build());
    }

    @PUT
    @Path("/chatrooms/{chatroomId}/name")
    @Override
    public Uni<Response> updateChatroomName(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("newName") @NotBlank String newName) {

        return chatServiceRestClient.updateChatroomName(
                        chatroomId,
                        currentUserId(token),
                        newName)
                .invoke(response -> ensureOk(response,
                        "Unable to update chatroom name", LOG))
                .map(r -> Response.ok().build());
    }

    @GET
    @Path("/messages/page")
    @Override
    public Uni<ContentPage<ChatMessageElement>> getMessagePage(
            @QueryParam("chatroomId") @Positive int chatroomId,
            @QueryParam("messageCursorId") Long messageCursorId,
            @QueryParam("downScroll") boolean downScroll,
            @QueryParam("initialRequest") boolean initialRequest) {

        return chatServiceRestClient.getMessagePage(
                currentUserId(token),
                chatroomId,
                messageCursorId,
                downScroll,
                initialRequest
        );
    }

}
