package com.gen.rally.dto;

import jakarta.persistence.Column;
import lombok.Getter;

@Getter
public class GameHealthRequest {
    public Long gameId;
    public Integer steps;
    public Integer maxHr;
    public Integer minHr;
    public Integer calories;
    public String seriesHr;
}
