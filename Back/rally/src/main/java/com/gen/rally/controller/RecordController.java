package com.gen.rally.controller;

import com.gen.rally.dto.RecordCalendarResponse;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.service.RecordService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/record")
@RestController
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    /* 기록 - 분석 탭
    @GetMapping("/analysis")
    public ResponseEntity<?> recordAnalysis(@AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok();
    }*/

    // 기록 - 달력 탭
    @GetMapping("/calendar")
    public ResponseEntity<?> getMonthlyGames(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam int year, @RequestParam int month){

        return ResponseEntity.ok(recordService.getMonthlyGames(userDetails.getId(), year, month));
    }
}
