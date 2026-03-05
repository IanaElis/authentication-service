package com.alex.project.dtos.chat;

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

