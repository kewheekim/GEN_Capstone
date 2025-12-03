package com.example.rally.dto;

import lombok.Getter;

@Getter
public class GameReviewDto {
    private Long gameId;
    private String date;
    private int myScore;
    private int opponentScore;
    private String playTime;
    private int steps;
    private int calories;
    private String opponentImage;
}
