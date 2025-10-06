package com.example.rally.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ChatMessageDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessageDto (Long messageId, Long roomId, Long senderId, String content, LocalDateTime createdAt){
        this.messageId = messageId;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }
}
