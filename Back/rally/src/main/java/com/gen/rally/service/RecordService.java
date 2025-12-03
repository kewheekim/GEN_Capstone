package com.gen.rally.service;

import com.gen.rally.dto.*;
import com.gen.rally.entity.*;
import com.gen.rally.enums.State;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GameRepository gameRepository;
    private final EvaluationRepository evaluationRepository;
    private final GameHealthRepository gameHealthRepository;

    private String formatPlayTime(int totalSeconds) {
        if (totalSeconds == 0) {
            return "00:00:00";
        }
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    // 기록 - 분석 탭
    public ResponseEntity<RecordAnalysisResponse> showAnalysis(Long userId){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 사용자가 미달성한 목표
        List<Goal> goals = goalRepository.findByUserAndAchieved(user, false);
        List<GoalItem> goalDtos = goals.stream().map(goal -> GoalItem.builder()
                .id(goal.getId())
                .name(goal.getName())
                .theme(goal.getTheme().name())
                .progressCount(goal.getProgressCount())
                .targetWeeksCount(goal.getTargetWeeksCount())
                .build()
        ).collect(Collectors.toList());

        // 최근 경기 기록 5개
        List<Game> games = gameRepository.findTop5GamesByUser(user.getUserId());

        List<GameReviewDto> gameReviewDtos = games.stream().map(game -> {
            boolean isUser1 = game.getUser1().getId().equals(userId);
            User opponent = isUser1 ? game.getUser2() : game.getUser1();

            GameHealth myHealth = game.getGameHealth().stream()
                    .filter(h -> h.getUser().getId().equals(userId))
                    .findFirst()
                    .orElse(null);

            Integer mySteps = (myHealth != null) ? myHealth.getSteps() : 0;
            Integer myCalories = (myHealth != null) ? myHealth.getCalories() : 0;

            GameScore score = game.getGameScore();
            int myScoreStr = 0;
            int opScoreStr = 0;
            int totalSec = (score != null) ? score.getTotalElapsedSec() : null;

            if (score != null) {
                myScoreStr = isUser1 ? score.getUser1Sets() : score.getUser2Sets();
                opScoreStr = isUser1 ? score.getUser2Sets() : score.getUser1Sets();
            }

            return GameReviewDto.builder()
                .gameId(game.getGameId())
                .myScore(myScoreStr)
                .opponentScore(opScoreStr)
                .playTime(formatPlayTime(totalSec)) // Null check
                .steps(mySteps)
                .calories(myCalories)
                .opponentImage(opponent.getImageUrl())
                .build();
        }).toList();

        List<CommentDto> commentDtos = getComments(userId, 3);

        RecordAnalysisResponse res = new RecordAnalysisResponse();
        res.setGoalItems(goalDtos);
        res.setGameResults(gameReviewDtos);
        res.setComments(commentDtos);

        return ResponseEntity.ok(res);
    }

    // 기록 - 주차별 칼로리
    public ResponseEntity<RecordWeeklyCalorie> getWeeklyCalories(Long userId, LocalDate date){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDate targetDate = (date == null) ? LocalDate.now() : date;
        LocalDate startOfWeek = targetDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = targetDate.with(DayOfWeek.SUNDAY);

        List<GameHealth> myHealths = gameHealthRepository.findMyHealthRecords(user.getUserId(), startOfWeek, endOfWeek);

        // 날짜별 칼로리 합산
        Map<LocalDate, Integer> calorieMap = myHealths.stream()
                .collect(Collectors.groupingBy(
                        gh -> gh.getGame().getDate(),
                        Collectors.summingInt(GameHealth::getCalories)
                ));

        List<RecordWeeklyCalorie.DailyCalorie> dailyList = new ArrayList<>();
        LocalDate current = startOfWeek;

        while (!current.isAfter(endOfWeek)) {
            int cal = calorieMap.getOrDefault(current, 0);

            String dayKor = current.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.KOREAN);

            dailyList.add(RecordWeeklyCalorie.DailyCalorie.builder()
                    .dayOfWeek(dayKor)
                    .calories(cal)
                    .date(current.toString())
                    .build());

            current = current.plusDays(1);
        }

        String title = getWeekString(targetDate);

        return ResponseEntity.ok(RecordWeeklyCalorie.builder()
                .title(title)
                .dailyCalories(dailyList)
                .build());

    }

    // 몇월 몇주 계산
    private String getWeekString(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int weekOfMonth = date.get(weekFields.weekOfMonth());
        int month = date.getMonthValue();
        return month + "월 " + weekOfMonth + "주차";
    }

    // 기록 - 칭찬 가져오기
    public List<CommentDto> getComments(Long userId, int count){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, count);

        List<Evaluation> evals = evaluationRepository.findCommentsByUserId(user.getUserId(), pageable);

        return evals.stream().map(e -> {
            User sender = e.getEvaluator();
            Game game = e.getGame();

            return CommentDto.builder()
                    .nickname(sender.getName())
                    .tier(sender.getTier().toString())
                    .comment(e.getComment())
                    .gameStyle(game.getGameStyle().name())
                    .date(e.getCreatedAt().toLocalDate().toString())
                    .build();
        }).collect(Collectors.toList());
    }

    // 기록 - 달력 탭
    public RecordCalendarResponse getMonthlyGames(Long userId, int year, int month){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Game> games = gameRepository.findByUserAndDateBetweenAndState(user, startDate, endDate, State.경기완료);

        List<LocalDate> markedDates = games.stream()
                .map(Game::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<GameReviewDto> gameReviewDtos = games.stream().map(game -> {
            boolean isUser1 = game.getUser1().getId().equals(userId);
            User opponent = isUser1 ? game.getUser2() : game.getUser1();

            GameHealth myHealth = game.getGameHealth().stream()
                    .filter(h -> h.getUser().getId().equals(userId))
                    .findFirst()
                    .orElse(null);

            Integer mySteps = (myHealth != null) ? myHealth.getSteps() : 0;
            Integer myCalories = (myHealth != null) ? myHealth.getCalories() : 0;

            GameScore score = game.getGameScore();
            int myScoreStr = 0;
            int opScoreStr = 0;

            int totalSec = (score != null) ? score.getTotalElapsedSec() : null;

            if (score != null) {
                myScoreStr = isUser1 ? score.getUser1Sets() : score.getUser2Sets();
                opScoreStr = isUser1 ? score.getUser2Sets() : score.getUser1Sets();
            }

            return GameReviewDto.builder()
                    .gameId(game.getGameId())
                    .date(String.valueOf(game.getDate()))
                    .myScore(myScoreStr)
                    .opponentScore(opScoreStr)
                    .playTime(formatPlayTime(totalSec))
                    .steps(mySteps)
                    .calories(myCalories)
                    .opponentImage(opponent.getImageUrl())
                    .build();
        }).toList();

        return RecordCalendarResponse.builder()
                .markedDates(markedDates)
                .games(gameReviewDtos)
                .build();
    }
}
