package com.gen.rally.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class GameScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "game_id")
    private Game game;

    // 세트 스코어 (예: 2:1)
    private Integer user1Sets;
    private Integer user2Sets;

    // 전체 플레이 시간
    private Integer totalElapsedSec;
    // 세트별 점수, 시간
    @Column(columnDefinition = "json")
    private String setsJson;
}
