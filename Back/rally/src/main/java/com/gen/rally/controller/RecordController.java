package com.gen.rally.controller;

import com.gen.rally.dto.CommentDto;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/record")
@RestController
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    // 기록 - 분석 탭
    @GetMapping("/analysis")
    public ResponseEntity<?> getRecordAnalysis(@AuthenticationPrincipal CustomUserDetails userDetails){
        return recordService.showAnalysis(userDetails.getId());
    }

    // 분석 탭 - 주차별 칼로리 조회
    @GetMapping("/analysis/weekly-calorie")
    public ResponseEntity<?> getWeeklyCalorie(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){
        return recordService.getWeeklyCalories(userDetails.getId(), date);
    }

    // 분석 탭 - 칭찬 더보기
    @GetMapping("/analysis/comments")
    public List<CommentDto> getComments(@AuthenticationPrincipal CustomUserDetails userDetails){
        return recordService.getComments(userDetails.getId(), 10);
    }

    // 기록 - 달력 탭
    @GetMapping("/calendar")
    public ResponseEntity<?> getMonthlyGames(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam int year, @RequestParam int month){
        return ResponseEntity.ok(recordService.getMonthlyGames(userDetails.getId(), year, month));
    }

}
