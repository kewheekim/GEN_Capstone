package com.gen.rally.controller;

import com.gen.rally.dto.TierAssessRequest;
import com.gen.rally.dto.TierAssessResponse;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import com.gen.rally.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    // 메인 화면
    @GetMapping("/api/home")
    public void home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails == null){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String userId = userDetails.getUsername();
        User user = userRepository.findByUserId(userId).
                orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));

    }

    // 실력 자가진단
    @PostMapping("/api/users/tier")
    public ResponseEntity<?> getTier(@RequestBody TierAssessRequest request, @AuthenticationPrincipal CustomUserDetails userDetails){
        if(userDetails == null){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String userId = userDetails.getUsername();
        TierAssessResponse response = userService.getFirstTier(request, userId);
        return ResponseEntity.ok(response);
    }

    // 매너점수 갱신
    @PostMapping("/api/users/manner")
    public void setManner( int manner, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails == null){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String userId = userDetails.getUsername();
        // 반환 필요없음
        return ;
    }
}
