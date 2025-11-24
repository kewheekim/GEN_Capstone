package com.example.rally.dto;

public class GoalActiveItem {
    private long id;
    private String type;
    private String theme;
    private Integer targetWeeksCount;
    private Integer progressCount;
    private String name;
    private boolean achieved;

    public GoalActiveItem(long id, String type, String theme, Integer targetWeeksCount, Integer progressCount, String goal, boolean achieved) {
        this.id = id;
        this.type = type;
        this.theme = theme;
        this.targetWeeksCount = targetWeeksCount;
        this.progressCount = progressCount;
        this.name = goal;
        this.achieved = achieved;
    }

    public long getId() { return id;}

    public String getType() { return type; }

    public String getTheme() { return theme; }
    public Integer getTargetWeeksCount() { return targetWeeksCount; }
    public Integer getProgressCount() { return progressCount; }
    public String getName() { return name; }
    public boolean isAchieved() { return achieved;}
    public void setAchieved(boolean achieved) { this.achieved = achieved; }
}
