package com.example.rally.dto;

import java.util.List;

public class GoalCheckDto {
    List<Long> goalIds;
    Long gameId;
    public GoalCheckDto(List<Long> goalIds, Long gameId) {
        this.goalIds = goalIds;
        this.gameId = gameId;
    }
}
