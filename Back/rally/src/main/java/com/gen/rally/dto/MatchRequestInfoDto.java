package com.gen.rally.dto;

import com.gen.rally.enums.GameStyle;
import com.gen.rally.enums.GameType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchRequestInfoDto {
    private String place;
    private String date; // LocalDate 대신 문자열로 처리하거나, 필요에 따라 LocalDate를 사용
    private String timeRange;
    private GameStyle gameStyle;
    private GameType gameType;
}
