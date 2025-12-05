package com.example.rally.dto;

import java.util.List;

public class GameHealthDto {
    public Integer maxHr;
    public Integer minHr;
    public Integer steps;
    public Integer calories;
    public List<HeartSampleDto> seriesHr;

    public static class HeartSampleDto {
        public int bpm;
        public long epochMs;
    }
}

