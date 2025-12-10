package com.example.rally.dto;

import lombok.Getter;

@Getter
public class SocialLoginResponse {
    private TokenResponse token;
    private boolean isNew;
    private String name;
    private String socialId;
}
