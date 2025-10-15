package com.example.rally.dto;

import lombok.Getter;

@Getter
public class ChatRoomDto {
    private Long opponentId;
    private String opponentName;
    private String opponentProfileUrl;

    private MatchRequestInfoDto myRequestInfo;
    private MatchRequestInfoDto opponentRequestInfo;
}
