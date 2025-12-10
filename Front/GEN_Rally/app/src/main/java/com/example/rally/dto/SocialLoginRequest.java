package com.example.rally.dto;

import lombok.Getter;

@Getter
public class SocialLoginRequest {
    private String accessToken;

    public SocialLoginRequest(String accessToken){
        this.accessToken = accessToken;
    }
}
