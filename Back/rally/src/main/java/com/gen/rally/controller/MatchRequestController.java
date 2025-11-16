package com.gen.rally.controller;

import com.gen.rally.dto.*;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.service.GameService;
import com.gen.rally.service.MatchRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchRequestController {
    private final MatchRequestService matchService;
    private final GameService gameService;
    @PostMapping("/request")
    public ResponseEntity<Long> requestMatch(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MatchRequestCreateDto dto) {
        // match_request 테이블에 데이터 저장
        Long requestId = matchService.createMatchRequest(userDetails.getUsername(), dto);
        // request id 반환
        return ResponseEntity.ok(requestId);
    }

    @PostMapping("/candidates")
    public ResponseEntity<List<CandidateResponseDto>> getCandidates(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MatchRequestCreateDto dto) {
        // findCandidates 매칭 후보 필터링
        List<CandidateResponseDto> candidates = matchService.findCandidates(userDetails.getUsername(), dto);
        // 매칭 후보 반환
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/found")
    public ResponseEntity<List<MatchFoundItem>> found(@AuthenticationPrincipal CustomUserDetails userDetails ) {
        return ResponseEntity.ok(gameService.findFound(userDetails.getUsername(), userDetails.getId()));
    }
    @GetMapping("/seeking")
    public ResponseEntity<List<MatchSeekingItem>> seeking(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MatchSeekingItem> body = matchService.findSeekingMatchByUser(userDetails.getUsername());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/details")
    public ResponseEntity<MatchRequestDetails> getInvitationDetail(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam Long myRequestId, @RequestParam Long opponentRequestId
    ) {
        return ResponseEntity.ok(matchService.getMatchRequestDetails(userDetails.getUsername(), myRequestId, opponentRequestId));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> refuse(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Long requestId) {
        String userId = userDetails.getUsername();
        matchService.cancelRequest(userDetails.getUsername(), requestId);
        return ResponseEntity.ok().build();
    }
}