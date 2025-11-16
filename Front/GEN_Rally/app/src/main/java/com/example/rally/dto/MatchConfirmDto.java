package com.example.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchConfirmDto {
    private Long roomId;
    private Long gameId;
    private String time;
    private String place;
}
