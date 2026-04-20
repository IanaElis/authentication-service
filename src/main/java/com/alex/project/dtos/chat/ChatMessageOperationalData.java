package com.alex.project.dtos.chat;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@RegisterForReflection
public record ChatMessageOperationalData (
        String clientMessageId,
        int chatroomId,
        Long senderId,
        Long senderUserId,
        String content,
        String creationTimestamp) {
}

