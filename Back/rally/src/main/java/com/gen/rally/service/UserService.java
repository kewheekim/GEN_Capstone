package com.gen.rally.service;

import com.gen.rally.config.jwt.JwtProvider;
import com.gen.rally.dto.TierAssessRequest;
import com.gen.rally.dto.TierAssessResponse;
import com.gen.rally.dto.auth.*;
import com.gen.rally.enums.LoginType;
import com.gen.rally.entity.User;
import com.gen.rally.enums.Tier;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {
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
        userRepository.save(user);

        String subject = String.valueOf(user.getId());

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

    @Transactional
    public TierAssessResponse getFirstTier(TierAssessRequest request, String userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int selfQ = request.getQ1() + request.getQ2() + request.getQ3();
        int expQ = request.getQ4();
        int careerQ = request.getQ5();

        double totalScore = (selfQ/15.0)*40 + (expQ/5.0)*40 + (careerQ/7.0)*20;
        int score = (int) totalScore;
        TierAssessResponse response = new TierAssessResponse();
        response.setScore(score);
        response.setNickname(user.getName());
        user.setSkill(score);

        if(totalScore>=80.0){
            response.setTier(Tier.valueOf("상급자1"));
            user.setTier(Tier.valueOf("상급자1"));
        }else if(totalScore>=60.0){
            response.setTier(Tier.valueOf("중급자1"));
            user.setTier(Tier.valueOf("중급자1"));
        }else if(totalScore>=40.0){
            response.setTier(Tier.valueOf("초보자1"));
            user.setTier(Tier.valueOf("초보자1"));
        }else {
            response.setTier(Tier.valueOf("입문자1"));
            user.setTier(Tier.valueOf("입문자1"));
        }
        return response;
    }

    public void setManner(double manner, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
