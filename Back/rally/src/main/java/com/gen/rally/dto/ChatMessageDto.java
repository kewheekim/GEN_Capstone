package com.gen.rally.dto;

import com.gen.rally.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;

    // DTO 변환용
    public static ChatMessageDto fromEntity(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
