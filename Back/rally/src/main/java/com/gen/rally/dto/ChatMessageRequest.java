package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private Long senderId;
    private String content;
}
