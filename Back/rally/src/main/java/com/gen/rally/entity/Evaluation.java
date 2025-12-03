package com.gen.rally.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter @Setter
@Table(
        name = "evaluation",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_eval_once_per_match", columnNames = {"game_id", "evaluator", "subject"})
        },
        indexes = {
                @Index(name = "idx_eval_subject_created", columnList = "subject, created_at DESC"),
                @Index(name = "idx_eval_game", columnList = "game_id")
        }
)
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Long evaluationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator", referencedColumnName = "user_id")
    private User evaluator; // 평가하는 유저 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject", referencedColumnName = "user_id")
    private User subject;   // 평가 받는 유저 아이디

    @Column(name = "manner_score", nullable = false)
    private Double mannerScore;

    @Column(name = "comment")
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();
}
