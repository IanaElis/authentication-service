package com.alex.project.controllers;

import com.alex.project.clients.ChatWsRestClient;
import com.alex.project.dtos.chat.ChatMessageOperationalData;
import com.alex.project.dtos.chat.enums.AckStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.alex.project.utils.ResponseChecker.ensureOk;


@ApplicationScoped
public class AckProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AckProcessor.class);
    @RestClient
    ChatWsRestClient chatWsRestClient;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("chat-message-response")
    public void ackMessages(JsonObject res) {

        Uni<Response> ackUni = chatWsRestClient.ackResult(objectMapper.convertValue(
                res.getMap(),
                new TypeReference<Map<AckStatus, List<ChatMessageOperationalData>>>() {}
        ));

        ackUni.subscribe().with(
                response -> ensureOk(response, "Unable to Acknowledge messages", LOG),
                failure -> LOG.error("Failed to ack messages", failure)
        );
    }
}
