package com.gen.rally.entity;

import com.gen.rally.enums.GoalTheme;
import com.gen.rally.enums.GoalType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalTheme theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalType goalType;

    private Integer targetWeeks;  // 기간
    private Integer targetCount;  // 횟수
    private LocalDate endDate;   // 마감 날짜
    @Column(nullable = false)
    private Integer progressCount = 0;  // 실천 횟수
    private Integer calorie; // 목표 칼로리

    @Column(nullable = false)
    private boolean achieved = false; // 달성 여부
}
