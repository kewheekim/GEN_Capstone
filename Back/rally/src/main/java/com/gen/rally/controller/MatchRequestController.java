package com.gen.rally.controller;

import com.gen.rally.dto.CandidatesResponseDto;
import com.gen.rally.dto.MatchRequestCreateDto;
import com.gen.rally.service.MatchRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchRequestController {
    private final MatchRequestService matchRequestService;
    @PostMapping("/request")
    public ResponseEntity<List<CandidatesResponseDto>> requestMatch(@RequestBody MatchRequestCreateDto dto) {
            // match_request 테이블에 데이터 저장
            matchRequestService.createMatchRequest(dto);
            // findCandidates
            List<CandidatesResponseDto> candidates = matchRequestService.findCandidates(dto);
            return ResponseEntity.ok(candidates);
    }
}