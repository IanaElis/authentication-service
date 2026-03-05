package com.alex.project.clients;

import com.alex.project.dtos.people.FriendRequestDto;
import com.alex.project.dtos.people.PersonDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient
public interface FriendServiceApiClient {

    @POST
    @Path("/create")
    boolean createUser(CreateUserRequest request);

    @PUT
    @Path("/{userId}/name")
    boolean updateName(@PathParam("userId") long userId, UpdateNameRequest request);

    @GET
    @Path("{userId}/search/{page}")
    List<PersonDto> searchPeople(
            @PathParam("userId") long userId,
            @PathParam("page") int page,
            @QueryParam("query") String query
    );

    @GET
    @Path("{userId}/friends/{page}")
    List<PersonDto> getFriendsOfPerson(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/friends-all")
    List<PersonDto> getAllFriendsOfPerson(@PathParam("userId") long userId);

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    List<PersonDto> getCommonABFriends(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/incoming-requests/{page}")
    List<FriendRequestDto> getIncomingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    List<FriendRequestDto> getOutgoingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @GET
    @Path("{userId}/blocked/{page}")
    List<PersonDto> getBlockedUsers(
            @PathParam("userId") long userId,
            @PathParam("page") int page
    );

    @POST
    @Path("/send-request")
    boolean sendFriendRequest(UserAction action);

    @POST
    @Path("/remove-request")
    boolean removeFriendRequest(UserAction action);

    @POST
    @Path("/delete-friend")
    boolean deleteFriend(UserAction action);

    @POST
    @Path("/add-blacklist")
    boolean addUserToBlacklist(BlockAction action);

    @GET
    @Path("{userA}/{userB}/is-blocked")
    boolean checkIfBlocked(
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

