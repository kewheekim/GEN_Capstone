package com.gen.rally.repository;

import com.gen.rally.entity.Evaluation;
import com.gen.rally.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsByGameAndEvaluator_UserId(Game game, String evaluatorUserId);

    // 나에 대한 평가 5개 (subject.userId 기준)
    List<Evaluation> findTop5BySubject_UserIdOrderByCreatedAtDesc(String subjectUserId);

    // 특정 경기에서 내가 받은 평가
    Optional<Evaluation> findByGameAndSubject_UserId(Game game, String subjectUserId);

    @Query("SELECT e FROM Evaluation e " +
            "JOIN FETCH e.evaluator " +
            "JOIN FETCH e.game " +
            "WHERE e.subject.userId = :userId " +
            "ORDER BY e.createdAt DESC")
    List<Evaluation> findCommentsByUserId(@Param("userId") String userId, Pageable pageable);}
