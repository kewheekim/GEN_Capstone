package com.gen.rally.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatRoomDto {
    private Long opponentId;
    private String opponentName;
    private String opponentProfileUrl;

    private MatchRequestInfoDto myRequestInfo;
    private MatchRequestInfoDto opponentRequestInfo;
}
