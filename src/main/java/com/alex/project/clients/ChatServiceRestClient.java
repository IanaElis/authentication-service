package com.alex.project.clients;

import com.alex.project.dtos.ChatMessageElement;
import com.alex.project.dtos.ChatroomEventfulElement;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.Instant;
import java.util.List;

@Path("http://localhost:8083/chat")
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ChatServiceRestClient {

    record ChatActionRequest(long requesterUserId, ChatMessageElement message) {}

    @PUT
    @Path("/archive")
    Response archiveMessage (ChatActionRequest actionRequest);

    @PUT
    @Path("/update")
    Response updateMessage(ChatActionRequest actionRequest);

    @GET
    @Path("/load-chats-sidebar")
    List<ChatroomEventfulElement> loadChatroomEventfulElements(@QueryParam("userId") long userId,
                                                               @QueryParam("latestEventTimeOnPage") Instant latestEventTimeOnPage,
                                                               @QueryParam("latestChatroomIdOnPage") Integer latestChatroomIdOnPage,
                                                               @QueryParam("latestChatMessageIdOnPage") Long latestChatMessageIdOnPage);




    // send save request to rabbit, so not here

    // but archive and update are different?

    // I can archive and delete, but also send broadcast after operation?

}
