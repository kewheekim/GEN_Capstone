package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class GameHealthDto {
    private Integer maxHr;
    private Integer minHr;
    private Integer steps;
    private Integer calories;
    private List<HeartSampleDto> seriesHr;

    @Getter @Setter
    public static class HeartSampleDto {
        private int bpm;
        private long epochMs;
    }
}
