package com.example.rally.dto;

public class GoalCreateRequest {
    public String name;
    public String theme;
    public String type;
    public Integer targetWeeksCount;
    public Integer calorie;

    public GoalCreateRequest(String name,
                             String theme,
                             String type,
                             Integer targetWeeksCount,
                             Integer calorie) {
        this.name = name;
        this.theme = theme;
        this.type = type;
        this.targetWeeksCount = targetWeeksCount;
        this.calorie = calorie;
    }
}
