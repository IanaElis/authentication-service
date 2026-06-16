package com.alex.project.clients;

import com.alex.project.dtos.ContentPage;
import com.alex.project.dtos.chat.ChatMessageElement;
import com.alex.project.dtos.chat.ChatroomEventfulElement;
import com.alex.project.dtos.chat.ChatroomOverview;
import com.alex.project.dtos.chat.ChatroomUserDetails;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Map;

@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ChatServiceRestClient {

    public record MessageUpdateRequest(long requesterUserId,
                                       int chatroomId,
                                       ChatMessageElement message,
                                       String newMessage
    ) {}

    public record MessageRemoveRequest(
            long requesterUserId,
            int chatroomId,
            ChatMessageElement message
    ) {}

    @PUT
    @Path("/chat-messages/update")
    Response updateMessage(MessageUpdateRequest request);

    @PUT
    @Path("/chat-messages/archive")
    Response archiveMessage(MessageRemoveRequest request);

    public record CreateChatroomRequest(
            long requestingUserId,
            String name
    ) {}


    @GET
    @Path("/chat-messages/page")
    ContentPage<ChatMessageElement> getMessagePage(@QueryParam("requesterUserId") @Positive long requesterUserId,
                                                   @QueryParam("chatroomId") @Positive int chatroomId,
                                                   @QueryParam("messageCursorId") Long messageCursorId,
                                                   @QueryParam("downScroll") boolean downScroll,
                                                   @QueryParam("initialRequest") boolean initialRequest);

    @POST
    @Path("/chatrooms/create")
    Integer createChatroom(CreateChatroomRequest request);

    @PUT
    @Path("/chatrooms/{chatroomId}/archive")
    Response archiveChatroom(
            @QueryParam("userId") long requestingUserId,
            @PathParam("chatroomId") int chatroomId
    );

    @GET
    @Path("/chatrooms/{chatroomId}")
    ChatroomOverview getChatroomOverview(
            @QueryParam("userId") long requestingUserId,
            @PathParam("chatroomId") int chatroomId
    );

    @GET
    @Path("/chatrooms/events")
    ContentPage<ChatroomEventfulElement> loadChatroomEventfulElements(
            @QueryParam("userId") long userId,
            @QueryParam("latestEventTimeOnPage") String latestEventTimeOnPage,
            @QueryParam("latestChatroomIdOnPage") Integer latestChatroomIdOnPage,
            @QueryParam("latestChatMessageIdOnPage") Long latestChatMessageIdOnPage
    );

    @GET
    @Path("/chatrooms/user")
    List<Integer> getChatroomIdsForUser(@QueryParam("userId") long requestingUserId);

    public record AddUsersRequest(Map<Long, String> usersWithRoles) {}

    public record UpdateRoleRequest(String updatedRole) {}

    public record UpdateMembershipStatusRequest(String updatedStatus) {}

    public record UpdateLastReadStateRequest(long newLastReadMessage, String newLastReadTimestamp) {}

    @GET
    @Path("/chatroom-users/{chatroomId}/users")
    ContentPage<ChatroomUserDetails> getUsers(
            @QueryParam("userId") long requestingUserId,
            @QueryParam("oldestAdditionTimestamp") String oldestAdditionTimestamp,
            @QueryParam("oldestAdditionId") Integer oldestAdditionId,
            @PathParam("chatroomId") int chatroomId
    );

    @POST
    @Path("/chatroom-users/{chatroomId}/users")
    Response addUsers(
            @QueryParam("userId") long requestingUserId,
            @PathParam("chatroomId") int chatroomId,
            AddUsersRequest request
    );

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/{affectedUserId}/role")
    Response changeUserRole(
            @QueryParam("userId") long requestingUserId,
            @PathParam("chatroomId") int chatroomId,
            @PathParam("affectedUserId") long affectedUserId,
            UpdateRoleRequest request
    );

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/membership-status")
    Response changeUserMembershipStatus(
            @QueryParam("userId") long requestingUserId,
            @QueryParam("affectedUserId") long affectedUserId,
            @PathParam("chatroomId") int chatroomId,
            UpdateMembershipStatusRequest request
    );

    @PUT
    @Path("/chatroom-users/{chatroomId}/users/update-last-read")
    Response updateLastRead(
            @PathParam("chatroomId") int chatroomId,
            @QueryParam("userId") long userId,
            UpdateLastReadStateRequest request
    );

    @PUT
    @Path("/{chatroomId}/name")
    Response updateChatroomName(
            @PathParam("chatroomId") @Positive int chatroomId,
            @QueryParam("userId") @Positive long userId,
            @QueryParam("newName") String newName);


}



//    @GET
//    @Path("/chatrooms/events/{chatroom-id}")
//    ChatroomEventfulElement getChatroomEventfulElementById(
//            @PathParam("chatroom-id") int chatroomId,
//            @QueryParam("userId") long userId
//    );
