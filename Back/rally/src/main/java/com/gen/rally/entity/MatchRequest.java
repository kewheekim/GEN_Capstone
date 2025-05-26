package com.gen.rally.entity;

import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MatchRequest {
    @Id
    private Long request_id;
    private String user_id;
    private Gender gender;
    private int skill;
    private GameType game_type;   // 경기 유형: 단식(0), 복식(1)
    private GameStyle game_style; // 경기 스타일: 상관없음(0), 편하게(1), 열심히(2)
    private boolean same_gender;   // 경기 상대 같은 성별?: 상관없음(0), 같은 성별(1)
    //시간
    private LocalDate game_data;
    private int start_time;
    private int end_time;
    // 장소
    private double latitude;
    private double longitude;
    private State state;       // 상태
    private LocalDateTime created_at;
}
