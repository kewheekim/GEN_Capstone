package com.gen.rally.entity;

import com.gen.rally.enums.GameType;
import com.gen.rally.enums.State;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity @Setter @Getter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long game_id;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private MatchRequest request_id;

    @ManyToOne
    @JoinColumn(name = "user_id1")
    private User user1;
    @ManyToOne
    @JoinColumn(name = "user_id2")
    private User user2;

    private LocalDate date;
    private String time;
    private String place;

    private State state;
    private GameType gameType;

    @ManyToOne
    @JoinColumn(name = "winner")
    private User winner;
    // @@ score 필드 추가하기
}
