package com.gen.rally.service;

import com.gen.rally.dto.EvaluationCreateRequest;
import com.gen.rally.entity.Evaluation;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.EvaluationRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createEvaluation(EvaluationCreateRequest req) {
        // 필수값 널 체크(간단)
        if (req.getGameId() == null ||
                req.getEvaluator() == null ||
                req.getSubject() == null ||
                req.getMannerScore() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "필수 값 누락");
        }

        // 사용자 존재 확인
        userRepository.findByUserId(req.getEvaluator())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "평가자 없음"));
        userRepository.findByUserId(req.getSubject())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "평가 대상자 없음"));

        // 점수 검증: 1.0~5.0, 0.5 step
        double score = req.getMannerScore();
        if (Double.isNaN(score) || Double.isInfinite(score)
                || score < 1.0 || score > 5.0 || !isHalfStep(score)) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "mannerScore는 1.0~5.0, 0.5 단위여야 합니다.");
        }

        // 중복 평가 방지
        if (evaluationRepository.existsByGameIdAndEvaluatorAndSubject(
                req.getGameId(), req.getEvaluator(), req.getSubject())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 이 경기에서 해당 사용자를 평가했습니다.");
        }

        // 저장
        Evaluation e = new Evaluation();
        e.setGameId(req.getGameId());
        e.setEvaluator(req.getEvaluator());
        e.setSubject(req.getSubject());
        e.setMannerScore(score);
        e.setComment(req.getComment());

        try {
            return evaluationRepository.save(e).getEvaluationId();
        } catch (DataIntegrityViolationException ex) {
            throw new CustomException(ErrorCode.INVALID_STATE);
        }
    }

    @Transactional(readOnly = true)
    public Double getLast5MannerAvg(String subjectUserId) {
        userRepository.findByUserId(subjectUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Evaluation> latest5 =
                evaluationRepository.findTop5BySubjectOrderByCreatedAtDesc(subjectUserId);

        if (latest5.size() < 5) return null;

        OptionalDouble avgOpt = latest5.stream()
                .mapToDouble(Evaluation::getMannerScore)
                .average();

        return avgOpt.isPresent()
                ? Math.round(avgOpt.getAsDouble() * 10.0) / 10.0
                : null;
    }

    private boolean isHalfStep(double v) {
        double t = v * 2.0;
        return Math.abs(t - Math.rint(t)) < 1e-9;
    }
}