package com.gen.rally.dto;

import com.gen.rally.entity.GameStyle;
import com.gen.rally.entity.GameType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;


// 매칭 신청 시 프론트에서 전달하는 데이터 형식
@Getter @Setter
@NoArgsConstructor
public class MatchRequestCreateDto {
    private String userId;
    private int gameType;
    private int gameStyle;
    private boolean sameGender;

    private LocalDate gameDate;
    private int startTime;
    private int endTime;

    private String place;
    private double latitude;
    private double longitude;
}