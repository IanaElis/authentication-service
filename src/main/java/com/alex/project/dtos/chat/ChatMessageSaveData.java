package com.alex.project.dtos.chat;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@RegisterForReflection
public record ChatMessageSaveData(
        @NotNull @NotBlank String clientMessageId,
        int chatroomId,
        long senderId,
        @NotNull @NotBlank String content,
        @NotNull Instant creationTimestamp) {
}
