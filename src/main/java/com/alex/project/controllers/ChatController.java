package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.dtos.ContentPage;
import com.alex.project.dtos.chat.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

public interface ChatController {

    @GET
    @Path("/load-chatrooms")
    ContentPage<ChatroomEventfulElement> loadChatroomEventfulElements(
            @QueryParam("latestChatroomId") Integer latestChatroomId,
            @QueryParam("latestChatMessageId") Long latestChatMessageId,
            @QueryParam("latestEventTime") String latestEventTime
    );

    @POST
    @Path("/subscribe-chatrooms")
    Response subscribeUserToChatrooms();

    @POST
    @Path("/subscribe-chatrooms/{chatroom-id}")
    Response subscribeUserToChatroom(@PathParam("chatroom-id") int chatroomId);

    @POST
    @Path("/messages/send-message")
    Response sendMessage(ChatMessageOperationalData chatMessageOperationalData);

    @PUT
    @Path("/messages/update")
    Response updateMessage(ChatServiceRestClient.MessageUpdateRequest request);

    @PUT
    @Path("/messages/archive")
    Response archiveMessage(ChatServiceRestClient.MessageRemoveRequest request);

    @POST
    @Path("/chatrooms")
    Integer createChatroom(ChatServiceRestClient.CreateChatroomRequest request);

    @PUT
    @Path("/chatrooms/{chatroomId}/archive")
    Response archiveChatroom(@PathParam("chatroomId") int chatroomId);

    @GET
    @Path("/chatrooms/{chatroomId}")
    ChatroomOverview getChatroomOverview(@PathParam("chatroomId") int chatroomId);

    @GET
    @Path("/chatrooms/{chatroomId}/users")
    ContentPage<ChatroomUserDetails> getUsers(
            @PathParam("chatroomId") int chatroomId,
            @QueryParam("oldestAdditionTimestamp") String oldestAdditionTimestamp,
            @QueryParam("oldestAdditionId") Integer oldestAdditionId
    );

    @POST
    @Path("/chatrooms/{chatroomId}/users")
    Response addUsers(
            @PathParam("chatroomId") int chatroomId,
            ChatServiceRestClient.AddUsersRequest request);

    @PUT
    @Path("/chatrooms/{chatroomId}/users/{affectedUserId}/role")
    Response changeUserRole(
            @PathParam("chatroomId") int chatroomId,
            @PathParam("affectedUserId") long affectedUserId,
            ChatServiceRestClient.UpdateRoleRequest request
    );

    @PUT
    @Path("/chatrooms/{chatroomId}/users/update-last-read")
    Response updateLastReadState(@PathParam("chatroomId") int chatroomId,
                                 @QueryParam("messageId") long messageId);

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/membership-status")
    Response changeUserMembershipStatus(
            @PathParam("chatroomId") int chatroomId,
            @QueryParam("affectedUserId") long affectedUserId,
            ChatServiceRestClient.UpdateMembershipStatusRequest request
    );

    @PUT
    @Path("/chatrooms/{chatroomId}/name")
    Response updateChatroomName(
            @PathParam("chatroomId") int chatroomId,
            @QueryParam("newName") String newName);

    @GET
    @Path("/messages/page")
    ContentPage<ChatMessageElement> getMessagePage(
            @QueryParam("chatroomId") int chatroomId,
            @QueryParam("oldestTimestamp") String oldestTimestamp,
            @QueryParam("oldestId") Integer oldestId,
            @QueryParam("requestForOlder") boolean requestForOlder);
}
