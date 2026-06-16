package com.alex.project.dtos.chat;

import java.time.Instant;

public record ChatMessageElement(long id,
                                 String uuid,
                                 long senderId,
                                 long senderUserId,
                                 String timeSent,
                                 String content) {
}
