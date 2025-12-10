package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class GoalCheckDto {
    private List<Long> goalIds;
    private Long gameId;
}
