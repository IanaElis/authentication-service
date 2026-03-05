package com.alex.project.controllers;

import com.alex.project.clients.FriendServiceApiClient;
import com.alex.project.dtos.people.FriendRequestDto;
import com.alex.project.dtos.people.PersonDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FriendController {

    private static final Logger LOG = Logger.getLogger(FriendController.class);
    private static final int PAGE_SIZE = 15;

    @Inject
    @RestClient
    FriendServiceApiClient client;

    @POST
    @Path("/create")
    public boolean createUser(FriendServiceApiClient.CreateUserRequest request) {

        if (request == null
                || request.userId <= 0
                || request.name == null
                || request.name.isBlank()
                || request.facultyNumber <= 0) {
            throw new BadRequestException("Invalid user data.");
        }

        try {
            return client.createUser(request);
        } catch (Exception e) {
            LOG.error("Error while creating user", e);
            throw new BadRequestException("Unable to create user.");
        }
    }

    @PUT
    @Path("/{userId}/name")
    public boolean updateName(@PathParam("userId") long userId,
                              FriendServiceApiClient.UpdateNameRequest request) {

        if (userId <= 0
                || request == null
                || request.newName == null
                || request.newName.isBlank()) {
            throw new BadRequestException("Invalid name update request.");
        }

        try {
            return client.updateName(userId, request);
        } catch (Exception e) {
            LOG.error("Error while updating name", e);
            throw new BadRequestException("Unable to update name.");
        }
    }

    @GET
    @Path("{userId}/search/{page}")
    public List<PersonDto> searchPeople(@PathParam("userId") long userId,
                                        @PathParam("page") int page,
                                        @QueryParam("query") String query) {

        if (userId <= 0 || page < 0 || query == null || query.isBlank()) {
            throw new BadRequestException("Invalid search parameters.");
        }

        try {
            return client.searchPeople(userId, page, query);
        } catch (Exception e) {
            LOG.error("Error while searching people", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("{userId}/friends/{page}")
    public List<PersonDto> getFriendsOfPerson(@PathParam("userId") long userId,
                                              @PathParam("page") int page) {

        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid friends request.");
        }

        try {
            return client.getFriendsOfPerson(userId, page);
        } catch (Exception e) {
            LOG.error("Error while fetching friends", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("{userId}/friends-all")
    public List<PersonDto> getAllFriendsOfPerson(@PathParam("userId") long userId) {

        if (userId <= 0) {
            throw new BadRequestException("Invalid user id.");
        }

        try {
            return client.getAllFriendsOfPerson(userId);
        } catch (Exception e) {
            LOG.error("Error while fetching all friends", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    public List<PersonDto> getCommonABFriends(@PathParam("userA") long userA,
                                              @PathParam("userB") long userB,
                                              @PathParam("page") int page) {

        if (userA <= 0 || userB <= 0 || userA == userB || page < 0) {
            throw new BadRequestException("Invalid common friends request.");
        }

        try {
            return client.getCommonABFriends(userA, userB, page);
        } catch (Exception e) {
            LOG.error("Error while fetching common friends", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("{userId}/incoming-requests/{page}")
    public List<FriendRequestDto> getIncomingRequests(@PathParam("userId") long userId,
                                                      @PathParam("page") int page) {

        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid incoming requests query.");
        }

        try {
            return client.getIncomingRequests(userId, page);
        } catch (Exception e) {
            LOG.error("Error while fetching incoming requests", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    public List<FriendRequestDto> getOutgoingRequests(@PathParam("userId") long userId,
                                                      @PathParam("page") int page) {

        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid outgoing requests query.");
        }

        try {
            return client.getOutgoingRequests(userId, page);
        } catch (Exception e) {
            LOG.error("Error while fetching outgoing requests", e);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("{userId}/blocked/{page}")
    public List<PersonDto> getBlockedUsers(@PathParam("userId") long userId,
                                           @PathParam("page") int page) {

        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid blocked users query.");
        }

        try {
            return client.getBlockedUsers(userId, page);
        } catch (Exception e) {
            LOG.error("Error while fetching blocked users", e);
            return Collections.emptyList();
        }
    }

    @POST
    @Path("/send-request")
    public boolean sendFriendRequest(FriendServiceApiClient.UserAction action) {

        if (action == null
                || action.userA <= 0
                || action.userB <= 0
                || action.userA == action.userB) {
            throw new BadRequestException("Invalid friend request.");
        }

        try {
            return client.sendFriendRequest(action);
        } catch (Exception e) {
            LOG.error("Error while sending friend request", e);
            throw new BadRequestException("Unable to send friend request.");
        }
    }

    @POST
    @Path("/remove-request")
    public boolean removeFriendRequest(FriendServiceApiClient.UserAction action) {

        if (action == null || action.userA <= 0 || action.userB <= 0) {
            throw new BadRequestException("Invalid remove request.");
        }

        try {
            return client.removeFriendRequest(action);
        } catch (Exception e) {
            LOG.error("Error while removing friend request", e);
            throw new BadRequestException("Unable to remove friend request.");
        }
    }

    @POST
    @Path("/delete-friend")
    public boolean deleteFriend(FriendServiceApiClient.UserAction action) {

        if (action == null || action.userA <= 0 || action.userB <= 0) {
            throw new BadRequestException("Invalid delete friend request.");
        }

        try {
            return client.deleteFriend(action);
        } catch (Exception e) {
            LOG.error("Error while deleting friend", e);
            throw new BadRequestException("Unable to delete friend.");
        }
    }

    @POST
    @Path("/add-blacklist")
    public boolean addUserToBlacklist(FriendServiceApiClient.BlockAction action) {

        if (action == null
                || action.blocker <= 0
                || action.blocked <= 0
                || action.blocker == action.blocked) {
            throw new BadRequestException("Invalid blacklist request.");
        }

        try {
            return client.addUserToBlacklist(action);
        } catch (Exception e) {
            LOG.error("Error while blocking user", e);
            throw new BadRequestException("Unable to block user.");
        }
    }

    @GET
    @Path("{userA}/{userB}/is-blocked")
    public boolean checkIfBlocked(@PathParam("userA") long userA,
                                  @PathParam("userB") long userB) {

        if (userA <= 0 || userB <= 0 || userA == userB) {
            throw new BadRequestException("Invalid block check request.");
        }

        try {
            return client.checkIfBlocked(userA, userB);
        } catch (Exception e) {
            LOG.error("Error while checking block status", e);
            throw new BadRequestException("Unable to check block status.");
        }
    }
}