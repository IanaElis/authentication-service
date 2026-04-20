package com.alex.project.dtos.chat;

public record ChatroomUserDetails(long id,
                                  int chatroomId,
                                  long userId,
                                  String role,
                                  String status,
                                  String timeAdded,
                                  Long lastRead) {
}
