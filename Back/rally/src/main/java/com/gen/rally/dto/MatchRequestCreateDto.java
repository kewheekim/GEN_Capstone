package com.gen.rally.dto;

import com.gen.rally.entity.GameStyle;
import com.gen.rally.entity.GameType;
import com.gen.rally.entity.Gender;
import lombok.Getter;
import lombok.Setter;

// 매칭 신청 시 전달되는 데이터 형식
@Getter @Setter
public class MatchRequestCreateDto {
    private String user_id;
    private GameType game_type;
    private GameStyle game_style;
    private int same_gender;
    private int start_time;
    private int end_time;
    private double latitude;
    private double longitude;
}
