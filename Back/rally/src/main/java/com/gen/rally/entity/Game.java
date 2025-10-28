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
    private Long gameId;

    @ManyToOne
    @JoinColumn(name = "request_id1")
    private MatchRequest requestId1; // 요청은 두 개이므로, 두 개의 아이디로 관리

    @ManyToOne
    @JoinColumn(name = "request_id2")
    private MatchRequest requestId2;

    @ManyToOne
    @JoinColumn(name = "user_id1", referencedColumnName =  "user_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user_id2", referencedColumnName =  "user_id")
    private User user2;

    private LocalDate date;
    private String time;
    private String place;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type")
    private GameType gameType;

    @ManyToOne
    @JoinColumn(name = "winner", referencedColumnName = "user_id")
    private User winner;
    // @@ score 필드 추가하기
}
