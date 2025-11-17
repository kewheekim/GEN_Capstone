package com.gen.rally.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class GameSetScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private int setNumber;
    private int user1Score;
    private int user2Score;

    private Integer durationSeconds;  // 세트 소요 시간(초)
}
