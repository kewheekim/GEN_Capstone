package com.gen.rally.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Setter @Getter
public class MatchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private Gender gender;
    private int skill;
    private GameType gameType;   // 경기 유형: 단식(0), 복식(1)
    private GameStyle gameStyle; // 경기 스타일: 상관없음(0), 편하게(1), 열심히(2)
    private boolean sameGender;   // 경기 상대 같은 성별?: 상관없음(0), 같은 성별(1)
    //시간
    private LocalDate gameDate;
    private int startTime;
    private int endTime;
    // 장소
    private String place;
    private double latitude;
    private double longitude;
    private State state;       // 상태
    private LocalDateTime createdAt =  LocalDateTime.now();
}
