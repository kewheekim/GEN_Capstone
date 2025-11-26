package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameInfoDto {
    private Long gameId;
    private String dateText;
    private String place;
    private String opponentName;
    private String opponentProfileUrl;
    private String myName;
    private String myProfileUrl;

    private int mySetScore;
    private int opponentSetScore;
    private String totalDuration;
}
