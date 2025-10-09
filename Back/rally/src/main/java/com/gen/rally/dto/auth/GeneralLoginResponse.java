package com.gen.rally.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeneralLoginResponse {
    private Long userId;
    private String name;
    private String accessToken;
    private String refreshToken;
}
