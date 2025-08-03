package com.gen.rally.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NaverLoginResponse {
    private TokenResponse token;
    private boolean isNew;
    private String nickname;
    private String socialId;
}
