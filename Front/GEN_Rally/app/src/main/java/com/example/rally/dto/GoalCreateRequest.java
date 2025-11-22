package com.example.rally.dto;

public class GoalCreateRequest {
    public String name;
    public String theme;
    public String type;
    public Integer targetWeeks;
    public Integer targetCount;
    public Integer calorie;

    public GoalCreateRequest(String name,
                             String theme,
                             String type,
                             Integer targetWeeks,
                             Integer targetCount,
                             Integer calorie) {
        this.name = name;
        this.theme = theme;
        this.type = type;
        this.targetWeeks = targetWeeks;
        this.targetCount = targetCount;
        this.calorie = calorie;
    }
}
