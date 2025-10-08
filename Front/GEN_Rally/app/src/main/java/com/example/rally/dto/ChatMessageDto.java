package com.example.rally.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ChatMessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessageDto (Long id, Long roomId, Long senderId, String content, LocalDateTime createdAt){
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }
}
