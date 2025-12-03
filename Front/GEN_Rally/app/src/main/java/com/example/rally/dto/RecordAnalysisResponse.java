package com.example.rally.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class RecordAnalysisResponse {
    private List<GoalActiveItem> goalItems;
    private List<GameReviewDto> gameResults;
    private List<CommentDto> comments;
}
