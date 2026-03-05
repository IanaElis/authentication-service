package com.alex.project.dtos.chat;

import java.time.Instant;

public record ChatMessageElement(long id,
                          String uuid,
                          long senderUser,
                          Instant timeSent,
                          String content) {
}