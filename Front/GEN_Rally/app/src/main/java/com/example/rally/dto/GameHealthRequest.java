package com.example.rally.dto;

public class GameHealthRequest {
    public Long gameId;
    public Integer steps;
    public Integer maxHr;
    public Integer minHr;
    public Integer calories;
    public String seriesHr;

    public GameHealthRequest(Long gameId, Integer steps, Integer maxHr, Integer minHr, Integer calories, String seriesHr) {
        this.gameId = gameId;
        this.steps = steps;
        this.maxHr = maxHr;
        this.minHr = minHr;
        this.calories = calories;
        this.seriesHr = seriesHr;
    }
}
