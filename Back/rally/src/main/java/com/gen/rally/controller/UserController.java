package com.gen.rally.controller;

import com.gen.rally.dto.*;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Slf4j
@Controller
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
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(ErrorResponse.from(ErrorCode.PASSWORD_MISMATCH));
        }
        return userService.generalSignup(request);
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
}
