package com.gen.rally.dto.auth;

import com.gen.rally.enums.Gender;
import com.gen.rally.enums.LoginType;
import com.gen.rally.enums.Primary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialSignupRequest {
    private String name;
    private String imageUrl;
    private Gender gender;
    private Primary primaryThing;
    private String fcmToken;
}
