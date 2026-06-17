package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.dtos.ContentPage;
import com.alex.project.dtos.chat.*;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

public interface ChatControllerImpl {
    @GET
    @Path("/load-chatrooms")
    Uni<ContentPage<ChatroomEventfulElement>> loadChatroomEventfulElements(
            @QueryParam("latestChatroomId") Integer latestChatroomId,
            @QueryParam("latestChatMessageId") Long latestChatMessageId,
            @QueryParam("latestEventTime") String latestEventTime
    );

    @POST
    @Path("/subscribe-chatrooms")
    Uni<Response> subscribeUserToChatrooms();

    @POST
    @Path("/subscribe-chatrooms/{chatroom-id}")
    Uni<Response> subscribeUserToChatroom(@PathParam("chatroom-id") @Positive int chatroomId);

    @POST
    @Path("/messages/send-message")
    Uni<Response> sendMessage(@Valid ChatMessageOperationalData chatMessageOperationalData);

    @PUT
    @Path("/messages/update")
    Uni<Response> updateMessage(@Valid ChatServiceRestClient.MessageUpdateRequest request);

    @PUT
    @Path("/messages/archive")
    Uni<Response> archiveMessage(@Valid ChatServiceRestClient.MessageRemoveRequest request);

    @POST
    @Path("/chatrooms")
    Uni<Integer> createChatroom(@Valid ChatServiceRestClient.CreateChatroomRequest request);

    @PUT
    @Path("/chatrooms/{chatroomId}/archive")
    Uni<Response> archiveChatroom(@PathParam("chatroomId") @Positive int chatroomId);

    @GET
    @Path("/chatrooms/{chatroomId}")
    Uni<ChatroomOverview> getChatroomOverview(@PathParam("chatroomId") @Positive int chatroomId);

    @GET
    @Path("/chatrooms/{chatroomId}/users")
    Uni<ContentPage<ChatroomUserDetails>> getUsers(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("oldestAdditionTimestamp") String oldestAdditionTimestamp,
            @QueryParam("oldestAdditionId") Integer oldestAdditionId
    );

    @POST
    @Path("/chatrooms/{chatroomId}/users")
    Uni<Response> addUsers(
            @PathParam("chatroomId") @Positive int chatroomId,
            @Valid ChatServiceRestClient.AddUsersRequest request);

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/role")
    Uni<Response> changeUserRole(
            @PathParam("chatroomId") @Positive int chatroomId,
            @PathParam("affectedUserId") @Positive long affectedUserId,
            @Valid ChatServiceRestClient.UpdateRoleRequest request
    );

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/update-last-read")
    Uni<Response> updateLastReadState(@PathParam("chatroomId") @Positive int chatroomId,
                                       @QueryParam("messageId") long messageId,
                                       @QueryParam("timeSent") @Positive long timestamp);

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/membership-status")
    Uni<Response> changeUserMembershipStatus(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("affectedUserId") @Positive long affectedUserId,
            @Valid ChatServiceRestClient.UpdateMembershipStatusRequest request
    );

    @PUT
    @Path("/chatrooms/{chatroomId}/name")
    Uni<Response> updateChatroomName(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("newName") @NotBlank String newName);

    @GET
    @Path("/messages/page")
    Uni<ContentPage<ChatMessageElement>> getMessagePage(
            @QueryParam("chatroomId") @Positive int chatroomId,
            @QueryParam("messageCursorId") Long messageCursorId,
            @QueryParam("downScroll") boolean downScroll,
            @QueryParam("initialRequest") boolean initialRequest);
}
