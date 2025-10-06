package com.gen.rally.repository;

import com.gen.rally.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsByGameIdAndEvaluatorAndSubject(Long gameId, String evaluator, String subject);

    List<Evaluation> findTop5BySubjectOrderByCreatedAtDesc(String subject);
}
