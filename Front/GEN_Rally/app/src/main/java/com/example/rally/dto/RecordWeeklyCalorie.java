package com.example.rally.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class RecordWeeklyCalorie {
    private String title;   // 11월 1주차
    private List<DailyCalorie> dailyCalories;

    @Getter
    public static class DailyCalorie {
        private String dayOfWeek;
        private int calories;
        private String date;
    }
}