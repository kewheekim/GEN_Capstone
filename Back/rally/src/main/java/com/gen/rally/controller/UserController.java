package com.gen.rally.controller;

import com.gen.rally.dto.KakaoLoginResponse;
import com.gen.rally.exception.ErrorResponse;
import com.gen.rally.dto.GeneralSignupRequest;
import com.gen.rally.dto.KakaoTokenResponse;
import com.gen.rally.dto.KakaoUserInfoDto;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import com.gen.rally.service.KakaoService;
import com.gen.rally.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;
    private final UserRepository userRepository;

    // jwt signup
    @PostMapping("/api/users/signup")
    public ResponseEntity<?> signup(@RequestBody GeneralSignupRequest request) throws IOException {
        if(userRepository.findByUserId(request.getUserId()).isPresent()) {
            return ResponseEntity.badRequest().body(ErrorResponse.from(ErrorCode.USER_ALREADY_EXISTS));
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(ErrorResponse.from(ErrorCode.PASSWORD_MISMATCH));
        }
        return userService.signup(request);
    }

    // kakao signup & login
    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) throws IOException {
        KakaoTokenResponse tokenDto = kakaoService.getAccessToken(code);
        KakaoUserInfoDto userInfo = kakaoService.getUserInfo(tokenDto.getAccessToken());
        KakaoLoginResponse loginResponse = kakaoService.loginOrSignup(userInfo);

        return ResponseEntity.ok(loginResponse);
    }
}
