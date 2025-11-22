package com.gen.rally.controller;

import com.gen.rally.dto.GoalCreateRequest;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goal")
public class GoalController {
    private final GoalService goalService;

    @PostMapping("/create")
    public ResponseEntity<Long> createGoal(@RequestBody GoalCreateRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long goalId = goalService.createGoal(request, userDetails.getUsername());
        return ResponseEntity.ok(goalId);
    }
}
