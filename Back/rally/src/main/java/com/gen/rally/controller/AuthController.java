package com.gen.rally.controller;

import com.gen.rally.dto.TierAssessRequest;
import com.gen.rally.dto.TierAssessResponse;
import com.gen.rally.dto.auth.*;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.enums.LoginType;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorResponse;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import com.gen.rally.service.AuthService;
import com.gen.rally.service.KakaoService;
import com.gen.rally.service.NaverService;
import com.gen.rally.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final NaverService naverService;

    // jwt 사용 일반 회원가입
    @PostMapping("/api/users/signup")
    public ResponseEntity<?> signup(@RequestBody GeneralSignupRequest request) throws IOException {
        if(userRepository.findByUserId(request.getUserId()).isPresent()) {
            return ResponseEntity.badRequest().body(ErrorResponse.from(ErrorCode.USER_ALREADY_EXISTS));
        }
        SignupResponse res = authService.generalSignup(request);
        return ResponseEntity.ok(res);
    }

    // 일반 로그인
    @PostMapping("/api/users/login")
    public ResponseEntity<?> login(@RequestBody GeneralLoginRequest request){
        GeneralLoginResponse res = authService.login(request);
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
    public ResponseEntity<?> naverCallback(@RequestParam String code) throws IOException {
        NaverTokenResponse tokenDto = naverService.getAccessToken(code);
        NaverUserInfoDto userInfo = naverService.getUserInfo(tokenDto.getAccessToken());
        NaverLoginResponse loginResponse = naverService.loginOrSignup(userInfo);

        return ResponseEntity.ok(loginResponse);
    }

    // 소셜 로그인 후 -> 세부 정보 설정
    @PostMapping("/api/users/profile")
    public ResponseEntity<?> socialSignup(@RequestBody SocialSignupRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        String socialId = userDetails.getUsername();
        LoginType loginType = LoginType.KAKAO;
        return authService.socialSignup(request, socialId, loginType);
    }

    // [Android용] 카카오 로그인
    @PostMapping("/api/auth/login/kakao")
    public ResponseEntity<?> kakaoLoginAndroid(@RequestBody SocialLoginRequest request) {
        System.out.println(">>> 카카오 로그인 요청 들어옴! 토큰: " + request.getAccessToken());

        KakaoUserInfoDto userInfo = kakaoService.getUserInfo(request.getAccessToken());
        KakaoLoginResponse loginResponse = kakaoService.loginOrSignup(userInfo);

        return ResponseEntity.ok(loginResponse);
    }

    // [Android용] 네이버 로그인
    @PostMapping("/api/auth/login/naver")
    public ResponseEntity<?> naverLoginAndroid(@RequestBody SocialLoginRequest request) {
        NaverUserInfoDto userInfo = naverService.getUserInfo(request.getAccessToken());
        NaverLoginResponse loginResponse = naverService.loginOrSignup(userInfo);
        return ResponseEntity.ok(loginResponse);
    }
}
