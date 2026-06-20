package com.alex.project.clients;

import com.alex.project.dtos.people.FriendRequestDto;
import com.alex.project.dtos.people.PersonDto;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

//@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient
public interface FriendServiceApiClient {

    @POST
    @Path("/create")
    Uni<Boolean> createUser(CreateUserRequest request);

    @PUT
    @Path("/{userId}/name")
    Uni<Boolean> updateName(@PathParam("userId") long userId, UpdateNameRequest request);

    @GET
    @Path("{userId}/search/{page}")
    Uni<List<PersonDto>> searchPeople(
            @PathParam("userId") long userId,
            @PathParam("page") int page,
            @QueryParam("query") String query
    );

    @GET
    @Path("{userId}/friends/{page}")
    Uni<List<PersonDto>> getFriendsOfPerson(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/friends-all")
    Uni<List<PersonDto>> getAllFriendsOfPerson(@PathParam("userId") long userId);

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    Uni<List<PersonDto>> getCommonABFriends(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/incoming-requests/{page}")
    Uni<List<FriendRequestDto>> getIncomingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    Uni<List<FriendRequestDto>> getOutgoingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/blocked/{page}")
    Uni<List<PersonDto>> getBlockedUsers(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @POST
    @Path("/send-request")
    Uni<Boolean> sendFriendRequest(UserAction action);

    @POST
    @Path("/remove-request")
    Uni<Boolean> removeFriendRequest(UserAction action);

    @POST
    @Path("/delete-friend")
    Uni<Boolean> deleteFriend(UserAction action);

    @POST
    @Path("/add-blacklist")
    Uni<Boolean> addUserToBlacklist(BlockAction action);

    @POST
    @Path("/remove-blacklist")
    Uni<Boolean> removeUserFromBlacklist(BlockAction action);

    @GET
    @Path("{userA}/{userB}/is-blocked")
    Uni<Boolean> checkIfBlocked(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB
    );

    class CreateUserRequest {
        public long userId;
        public String name;
        public long facultyNumber;
    }

    class UpdateNameRequest {
        public String newName;
    }


    class UserAction {
        public long userA;
        public long userB;
    }

    class BlockAction {
        public long blocker;
        public long blocked;
    }
}

