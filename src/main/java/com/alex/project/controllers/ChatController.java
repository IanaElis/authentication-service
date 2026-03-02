package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.ChatMessageElement;
import com.alex.project.dtos.ChatMessageSaveData;
import com.alex.project.dtos.ChatroomEventfulElement;
import com.alex.project.utils.JwtService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<ChatroomEventfulElement> loadChatrooms(
            @QueryParam("latestChatroomId") Integer latestChatroomId,
            @QueryParam("latestChatMessageId") Long latestChatMessageId,
            @QueryParam("latestEventTime") String latestEventTime
    ) {

        String jwtToken = token.getRawToken();

        log.info("JWT token AAAA string: {}", jwtToken);
        log.info("JWT token BBBB token: {}", token.toString());
        var userId = token.getClaim("userid");
        log.info("JWT userid claim CCCC: {}", userId.toString());

        List<ChatroomEventfulElement> list = chatServiceRestClient.loadChatroomEventfulElements(
                Long.parseLong(userId.toString()),
                latestEventTime,
                latestChatroomId,
                latestChatMessageId);

        List<Integer> chatroomIds = list.stream().map(ChatroomEventfulElement::chatroomId).toList();

        if(chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserJwtToRooms(jwtToken, chatroomIds))
                .getStatus() == Response.Status.OK.getStatusCode()) {
            return list;
        }

        if(log.isDebugEnabled()) {
            log.warn("Unable to load chatrooms for user {}", userId);
        }
        return new ArrayList<>();

    }

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
                        request.newMessage()
                );

        return chatServiceRestClient.updateMessage(enriched);
    }

    @PUT
    @Path("/messages/archive")
    public Response archiveMessage(ChatServiceRestClient.MessageRemoveRequest request) {
        long userId = token.getClaim("userid");

        ChatServiceRestClient.MessageRemoveRequest enriched =
                new ChatServiceRestClient.MessageRemoveRequest(
                        userId,
                        request.message()
                );

        return chatServiceRestClient.archiveMessage(enriched);
    }


    @POST
    @Path("/chatrooms")
    public Response createChatroom(ChatServiceRestClient.CreateChatroomRequest request) {
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
    public Response getChatroomOverview(@PathParam("chatroomId") int chatroomId) {
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
            ChatServiceRestClient.AddUsersRequest request
    ) {
        long userId = token.getClaim("userid");

        return chatServiceRestClient.addUsers(
                userId,
                chatroomId,
                request
        );
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
