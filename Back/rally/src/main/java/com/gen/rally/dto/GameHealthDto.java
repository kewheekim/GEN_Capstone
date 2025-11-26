package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameHealthDto {
    private Integer maxHr;
    private Integer minHr;
    private Integer steps;
    private Integer calories;
    private String seriesHr;
}
