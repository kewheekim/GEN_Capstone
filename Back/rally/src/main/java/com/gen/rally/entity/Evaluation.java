package com.gen.rally.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
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

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "evaluator", nullable = false)
    private String evaluator; // 평가하는 유저 아이디

    @Column(name = "subject", nullable = false)
    private String subject;   // 평가 받는 유저 아이디

    @Column(name = "manner_score", nullable = false)
    private Double mannerScore;

    @Column(name = "comment")
    private String comment;

    private LocalDateTime createdAt =  LocalDateTime.now();

    public Long getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public String getEvaluator() { return evaluator; }
    public void setEvaluator(String evaluator) { this.evaluator = evaluator; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Double getMannerScore() { return mannerScore; }
    public void setMannerScore(Double mannerScore) { this.mannerScore = mannerScore; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
