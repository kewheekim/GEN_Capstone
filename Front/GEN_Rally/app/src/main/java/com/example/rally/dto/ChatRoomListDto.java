package com.example.rally.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomListDto {
    private Long roomId;
    private Long opponentId;
    private String opponentName;
    private String gameStyle;
    private String opponentProfileImageUrl;
    private String lastMessage; // 마지막 메시지 내용
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간
    private int unreadCount; // 안 읽은 메시지 수
}
