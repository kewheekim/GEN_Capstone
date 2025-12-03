package com.gen.rally.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecordWeeklyCalorie {
    private String title;   // 11월 1주차
    private List<DailyCalorie> dailyCalories;

    @Getter
    @Builder
    public static class DailyCalorie {
        private String dayOfWeek; //요일
        private int calories;
        private String date;
    }
}
