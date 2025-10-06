package com.example.rally.dto;

public class EvaluationCreateRequest {
    private Long gameId;
    private String subject;
    private Double mannerScore;
    private String comment;

    public EvaluationCreateRequest(Long gameId, String subject, Double mannerScore,
                                   String comment) {
        this.gameId = gameId;
        this.subject = subject;
        this.mannerScore = mannerScore;
        this.comment = comment;
    }
}
