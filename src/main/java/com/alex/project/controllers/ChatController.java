package com.alex.project.controllers;

import com.alex.project.clients.ChatServiceRestClient;
import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.ChatMessageElement;
import com.alex.project.dtos.ChatMessageSaveData;
import com.alex.project.dtos.ChatroomEventfulElement;
import com.alex.project.utils.JwtService;
import io.quarkus.oidc.Oidc;
import io.quarkus.oidc.UserInfo;
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

    @Channel("chat-message-requests")
    Emitter<ChatMessageSaveData> emitter;

    @Channel("chat-messages")
    Multi<ChatMessageElement> chatMessages;
    //may contain only clmsgid and chatroomid just to fetch state

    @RestClient
    ChatServiceRestClient chatServiceRestClient;

    @RestClient
    ChatWsRestClient chatWsRestClient;

    @GET
    @Path("/load-chatrooms")
    // I assume here, that the Jwt will contain user id and I would fetch it from claims.
    public List<ChatroomEventfulElement> loadChatrooms(ChatroomEventfulElement latestUi) {
        long userId = 1; // retrieved from JsonWebToken claim
        String jwtToken = ""; // from jwt as token
        // load chatrooms by UserId from Jwt
        List<ChatroomEventfulElement> list = chatServiceRestClient.loadChatroomEventfulElements(
                latestUi.userId(),
                latestUi.activityTime(),
                latestUi.chatroomId(),
                latestUi.lastChatMessageId());

        List<Integer> chatroomIds = list.stream().map(ChatroomEventfulElement::chatroomId).toList();

        if(chatWsRestClient.subscribeToRooms(
                new ChatWsRestClient.UserJwtToRooms(jwtToken, chatroomIds))
                .getStatus() == Response.Status.OK.getStatusCode()) {
            return list;
        };

        if(log.isDebugEnabled()) {
            log.warn("Unable to load chatrooms for user {}", userId);
        }
        return new ArrayList<>();

    }

    @POST
    @Path("/send-message")
    public void sendMessage(ChatMessageSaveData chatMessageSaveData) {
        emitter.send(chatMessageSaveData);
    }



//    public record ChatroomRequest(List<Integer> chatrooms) {}
//
//    public record MessageSaveState(String clientMessageId, SaveState state){}
//
//    public enum SaveState {
//        SUCCESS, FAILURE
//    }

}
