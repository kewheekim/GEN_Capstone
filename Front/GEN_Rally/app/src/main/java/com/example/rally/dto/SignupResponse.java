package com.example.rally.dto;

import lombok.Getter;

@Getter
public class SignupResponse {
    private String userId;
    private String name;
    private String accessToken;
    private String refreshToken;
}
