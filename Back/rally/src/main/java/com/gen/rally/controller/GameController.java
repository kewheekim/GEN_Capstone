package com.gen.rally.controller;

import com.gen.rally.dto.GameHealthRequest;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.service.GameHealthService;
import com.gen.rally.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;
    private final GameHealthService healthService;

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelGame( @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Long gameId
    ) {
        String userId = userDetails.getUsername();
        gameService.cancelGame(userId, gameId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/health/save")
    public ResponseEntity<Void> saveHealth(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody GameHealthRequest request){
        Long userId = userDetails.getId();
        healthService.saveHealth(userId, request);
        return ResponseEntity.ok().build();
    }
}
