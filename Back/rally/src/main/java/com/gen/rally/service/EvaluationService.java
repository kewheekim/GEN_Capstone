package com.gen.rally.service;

import com.gen.rally.dto.EvaluationCreateRequest;
import com.gen.rally.entity.Evaluation;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.EvaluationRepository;
import com.gen.rally.repository.GameRepository;
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
    private final GameRepository gameRepository;

    @Transactional
    public Long createEvaluation(EvaluationCreateRequest req, String evaluatorUserId) {
        // 사용자 존재 확인
        User evaluator = userRepository.findByUserId(evaluatorUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 경기 존재 확인
        Game game = gameRepository.findByGameId(req.getGameId())
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));
        // user1, 2 판단
        String user1Id = game.getUser1().getUserId();
        String user2Id = game.getUser2().getUserId();

        User subject;
        if (game.getUser1().getUserId().equals(evaluatorUserId)) {
            subject = game.getUser2();
        } else if (game.getUser2().getUserId().equals(evaluatorUserId)) {
            subject = game.getUser1();
        } else {
            // 이 경기에 참가한 사람이 아닌데 평가하려는 경우
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 점수 검증: 1.0~5.0
        double score = req.getMannerScore();
        if (Double.isNaN(score) || Double.isInfinite(score)
                || score < 1.0 || score > 5.0 || !isHalfStep(score)) {
            throw new CustomException(ErrorCode.INVALID_STATE);
        }

        // 중복 평가 방지
        if (evaluationRepository.existsByGameAndEvaluator_UserId(game, evaluatorUserId)) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        // 저장
        Evaluation e = new Evaluation();
        e.setGame(game);
        e.setEvaluator(evaluator);
        e.setSubject(subject);
        e.setMannerScore(score);
        e.setComment(req.getComment());

        try {
            return evaluationRepository.save(e).getEvaluationId();
        } catch (DataIntegrityViolationException ex) {
            throw new CustomException(ErrorCode.CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public Double getLast5MannerAvg(String subjectUserId) {
        userRepository.findByUserId(subjectUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Evaluation> latest5 =
                evaluationRepository.findTop5BySubject_UserIdOrderByCreatedAtDesc(subjectUserId);

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