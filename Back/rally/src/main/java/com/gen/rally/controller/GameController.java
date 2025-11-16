package com.gen.rally.controller;

import com.gen.rally.dto.MatchFoundItem;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelGame( @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Long gameId
    ) {
        String userId = userDetails.getUsername();
        gameService.cancelGame(userId, gameId);
        return ResponseEntity.ok().build();
    }
}
