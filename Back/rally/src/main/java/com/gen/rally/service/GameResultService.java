package com.gen.rally.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gen.rally.dto.GameHealthDto;
import com.gen.rally.dto.GameInfoDto;
import com.gen.rally.dto.GameResultResponse;
import com.gen.rally.dto.SetResultDto;
import com.gen.rally.entity.Evaluation;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.GameHealth;
import com.gen.rally.entity.GameScore;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.EvaluationRepository;
import com.gen.rally.repository.GameHealthRepository;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.GameScoreRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameResultService {
    private final GameRepository gameRepository;
    private final GameScoreRepository gameScoreRepository;
    private final GameHealthRepository gameHealthRepository;
    private final EvaluationRepository evaluationRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public GameResultResponse getGameResult(Long gameId, String userId) {
        // Game 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        boolean isUser1 = game.getUser1().getUserId().equals(userId);
        User me = isUser1 ? game.getUser1() : game.getUser2();
        User opponent = isUser1 ? game.getUser2() : game.getUser1();

        // GameScore 조회
        GameScore gameScore = gameScoreRepository.findByGame_GameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_SCORE_NOT_FOUND));

        // 상단 GameInfoDto 구성
        GameInfoDto gameInfoDto = new GameInfoDto();
        gameInfoDto.setGameId(game.getGameId());
        gameInfoDto.setDateText(formatGameDateText(game.getDate())); // "4월 14일 경기"
        gameInfoDto.setPlace(game.getPlace());

        gameInfoDto.setMyName(me.getName());
        gameInfoDto.setOpponentName(opponent.getName());
        gameInfoDto.setMyProfileUrl(me.getImageUrl());
        gameInfoDto.setOpponentProfileUrl(opponent.getImageUrl());

        List<SetResultDto> setDtos = new ArrayList<>();

        // 획득 세트
        int mySetScore = isUser1 ? nvl(gameScore.getUser1Sets()) : nvl(gameScore.getUser2Sets());
        int oppSetScore = isUser1 ? nvl(gameScore.getUser2Sets()) : nvl(gameScore.getUser1Sets());
        gameInfoDto.setMySetScore(mySetScore);
        gameInfoDto.setOpponentSetScore(oppSetScore);

        // 전체시간
        Integer totalSec = gameScore.getTotalElapsedSec();
        gameInfoDto.setTotalDuration(totalSec != null ? formatDuration(totalSec) : "00:00:00");

        // 세트별 점수/시간
        if (gameScore.getSetsJson() != null) {
            List<SetRow> rows = parseSetsJson(gameScore.getSetsJson());
            for (SetRow row : rows) {
                SetResultDto dto = new SetResultDto();
                dto.setSetNumber(row.getSetNumber());

                int myScore = isUser1 ? row.getUser1Score() : row.getUser2Score();
                int oppScore = isUser1 ? row.getUser2Score() : row.getUser1Score();

                dto.setMyScore(myScore);
                dto.setOpponentScore(oppScore);
                dto.setTime(formatDuration(row.getElapsedSec()));

                setDtos.add(dto);
            }
        }

        // 헬스데이터
        GameHealth health = gameHealthRepository
                .findByGame_GameIdAndUser_UserId(gameId, userId);

        GameHealthDto gameHealthDto = null;
        if (health != null) {
            gameHealthDto = new GameHealthDto();
            gameHealthDto.setMaxHr(health.getMaxHr());
            gameHealthDto.setMinHr(health.getMinHr());
            gameHealthDto.setSteps(health.getSteps());
            gameHealthDto.setCalories(health.getCalories());
            String rawJson = health.getSeriesHr();

            if (rawJson != null && !rawJson.isEmpty()) {
                try {
                    // 전체 JSONObject 변환
                    JsonNode node = objectMapper.readTree(rawJson);

                    // heartSeries 배열
                    JsonNode arrNode = node.get("heartSeries");

                    if (arrNode != null && arrNode.isArray()) {
                        List<GameHealthDto.HeartSampleDto> parsed =
                                objectMapper.readValue(
                                        arrNode.toString(),
                                        new TypeReference<List<GameHealthDto.HeartSampleDto>>() {}
                                );

                        gameHealthDto.setSeriesHr(parsed);
                    } else {
                        gameHealthDto.setSeriesHr(new ArrayList<>());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    gameHealthDto.setSeriesHr(new ArrayList<>());
                }
            } else {
                gameHealthDto.setSeriesHr(new ArrayList<>());
            }
        }

        // 상대의 평가
        String subject = String.valueOf(me.getUserId());

        Evaluation evaluation = evaluationRepository.findByGameAndSubject_UserId(game, subject).orElse(null);

        String comment = evaluation != null ? evaluation.getComment() : null;

        GameResultResponse response = new GameResultResponse();
        response.setGame(gameInfoDto);
        response.setSets(setDtos);
        response.setHealth(gameHealthDto);
        response.setComment(comment);

        return response;
    }

    private String formatGameDateText(LocalDate date) {
        if (date == null) return "";
        return date.getMonthValue() + "월 " + date.getDayOfMonth() + "일 경기";
    }

    private String formatDuration(int totalSec) {
        int hours = totalSec / 3600;
        int minutes = (totalSec % 3600) / 60;
        int seconds = totalSec % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    // setsJson 파싱
    private List<SetRow> parseSetsJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<SetRow>>() {});
        } catch (Exception e) {
            // 파싱 실패 시 빈 리스트 반환
            return List.of();
        }
    }

    @Getter @Setter
    public static class SetRow {
        private Integer setNumber;
        private Integer user1Score;
        private Integer user2Score;
        private Integer elapsedSec;
    }
}
