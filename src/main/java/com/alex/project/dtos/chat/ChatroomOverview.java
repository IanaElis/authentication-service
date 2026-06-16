package com.alex.project.dtos.chat;

public record ChatroomOverview(int id,
                               String name,
                               String createdAt,
                               String globalLastRead) {
}
