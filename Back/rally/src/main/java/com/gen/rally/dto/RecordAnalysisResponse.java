package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecordAnalysisResponse {
    private List<GoalItem> goalItems;
    private List<GameReviewDto> gameResults;
    private List<CommentDto> comments;
}
