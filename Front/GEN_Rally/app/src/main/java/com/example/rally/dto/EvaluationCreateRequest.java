package com.example.rally.dto;

public class EvaluationCreateRequest {
    private Long gameId;
    private Double mannerScore;
    private String comment;

    public EvaluationCreateRequest(Long gameId, Double mannerScore, String comment) {
        this.gameId = gameId;
        this.mannerScore = mannerScore;
        this.comment = comment;
    }
}
