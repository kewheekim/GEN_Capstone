package com.example.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchInfoDto {
    private String place;
    private String date;
    private String timeRange;
    private String gameStyle;
    private String gameType;
    private String opponentName;
    private String opponentProfileUrl;
    private Long roomId;
}
