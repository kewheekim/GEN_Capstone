package com.example.rally.dto;

import lombok.Getter;

@Getter
public class GeneralLoginResponse {
    private Long userId;
    private String name;
    private String accessToken;
    private String refreshToken;
}
