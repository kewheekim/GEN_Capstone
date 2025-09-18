package com.gen.rally.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
}
