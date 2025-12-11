package com.example.rally.dto;

import com.kakao.sdk.user.model.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SocialSignupRequest {
    private String name;
    private String imageUrl;
    private String gender;
    private String primaryThing;
    private String fcmToken;
}
