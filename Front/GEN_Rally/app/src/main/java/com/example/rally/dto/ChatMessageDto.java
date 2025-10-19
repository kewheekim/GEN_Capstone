package com.example.rally.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ChatMessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String type;
    private String content;
    private String createdAt;

    public ChatMessageDto (Long id, Long roomId, Long senderId, String type, String content, String createdAt){
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }
}
