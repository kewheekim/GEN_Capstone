package com.gen.rally.controller;

import com.gen.rally.dto.TierAssessRequest;
import com.gen.rally.dto.TierAssessResponse;
import com.gen.rally.dto.auth.*;
import com.gen.rally.enums.LoginType;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorResponse;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import com.gen.rally.service.KakaoService;
import com.gen.rally.service.NaverService;
import com.gen.rally.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Check;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final NaverService naverService;

    // jwt 사용 일반 회원가입
    @PostMapping("/api/users/signup")
    public ResponseEntity<?> signup(@RequestBody GeneralSignupRequest request) throws IOException {
        if(userRepository.findByUserId(request.getUserId()).isPresent()) {
            return ResponseEntity.badRequest().body(ErrorResponse.from(ErrorCode.USER_ALREADY_EXISTS));
        }
        SignupResponse res = userService.generalSignup(request);
        return ResponseEntity.ok(res);
    }

    // 아이디 중복 체크
    @PostMapping("/api/users/check-id")
    public ResponseEntity<?> checkId(@RequestBody CheckIdRequest request) throws IOException {
        String id = request.getUserId();
        if(userRepository.findByUserId(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.from(ErrorCode.USER_ALREADY_EXISTS));
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    // 닉네임 중복 체크
    @GetMapping("/api/users/check-nickname")
    public ResponseEntity<CheckNicknameResponse> checkNickname(@RequestParam String nickname) {
        String name = nickname.trim();
        if (!name.matches("^[a-zA-Z0-9가-힣]{2,12}$")) {
            return ResponseEntity.ok(new CheckNicknameResponse(false, "형식 오류"));
        }
        boolean exists = userRepository.existsByNameIgnoreCase(name);
        if (exists) {
            return ResponseEntity.ok(new CheckNicknameResponse(false, "이미 사용 중인 닉네임입니다"));
        }
        return ResponseEntity.ok(new CheckNicknameResponse(true, "사용 가능한 닉네임입니다"));
    }

    // kakao 로그인 & 회원가입
    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) throws IOException {
        KakaoTokenResponse tokenDto = kakaoService.getAccessToken(code);
        KakaoUserInfoDto userInfo = kakaoService.getUserInfo(tokenDto.getAccessToken());
        KakaoLoginResponse loginResponse = kakaoService.loginOrSignup(userInfo);

        return ResponseEntity.ok(loginResponse);
    }

    // naver 로그인 & 회원가입
    @GetMapping("/auth/naver/callback")
    public ResponseEntity<?> naverCallback(@RequestParam String code, @RequestParam String state) throws IOException {
        // 개발 중이므로 검증은 생략 or rally-test만 허용
        if (!state.equals("rally-test")) {
            throw new CustomException(ErrorCode.INVALID_STATE);
        }
        NaverTokenResponse tokenDto = naverService.getAccessToken(code, state);
        NaverUserInfoDto userInfo = naverService.getUserInfo(tokenDto.getAccessToken());
        NaverLoginResponse loginResponse = naverService.loginOrSignup(userInfo);

        return ResponseEntity.ok(loginResponse);
    }

    // 소셜 로그인 후 -> 세부 정보 설정
    @PostMapping("/api/users/profile")
    public ResponseEntity<?> socialSignup(@RequestBody SocialSignupRequest request, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        String socialId = userDetails.getUsername();
        LoginType loginType = LoginType.KAKAO;
        return userService.socialSignup(request, socialId, loginType);
    }

    // 실력 자가진단
    @PostMapping("/api/users/tier") public ResponseEntity<?> getTier(@RequestBody TierAssessRequest request, @AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null){
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String userId = userDetails.getUsername();
        TierAssessResponse response = userService.getFirstTier(request, userId);
        return ResponseEntity.ok(response);
    }
}
