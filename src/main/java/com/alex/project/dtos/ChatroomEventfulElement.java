package com.alex.project.dtos;

import java.time.Instant;
import java.util.UUID;

public record ChatroomEventfulElement(
        long userId,
        int chatroomId,
        String chatroomName,
        String messageContent,
        long lastChatMessageId,
        String clientMessageId,
        String activityTime
) {
}

