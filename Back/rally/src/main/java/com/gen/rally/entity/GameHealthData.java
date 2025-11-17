package com.gen.rally.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class GameHealthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    private int totalPoints; // 경기 획득 세트
    // 헬스 데이터
    private Integer steps;
    private Integer maxHr;
    private Integer minHr;
    private Integer calories;
    @Column(columnDefinition = "json")
    private String seriesHr;
}
