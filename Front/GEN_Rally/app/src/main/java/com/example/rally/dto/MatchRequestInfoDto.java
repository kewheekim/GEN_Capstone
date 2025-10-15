package com.example.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchRequestInfoDto {
    private String place;
    private String date;
    private String timeRange;
    private String gameStyle;
    private String gameType;
}
