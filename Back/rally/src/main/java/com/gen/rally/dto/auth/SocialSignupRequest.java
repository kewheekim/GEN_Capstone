package com.gen.rally.dto.auth;

import com.gen.rally.enums.Gender;
import com.gen.rally.enums.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialSignupRequest {
    private String name;
    private byte[] profileImage;
    private Gender gender;
}
