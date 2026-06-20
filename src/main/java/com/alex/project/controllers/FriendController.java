package com.alex.project.controllers;

import com.alex.project.clients.FriendServiceApiClient;
import com.alex.project.dtos.people.FriendRequestDto;
import com.alex.project.dtos.people.PersonDto;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
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
    public Uni<Boolean> createUser(FriendServiceApiClient.CreateUserRequest request) {
        if (request == null
                || request.userId <= 0
                || request.name == null
                || request.name.isBlank()
                || request.facultyNumber <= 0) {
            throw new BadRequestException("Invalid user data.");
        }
        return client.createUser(request)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while creating user", e);
                    throw new BadRequestException("Unable to create user.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @PUT
    @Path("/{userId}/name")
    public Uni<Boolean> updateName(@PathParam("userId") long userId,
                                    FriendServiceApiClient.UpdateNameRequest request) {
        if (userId <= 0
                || request == null
                || request.newName == null
                || request.newName.isBlank()) {
            throw new BadRequestException("Invalid name update request.");
        }
        return client.updateName(userId, request)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while updating name", e);
                    throw new BadRequestException("Unable to update name.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userId}/search/{page}")
    public Uni<List<PersonDto>> searchPeople(@PathParam("userId") long userId,
                                              @PathParam("page") int page,
                                              @QueryParam("query") String query) {
        if (userId <= 0 || page < 0 || query == null || query.isBlank()) {
            throw new BadRequestException("Invalid search parameters.");
        }
        return client.searchPeople(userId, page, query)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while searching people", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userId}/friends/{page}")
    public Uni<List<PersonDto>> getFriendsOfPerson(@PathParam("userId") long userId,
                                                    @PathParam("page") int page) {
        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid friends request.");
        }
        return client.getFriendsOfPerson(userId, page)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while fetching friends", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userId}/friends-all")
    public Uni<List<PersonDto>> getAllFriendsOfPerson(@PathParam("userId") long userId) {
        if (userId <= 0) {
            throw new BadRequestException("Invalid user id.");
        }
        return client.getAllFriendsOfPerson(userId)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while fetching all friends", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    public Uni<List<PersonDto>> getCommonABFriends(@PathParam("userA") long userA,
                                                    @PathParam("userB") long userB,
                                                    @PathParam("page") int page) {
        if (userA <= 0 || userB <= 0 || userA == userB || page < 0) {
            throw new BadRequestException("Invalid common friends request.");
        }
        return client.getCommonABFriends(userA, userB, page)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while fetching common friends", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userId}/incoming-requests/{page}")
    public Uni<List<FriendRequestDto>> getIncomingRequests(@PathParam("userId") long userId,
                                                            @PathParam("page") int page) {
        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid incoming requests query.");
        }
        return client.getIncomingRequests(userId, page)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while fetching incoming requests", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    public Uni<List<FriendRequestDto>> getOutgoingRequests(@PathParam("userId") long userId,
                                                            @PathParam("page") int page) {
        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid outgoing requests query.");
        }
        return client.getOutgoingRequests(userId, page)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while fetching outgoing requests", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userId}/blocked/{page}")
    public Uni<List<PersonDto>> getBlockedUsers(@PathParam("userId") long userId,
                                                 @PathParam("page") int page) {
        if (userId <= 0 || page < 0) {
            throw new BadRequestException("Invalid blocked users query.");
        }
        return client.getBlockedUsers(userId, page)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while fetching blocked users", e);
                    return Collections.emptyList();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/send-request")
    public Uni<Boolean> sendFriendRequest(FriendServiceApiClient.UserAction action) {
        if (action == null
                || action.userA <= 0
                || action.userB <= 0
                || action.userA == action.userB) {
            throw new BadRequestException("Invalid friend request.");
        }
        return client.sendFriendRequest(action)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while sending friend request", e);
                    throw new BadRequestException("Unable to send friend request.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/remove-request")
    public Uni<Boolean> removeFriendRequest(FriendServiceApiClient.UserAction action) {
        if (action == null || action.userA <= 0 || action.userB <= 0) {
            throw new BadRequestException("Invalid remove request.");
        }
        return client.removeFriendRequest(action)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while removing friend request", e);
                    throw new BadRequestException("Unable to remove friend request.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/delete-friend")
    public Uni<Boolean> deleteFriend(FriendServiceApiClient.UserAction action) {
        if (action == null || action.userA <= 0 || action.userB <= 0) {
            throw new BadRequestException("Invalid delete friend request.");
        }
        return client.deleteFriend(action)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while deleting friend", e);
                    throw new BadRequestException("Unable to delete friend.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/add-blacklist")
    public Uni<Boolean> addUserToBlacklist(FriendServiceApiClient.BlockAction action) {
        if (action == null
                || action.blocker <= 0
                || action.blocked <= 0
                || action.blocker == action.blocked) {
            throw new BadRequestException("Invalid blacklist request.");
        }
        return client.addUserToBlacklist(action)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while blocking user", e);
                    throw new BadRequestException("Unable to block user.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Path("/remove-blacklist")
    public Uni<Boolean> removeUserFromBlacklist(FriendServiceApiClient.BlockAction action) {
        if (action == null
                || action.blocker <= 0
                || action.blocked <= 0) {
            throw new BadRequestException("Invalid unblock request.");
        }
        return client.removeUserFromBlacklist(action)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while unblocking user", e);
                    throw new BadRequestException("Unable to unblock user.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @Path("{userA}/{userB}/is-blocked")
    public Uni<Boolean> checkIfBlocked(@PathParam("userA") long userA,
                                        @PathParam("userB") long userB) {
        if (userA <= 0 || userB <= 0 || userA == userB) {
            throw new BadRequestException("Invalid block check request.");
        }
        return client.checkIfBlocked(userA, userB)
                .onFailure().recoverWithItem(e -> {
                    LOG.error("Error while checking block status", e);
                    throw new BadRequestException("Unable to check block status.");
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
