package com.gen.rally.repository;

import com.gen.rally.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsByGameIdAndEvaluator_UserId(Long gameId, String evaluatorUserId);

    // 나에 대한 평가 5개 (subject.userId 기준)
    List<Evaluation> findTop5BySubject_UserIdOrderByCreatedAtDesc(String subjectUserId);

    // 특정 경기에서 내가 받은 평가
    Optional<Evaluation> findByGameIdAndSubject_UserId(Long gameId, String subjectUserId);
}
