package com.gen.rally.service;

import com.gen.rally.dto.GeneralSignupRequest;
import com.gen.rally.enums.LoginType;
import com.gen.rally.entity.User;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> signup(GeneralSignupRequest request){
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

}
