package com.gen.rally.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameReviewDto {
    private Long gameId;
    private int myScore;
    private int opponentScore;
    private String playTime;
    private int steps;
    private int calories;
    private String opponentImage;
}
