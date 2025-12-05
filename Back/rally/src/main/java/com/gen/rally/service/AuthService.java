package com.gen.rally.service;

import com.gen.rally.config.jwt.JwtProvider;
import com.gen.rally.dto.auth.*;
import com.gen.rally.entity.User;
import com.gen.rally.enums.LoginType;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public SignupResponse generalSignup(GeneralSignupRequest request){
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setImageUrl(request.getImageUrl());
        user.setGender(request.getGender());
        user.setPrimaryThing(request.getPrimaryThing());
        user.setLoginType(LoginType.NORMAL);
        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);

        String subject =user.getUserId();

        String accessToken  = jwtProvider.generateAccessToken(subject);
        String refreshToken = jwtProvider.generateRefreshToken(subject);

        return new SignupResponse(
                user.getUserId(),
                user.getName(),
                accessToken,
                refreshToken,
                user.getId()
        );
    }

    public ResponseEntity<?> socialSignup(SocialSignupRequest request, String socialId, LoginType loginType){
        User user = userRepository.findBySocialIdAndLoginType(socialId, loginType)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setName(request.getName());
        user.setGender(request.getGender());
        user.setPrimaryThing(request.getPrimaryThing());

        userRepository.save(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 일반 로그인
    public GeneralLoginResponse login(GeneralLoginRequest request){
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        String accessToken  = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        GeneralLoginResponse res = new GeneralLoginResponse(user.getId(),user.getName(),accessToken,refreshToken);
        return res;
    }



}
