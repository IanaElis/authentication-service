package com.alex.project.dtos.chat;

public record ChatroomEventfulElement(
        Long userId,
        int chatroomId,
        Long lastRead,
        String lastReadTimestamp,
        String displayName,
        boolean isPrivateChat,
        String URLImage,
        String messageContent,
        Long lastChatMessageId,
        String clientMessageId,
        String activityTime
) {
}