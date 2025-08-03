package com.gen.rally.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private String userId;
    private String name;
    private String accessToken;
    private String refreshToken;
}
