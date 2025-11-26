package com.gen.rally.service;

import com.gen.rally.dto.RecordCalendarResponse;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.GoalRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GameRepository gameRepository;

    /* 기록 - 분석 탭
    public ResponseEntity<?> showAnalysis(Long userId){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 사용자가 미달성한 목표

        // 소모 칼로리

        // 최근 경기 기록 5개

        // 내가 받은 칭찬
    }*/

    // 기록 - 달력 탭
    public List<RecordCalendarResponse> getMonthlyGames(Long userId, int year, int month){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 현재 월 기준, 게임한 날짜에 따라 select한 캘린더 띄우기
        List<Game> games = gameRepository.findByUserAndDateBetween(user, startDate, endDate);

        return games.stream()
                .map(game -> RecordCalendarResponse.builder()
                        .gameId(game.getGameId())
                        .date(game.getDate())
                        .build())
                .collect(Collectors.toList());
        // TODO: 경기 dto (날짜, 점수, 상대방 프로필, 승패여부, 시간, 걸음, 칼로리) 가져오기

    }

}
