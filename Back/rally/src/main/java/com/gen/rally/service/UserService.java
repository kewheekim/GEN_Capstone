package com.gen.rally.service;

import com.gen.rally.dto.GeneralSignupRequest;
import com.gen.rally.dto.auth.SocialSignupRequest;
import com.gen.rally.enums.LoginType;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> generalSignup(GeneralSignupRequest request){
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setProfileImage(request.getProfileImage());
        user.setGender(request.getGender());
        user.setLoginType(LoginType.NORMAL);

        userRepository.save(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    public ResponseEntity<?> socialSignup(SocialSignupRequest request, String socialId, LoginType loginType){
        User user = userRepository.findBySocialIdAndLoginType(socialId, loginType)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setName(request.getName());
        user.setGender(request.getGender());

        if (request.getProfileImage() != null) {
            byte[] decodedImage = Base64.getDecoder().decode(request.getProfileImage());
            user.setProfileImage(decodedImage);
        }

        userRepository.save(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
