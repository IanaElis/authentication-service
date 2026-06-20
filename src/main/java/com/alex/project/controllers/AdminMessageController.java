package com.alex.project.controllers;

import com.alex.project.entiies.AdminMessage;
import com.alex.project.repositories.AdminMessageRepository;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Path("/auth/admin-messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminMessageController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminMessageController.class);

    @Inject
    AdminMessageRepository messageRepository;

    @Inject
    JsonWebToken jwt;

    public record MessageBody(String content) {}

    @POST
    @Transactional
    @RolesAllowed({"ADMIN", "MODERATOR", "USER"})
    public Uni<Response> sendMessage(MessageBody body) {
        return Uni.createFrom().item(() -> {
            long userId = Long.parseLong(jwt.getClaim("userid").toString());
            AdminMessage msg = new AdminMessage();
            msg.senderUserId = userId;
            msg.content = body.content();
            msg.createdAt = LocalDateTime.now();
            messageRepository.persist(msg);
            LOG.info("Admin message from user {}", userId);
            return Response.ok(Map.of("success", true)).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @GET
    @RolesAllowed({"ADMIN", "MODERATOR"})
    public Uni<Response> getMessages() {
        return Uni.createFrom().item(() -> {
            var messages = messageRepository.findAllActive();
            return Response.ok(messages).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @PUT
    @Path("/{id}/resolve")
    @Transactional
    @RolesAllowed({"ADMIN", "MODERATOR"})
    public Uni<Response> resolveMessage(@PathParam("id") long id) {
        return Uni.createFrom().item(() -> {
            var opt = messageRepository.findByIdOptional(id);
            if (opt.isEmpty()) {
                return Response.status(404).entity(Map.of("error", "Message not found")).build();
            }
            AdminMessage msg = opt.get();
            msg.resolved = true;
            messageRepository.persist(msg);
            return Response.ok(Map.of("success", true)).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Scheduled(every = "480h")
    @Transactional
    void cleanupOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusWeeks(3);
        long deleted = messageRepository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            LOG.info("Cleaned up {} admin messages older than 3 weeks", deleted);
        }
    }
}
