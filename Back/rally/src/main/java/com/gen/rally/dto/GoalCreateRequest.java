package com.gen.rally.dto;

import com.gen.rally.enums.GoalTheme;
import com.gen.rally.enums.GoalType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GoalCreateRequest {

    private String name;
    private GoalTheme theme;
    private GoalType type;
    private Integer targetWeeks;
    private Integer targetCount;
    private Integer calorie;
}
